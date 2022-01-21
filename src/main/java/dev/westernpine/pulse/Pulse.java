package dev.westernpine.pulse;

import com.google.gson.Gson;
import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeSearchProvider;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.lava.extensions.youtuberotator.YoutubeIpRotatorSetup;
import com.sedmelluq.lava.extensions.youtuberotator.tools.ip.IpBlock;
import com.sedmelluq.lava.extensions.youtuberotator.tools.ip.Ipv4Block;
import com.sedmelluq.lava.extensions.youtuberotator.tools.ip.Ipv6Block;
import dev.westernpine.bettertry.Try;
import dev.westernpine.eventapi.EventManager;
import dev.westernpine.lib.object.LoggingPrintStream;
import dev.westernpine.lib.object.Scheduler;
import dev.westernpine.pulse.audio.AudioFactory;
import dev.westernpine.pulse.audio.track.userdata.platform.PlatformFactory;
import dev.westernpine.pulse.audio.track.userdata.request.RequestFactory;
import dev.westernpine.pulse.events.console.ConsoleEvent;
import dev.westernpine.pulse.events.system.SystemStartedEvent;
import dev.westernpine.pulse.listeners.console.ConsoleListener;
import dev.westernpine.pulse.listeners.system.SystemStartedListener;
import dev.westernpine.pulse.listeners.system.jda.InteractionListener;
import dev.westernpine.pulse.listeners.system.jda.GuildInitializer;
import dev.westernpine.pulse.listeners.system.jda.MessageDeletionRequestListener;
import dev.westernpine.pulse.listeners.system.jda.ReadyListener;
import dev.westernpine.pulse.properties.IdentityProperties;
import dev.westernpine.pulse.properties.SystemProperties;
import dev.westernpine.pulse.sources.Router;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.annotation.Nullable;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class Pulse {

    /*
    TODO: Goals for today:
     1. Put messages built inside code. - Done
     - CHANGE OF PLANS!
     - - We need to convert everything from serializable to factories. Since each class has a different way of initializing, serializing, and deserializing, its better to just make a factory for each.
     2. Start work on the audio package.
     3. Build Audio Track Wrapper.
     4. Build Playlist Wrapper.
     5. Possibly start on TrackFactories?
     6. Possibly continue work on the controller, and SQL Integration for configuration?
     */

    public static final Gson gson = new Gson();

    public static final String version = Optional.ofNullable(Pulse.class.getPackage().getImplementationVersion()).filter(String::isEmpty).orElse("Unknown");

    public static final Scheduler scheduler = new Scheduler();

    public static final EventManager eventManager = new EventManager();

    public static final BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

    public static SystemProperties systemProperties;

    public static IdentityProperties identityProperties;

    public static CompletableFuture<Boolean> readyNotifier;

    public static Consumer<ReadyEvent> readyHandler;

    public static AudioPlayerManager audioPlayerManager;

    public static YoutubeSearchProvider youtubeSearchProvider;

    public static YoutubeAudioSourceManager youtubeAudioSourceManager;

    public static SoundCloudAudioSourceManager soundCloudAudioSourceManager;

    public static ShardManager shardManager;

    /*
    Near the bottom and out of order in case of some un-set values.
    */
    public static Runnable shutdownHook = () -> {
        System.out.println("System Shutdown >> Terminating scheduler.");
        scheduler.shutdownNow();
        System.out.println("System Shutdown >> Closing console inputs.");
        Try.of(input::close).onFailure(Throwable::printStackTrace);
        if (Objects.nonNull(shardManager)) {
            System.out.println("System Shutdown >> Shutting down shard-manager.");
            shardManager.shutdown();
        }
        System.out.println("System Shutdown >> System shutdown completed. Goodbye!");
        Try.of(() -> Thread.sleep(1000L));
    };

    public static void main(String[] args) {
        try {

            /*
            Set up an automatic logging system to print to console and to print to a log file.
             */
            File file = new File(new File(Pulse.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParentFile().toString() + File.separator + "logs");
            if (!file.exists() && !file.mkdirs()) throw new IOException("Unable to create logs folder.");
            file = new File(file.getPath(), new SimpleDateFormat("MM dd yyyy HH-mm-ss").format(new Date()) + ".log");
            LoggingPrintStream.initialize(file);

            System.out.println("System Startup >> Starting Pulse (v)" + version);

            /*
            Add a shutdown hook for whenever the environment shuts down.
             */
            System.out.println("System Startup >> Adding shutdown hook listener.");
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("System Shutdown >> Initiating system shutdown.");
                Pulse.shutdownHook.run();
            }));

            /*
            Register events before system startup.
             */
            System.out.println("System Startup >> Registering system event listeners.");
            eventManager.registerListeners(new ConsoleListener());
            eventManager.registerListeners(new SystemStartedListener());

            /*
            Set up console listening system via events.
             */
            System.out.println("System Startup >> Initializing console listener.");
            scheduler.runAsync(() -> Scheduler.loop(() -> true, () -> Try.of(() -> Scheduler.loop(() -> !Try.of(input::ready).getUnchecked(), () -> Try.of(() -> Thread.sleep(100)).map(nv -> false).getUnchecked())).map(nv -> Try.of(input::readLine).map(ConsoleEvent::new).onSuccess(event -> Try.of(() -> eventManager.call(event)).onFailure(Throwable::printStackTrace).onFailure(throwable -> System.out.println("Exception caught in command handler."))).map(so -> false).orElse(true)).orElse(true)));

            /*
            Load up system properties.
             */
            System.out.println("System Startup >> Loading system properties.");
            systemProperties = new SystemProperties();

            /*
             Load up identity properties.
             */
            System.out.println("System Startup >> Loading " + systemProperties.get(SystemProperties.IDENTITY) + " identity properties.");
            identityProperties = new IdentityProperties(systemProperties.get(SystemProperties.IDENTITY));

            /*
			Initialize the ready notifier format completion.
            */
            readyNotifier = new CompletableFuture<>();

            /*
			Initialize the ready handler to handle JDA readies.
            */
            readyHandler = event -> {
                JDA jda = event.getJDA();
                int shardId = jda.getShardInfo().getShardId();
                System.out.println("System Startup >> Ready event fired for shard ID: " + shardId);
                jda.getPresence().setPresence(OnlineStatus.IDLE, Activity.watching("Shard initialized! Waiting for other shards..."));
                if (!readyNotifier.isDone() && shardManager.getShardsQueued() <= 0)
                    readyNotifier.complete(true);


            };

            /*
            Initialize the audio player manager.
             */
            System.out.println("System Startup >> Building and initializing the audio player manager.");
            audioPlayerManager = new DefaultAudioPlayerManager();
            audioPlayerManager.setFrameBufferDuration(3000);
            audioPlayerManager.getConfiguration().setFilterHotSwapEnabled(true);
            audioPlayerManager.getConfiguration().setResamplingQuality(AudioConfiguration.ResamplingQuality.HIGH);


            /*
            Initialize and register source managers.
             */
            System.out.println("System Startup >> Registering source managers and search providers.");
            youtubeSearchProvider = new YoutubeSearchProvider();
            youtubeAudioSourceManager = new YoutubeAudioSourceManager(true);
            String[] blocks = identityProperties.get(IdentityProperties.IPBLOCKS).split(", ");
            if(blocks.length > 0) {
                List<IpBlock> ipBlocks = Stream.of(blocks)
                        .filter(String::isEmpty)
                        .filter(block -> Ipv4Block.isIpv4CidrBlock(block) || Ipv6Block.isIpv6CidrBlock(block))
                        .map(block -> (IpBlock) (Ipv4Block.isIpv4CidrBlock(block) ? new Ipv4Block(block) : new Ipv6Block(block)))
                        .toList();
                if(!ipBlocks.isEmpty()) {
                    Router router = Router.ROTATING_NANO_SWITCH;
                    System.out.println("System Startup >> Implementing IP Rotating router %s with %d IP Blocks.".formatted(router.name(), ipBlocks.size()));
                    new YoutubeIpRotatorSetup(router.getRouter(ipBlocks))
                            .forSource(youtubeAudioSourceManager)
                            .forConfiguration(youtubeSearchProvider.getHttpConfiguration(), true)
                            .withRetryLimit(Integer.MAX_VALUE)
                            .setup();
                }
            }
            soundCloudAudioSourceManager = SoundCloudAudioSourceManager.createDefault();
            audioPlayerManager.registerSourceManager(youtubeAudioSourceManager);
            audioPlayerManager.registerSourceManager(soundCloudAudioSourceManager);
            //TODO: More sources!

            AudioPlaylist result = Try.of(() -> AudioFactory.query("ytsearch:Grant - high tide").get()).map(item -> (AudioPlaylist)item).orElse(null);
            if(result == null) {
                System.out.println("Result was null!");
            } else {
                String json = AudioFactory.toJson(result);
                System.out.println(json);
                System.out.println(AudioFactory.toJson(AudioFactory.fromPlaylistJson(json).getTracks().get(0)));
            }

            /*
            Load up the shard manager.
             */
            System.out.println("System Startup >> Building and initializing the shard manager.");
            shardManager = DefaultShardManagerBuilder
                    .create(identityProperties.get(IdentityProperties.TOKEN),
                            GatewayIntent.GUILD_MESSAGES,
                            GatewayIntent.GUILD_VOICE_STATES,
                            GatewayIntent.GUILD_MESSAGE_REACTIONS,
                            GatewayIntent.DIRECT_MESSAGE_REACTIONS)
                    .disableCache(EnumSet.allOf(CacheFlag.class))
                    .enableCache(CacheFlag.VOICE_STATE)
                    .setMemberCachePolicy(MemberCachePolicy.DEFAULT)
                    .setChunkingFilter(ChunkingFilter.NONE)
                    .setRawEventsEnabled(true)
                    .setStatus(OnlineStatus.DO_NOT_DISTURB)
                    .setActivity(Activity.playing("Starting up..."))
                    .addEventListeners(new ReadyListener())
                    .addEventListeners(new GuildInitializer())
                    .addEventListeners(new InteractionListener())
                    .addEventListeners(new MessageDeletionRequestListener())
                    .build();

            /*
            Await all shards to finish starting up.
            */
            readyNotifier.get();

            /*
            Call the System Started event.
             */
            eventManager.call(new SystemStartedEvent());

        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(0);
        }
    }

    public static Color color(@Nullable Guild guild) {
        return Objects.isNull(guild) ? color() : guild.getSelfMember().getColor();
    }

    public static Color color() {
        return Color.decode(identityProperties.get(IdentityProperties.COLOR));
    }

}
