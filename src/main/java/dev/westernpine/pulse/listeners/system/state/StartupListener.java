package dev.westernpine.pulse.listeners.system.state;

import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeSearchProvider;
import com.sedmelluq.lava.extensions.youtuberotator.YoutubeIpRotatorSetup;
import com.sedmelluq.lava.extensions.youtuberotator.tools.ip.IpBlock;
import com.sedmelluq.lava.extensions.youtuberotator.tools.ip.Ipv4Block;
import com.sedmelluq.lava.extensions.youtuberotator.tools.ip.Ipv6Block;
import dev.westernpine.bettertry.Try;
import dev.westernpine.eventapi.objects.EventHandler;
import dev.westernpine.eventapi.objects.Listener;
import dev.westernpine.lib.object.Scheduler;
import dev.westernpine.lib.object.State;
import dev.westernpine.pulse.Pulse;
import dev.westernpine.pulse.controller.ControllerFactory;
import dev.westernpine.pulse.controller.EndCase;
import dev.westernpine.pulse.controller.settings.SettingsFactory;
import dev.westernpine.pulse.events.console.ConsoleEvent;
import dev.westernpine.pulse.events.system.StateChangeEvent;
import dev.westernpine.pulse.listeners.system.jda.GuildInitializer;
import dev.westernpine.pulse.listeners.system.jda.InteractionListener;
import dev.westernpine.pulse.listeners.system.jda.MessageDeletionRequestListener;
import dev.westernpine.pulse.listeners.system.jda.ReadyListener;
import dev.westernpine.pulse.listeners.system.jda.controller.GuildVoiceListener;
import dev.westernpine.pulse.properties.IdentityProperties;
import dev.westernpine.pulse.sources.Router;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static dev.westernpine.pulse.logging.Logger.logger;

public class StartupListener implements Listener {

