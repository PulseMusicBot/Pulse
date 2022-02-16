package dev.westernpine.pulse.listeners.system.state;

import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.getyarn.GetyarnAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeSearchProvider;
import com.sedmelluq.lava.extensions.youtuberotator.YoutubeIpRotatorSetup;
import com.sedmelluq.lava.extensions.youtuberotator.tools.ip.IpBlock;
import com.sedmelluq.lava.extensions.youtuberotator.tools.ip.Ipv4Block;
import com.sedmelluq.lava.extensions.youtuberotator.tools.ip.Ipv6Block;
import com.sedmelluq.lavaplayer.extensions.thirdpartysources.applemusic.AppleMusicAudioSourceManager;
import com.sedmelluq.lavaplayer.extensions.thirdpartysources.deezer.DeezerAudioSourceManager;
import com.sedmelluq.lavaplayer.extensions.thirdpartysources.napster.NapsterAudioSourceManager;
import com.sedmelluq.lavaplayer.extensions.thirdpartysources.spotify.SpotifyAudioSourceManager;
import com.sedmelluq.lavaplayer.extensions.thirdpartysources.tidal.TidalAudioSourceManager;
import com.sedmelluq.lavaplayer.extensions.thirdpartysources.yamusic.YandexMusicAudioSourceManager;
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
import dev.westernpine.pulse.player.sources.wrapper.Wrapper;
import dev.westernpine.pulse.properties.IdentityProperties;
import dev.westernpine.pulse.player.Router;
import dev.westernpine.pulse.player.sources.iHeart.iHeartAudioSourceManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static dev.westernpine.pulse.logging.Logger.logger;

public class StartupListener implements Listener {

    public static final Collection<Object> eventListeners = List.of(
            new ReadyListener(),
            new GuildInitializer(),
            new GuildVoiceListener(),
            new InteractionListener(),
            new MessageDeletionRequestListener());

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
                if (Objects.nonNull(Pulse.shardManager)) {
                    System.out.println(State.SHUTDOWN.getName() + " >> Setting shutdown presence.");
                    Pulse.shardManager.setPresence(OnlineStatus.DO_NOT_DISTURB, Activity.playing("shutting down..."));
                    System.out.println(State.SHUTDOWN.getName() + " >> Shutting down controllers.");
                    ControllerFactory.getControllers().values().forEach(controller -> controller.destroy(EndCase.BOT_RESTART));
                    System.out.println(State.SHUTDOWN.getName() + " >> Removing shard manager listeners.");
                    Pulse.shardManager.removeEventListener(eventListeners.toArray(Object[]::new));
                    System.out.println(State.SHUTDOWN.getName() + " >> Awaiting all required tasks to be completed.");
                    Scheduler.awaitTasksCompletion(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
                    System.out.println(State.SHUTDOWN.getName() + " >> Shutting down shard manager.");
                    Pulse.shardManager.shutdown();
                }
                System.out.println(State.SHUTDOWN.getName() + " >> Terminating scheduler.");
                Pulse.scheduler.shutdownNow();
            });

            /*
            Set up console listening system via events.
             */
            logger.info("Initializing console listener.");
            Pulse.scheduler.runAsync(() -> Scheduler.loop(() -> true, () -> Try.to(Pulse.input::readLine).map(ConsoleEvent::new).onSuccess(Pulse.eventManager::call).onFailure(Throwable::printStackTrace).map(consoleEvent -> false).orElse(false)));

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
            Pulse.soundCloudAudioSourceManager = SoundCloudAudioSourceManager.createDefault();
            Pulse.youtubeSearchProvider = new YoutubeSearchProvider();
            Pulse.youtubeAudioSourceManager = new YoutubeAudioSourceManager(true);
            String[] blocks = Pulse.identityProperties.get(IdentityProperties.IPBLOCKS).split(", ");
            if (blocks.length > 0) {
                List<IpBlock> ipBlocks = Arrays.stream(blocks)
                        .filter(string -> !string.isEmpty())
                        .filter(block -> Ipv4Block.isIpv4CidrBlock(block) || Ipv6Block.isIpv6CidrBlock(block))
                        .map(block -> (IpBlock) (Ipv4Block.isIpv4CidrBlock(block) ? new Ipv4Block(block) : new Ipv6Block(block)))
                        .toList();
                if (!ipBlocks.isEmpty()) {
                    Router router = Router.ROTATING_NANO_SWITCH;
                    logger.info("Implementing IP Rotating router %s with %d IP Block(s).".formatted(router.name(), ipBlocks.size()));
                    new YoutubeIpRotatorSetup(router.getRouter(ipBlocks))
                            .forSource(Pulse.youtubeAudioSourceManager)
                            .forConfiguration(Pulse.youtubeSearchProvider.getHttpConfiguration(), true)
                            .withRetryLimit(Integer.MAX_VALUE)
                            .setup();
                }
            }
            Pulse.audioPlayerManager.registerSourceManager(Pulse.youtubeAudioSourceManager);
            Pulse.audioPlayerManager.registerSourceManager(Pulse.soundCloudAudioSourceManager);
            Pulse.audioPlayerManager.registerSourceManager(new VimeoAudioSourceManager());
            Pulse.audioPlayerManager.registerSourceManager(new TwitchStreamAudioSourceManager());
            Pulse.audioPlayerManager.registerSourceManager(new BandcampAudioSourceManager());
            Pulse.audioPlayerManager.registerSourceManager(new GetyarnAudioSourceManager());
            Pulse.audioPlayerManager.registerSourceManager(new iHeartAudioSourceManager(Pulse.audioPlayerManager));
            Pulse.audioPlayerManager.registerSourceManager(new AppleMusicAudioSourceManager(Pulse.audioPlayerManager));
            Pulse.audioPlayerManager.registerSourceManager(new DeezerAudioSourceManager(Pulse.audioPlayerManager));
            Pulse.audioPlayerManager.registerSourceManager(new NapsterAudioSourceManager(Pulse.audioPlayerManager));
            Pulse.audioPlayerManager.registerSourceManager(new SpotifyAudioSourceManager(Pulse.audioPlayerManager));
            Pulse.audioPlayerManager.registerSourceManager(new TidalAudioSourceManager(Pulse.audioPlayerManager));
            Pulse.audioPlayerManager.registerSourceManager(new YandexMusicAudioSourceManager(Pulse.audioPlayerManager));
            Pulse.audioPlayerManager.registerSourceManager(new HttpAudioSourceManager());

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
                    .addEventListeners(eventListeners)
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