    @EventHandler
    public void onStartup(StateChangeEvent event) {
        if (!event.getNewState().is(State.STARTUP))
            return;

        try {
            /*
            Official System Startup.
             */
            logger.info("System Startup >> Starting Pulse (v)" + Pulse.version);

            /*
            Shutdown hooks...
             */
            Pulse.shutdownHooks.add(() -> {
                logger.info("Terminating scheduler.");
                Pulse.scheduler.shutdownNow();
                logger.info("Closing console inputs.");
                Try.to(Pulse.input::close).onFailure(Throwable::printStackTrace);
                if (Objects.nonNull(Pulse.shardManager)) {
                    logger.info("Shutting down controllers.");
                    ControllerFactory.getControllers().values().forEach(controller -> controller.destroy(EndCase.BOT_RESTART));
                    logger.info("Shutting down shard manager.");
                    Pulse.shardManager.shutdown();
                }
            });

            /*
            Set up console listening system via events.
             */
            logger.info("Initializing console listener.");
            Pulse.scheduler.runAsync(() -> Scheduler.loop(() -> true, () -> Try.to(() -> Scheduler.loop(() -> !Try.to(Pulse.input::ready).getUnchecked(), () -> Try.to(() -> Thread.sleep(100)).map(nv -> false).getUnchecked())).map(nv -> Try.to(Pulse.input::readLine).map(ConsoleEvent::new).onSuccess(consoleEvent -> Try.to(() -> Pulse.eventManager.call(consoleEvent)).onFailure(Throwable::printStackTrace).onFailure(throwable -> logger.warning("Exception caught in command handler."))).map(so -> false).orElse(true)).orElse(true)));

            /*
			Initialize the ready notifier format completion.
            */
            Pulse.readyNotifier = new CompletableFuture<>();

            /*
			Initialize the ready handler to handle JDA readies.
            */
            Pulse.readyHandler = readyEvent -> {
                JDA jda = readyEvent.getJDA();
                int shardId = jda.getShardInfo().getShardId();
                logger.info("Ready event fired for shard ID: " + shardId);
                jda.getPresence().setPresence(OnlineStatus.IDLE, Activity.watching("Shard initialized! Waiting for other shards..."));
                if (!Pulse.readyNotifier.isDone() && Pulse.shardManager.getShardsQueued() <= 0)
                    Pulse.readyNotifier.complete(true);
            };

            /*
            Initialize the audio player manager.
             */
            logger.info("Building and initializing the audio player manager.");
            Pulse.audioPlayerManager = new DefaultAudioPlayerManager();
            Pulse.audioPlayerManager.setFrameBufferDuration(3000);
            Pulse.audioPlayerManager.getConfiguration().setFilterHotSwapEnabled(true);
            Pulse.audioPlayerManager.getConfiguration().setResamplingQuality(AudioConfiguration.ResamplingQuality.HIGH);


            /*
            Initialize and register source managers.
             */
            logger.info("Registering source managers and search providers.");
            Pulse.youtubeSearchProvider = new YoutubeSearchProvider();
            Pulse.youtubeAudioSourceManager = new YoutubeAudioSourceManager(true);
            String[] blocks = Pulse.identityProperties.get(IdentityProperties.IPBLOCKS).split(", ");
            if (blocks.length > 0) {
                List<IpBlock> ipBlocks = Stream.of(blocks)
                        .filter(String::isEmpty)
                        .filter(block -> Ipv4Block.isIpv4CidrBlock(block) || Ipv6Block.isIpv6CidrBlock(block))
                        .map(block -> (IpBlock) (Ipv4Block.isIpv4CidrBlock(block) ? new Ipv4Block(block) : new Ipv6Block(block)))
                        .toList();
                if (!ipBlocks.isEmpty()) {
                    Router router = Router.ROTATING_NANO_SWITCH;
                    logger.info("Implementing IP Rotating router %s with %d IP Blocks.".formatted(router.name(), ipBlocks.size()));
                    new YoutubeIpRotatorSetup(router.getRouter(ipBlocks))
                            .forSource(Pulse.youtubeAudioSourceManager)
                            .forConfiguration(Pulse.youtubeSearchProvider.getHttpConfiguration(), true)
                            .withRetryLimit(Integer.MAX_VALUE)
                            .setup();
                }
            }
            Pulse.soundCloudAudioSourceManager = SoundCloudAudioSourceManager.createDefault();
            Pulse.audioPlayerManager.registerSourceManager(Pulse.youtubeAudioSourceManager);
            Pulse.audioPlayerManager.registerSourceManager(Pulse.soundCloudAudioSourceManager);
            //TODO: More sources!

            /*
            Load up the shard manager.
             */
            logger.info("Building and initializing the shard manager.");
            Pulse.shardManager = DefaultShardManagerBuilder
                    .create(Pulse.identityProperties.get(IdentityProperties.TOKEN),
                            GatewayIntent.GUILD_MESSAGES,
                            GatewayIntent.GUILD_VOICE_STATES,
                            GatewayIntent.GUILD_MESSAGE_REACTIONS,
                            GatewayIntent.DIRECT_MESSAGE_REACTIONS)
                    .disableCache(EnumSet.allOf(CacheFlag.class))
                    .enableCache(CacheFlag.VOICE_STATE)
                    .setMemberCachePolicy(MemberCachePolicy.DEFAULT)
                    .setChunkingFilter(ChunkingFilter.NONE)
                    .setRawEventsEnabled(true)
                    .setEnableShutdownHook(false)
                    .setStatus(OnlineStatus.DO_NOT_DISTURB)
                    .setActivity(Activity.playing("Starting up..."))
                    .addEventListeners(new ReadyListener())
                    .addEventListeners(new GuildInitializer())
                    .addEventListeners(new GuildVoiceListener())
                    .addEventListeners(new InteractionListener())
                    .addEventListeners(new MessageDeletionRequestListener())
                    .build();

            /*
            Await all shards to finish starting up.
            */
            Pulse.readyNotifier.get();

            /*
            Backend systems are optional, as pulse can still operate with them offline.
            However, controller resumptions require ControllerFactory to be initialized.
             */
            logger.info("Loading system backends.");
            Try.to(() -> Class.forName(SettingsFactory.class.getName())).getUnchecked();
            Try.to(() -> Class.forName(ControllerFactory.class.getName())).getUnchecked();

            /*
            Set the state to running, and do final adjustments.
             */
            Pulse.setState(State.RUNNING);
        } catch (Throwable t) {
            t.printStackTrace();
            logger.severe("Unable to startup application.");
            Pulse.setState(State.SHUTDOWN);
        }

    }

}
