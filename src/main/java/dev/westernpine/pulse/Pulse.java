package dev.westernpine.pulse;

import com.google.gson.Gson;
import dev.westernpine.bettertry.Try;
import dev.westernpine.eventapi.EventManager;
import dev.westernpine.lib.object.LoggingPrintStream;
import dev.westernpine.lib.object.Scheduler;
import dev.westernpine.pulse.events.console.ConsoleEvent;
import dev.westernpine.pulse.events.system.SystemStartedEvent;
import dev.westernpine.pulse.listeners.console.ConsoleListener;
import dev.westernpine.pulse.listeners.system.SystemStartedListener;
import dev.westernpine.pulse.listeners.system.jda.ReadyListener;
import dev.westernpine.pulse.properties.IdentityProperties;
import dev.westernpine.pulse.properties.SystemProperties;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumSet;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class Pulse {

    public static final Gson gson = new Gson();

    public static final String version = Optional.ofNullable(Pulse.class.getPackage().getImplementationVersion()).filter(String::isEmpty).orElse("Unknown");

    public static final Scheduler scheduler = new Scheduler();

    public static final EventManager eventManager = new EventManager();

    public static final BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

    public static SystemProperties systemProperties;

    public static IdentityProperties identityProperties;

    public static CompletableFuture<Boolean> readyNotifier;

    public static Consumer<ReadyEvent> readyHandler;

    public static ShardManager shardManager;

    /*
    Near the bottom and out of order in case of some un-set values.
    */
    public static Runnable shutdownHook = () -> {
        System.out.println("Initiating system shutdown.");
        scheduler.shutdownNow();
        Try.of(input::close).onFailure(Throwable::printStackTrace);
        System.out.println("System shutdown completed. Goodbye!");
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

            System.out.println("Startup >> Starting Pulse (v)" + version);

            /*
            Add a shutdown hook for whenever the environment shuts down.
             */
            System.out.println("Startup >> Adding shutdown hook listener.");
            Runtime.getRuntime().addShutdownHook(new Thread(() -> Pulse.shutdownHook.run()));

            /*
            Register events before system startup.
             */
            System.out.println("Startup >> Registering system event listeners.");
            eventManager.registerListeners(new ConsoleListener());
            eventManager.registerListeners(new SystemStartedListener());

            /*
            Set up console listening system via events.
             */
            System.out.println("Startup >> Initializing console listener.");
            scheduler.runAsync(() -> Scheduler.loop(() -> true, () -> Try.of(() -> Scheduler.loop(() -> !Try.of(input::ready).getUnchecked(), () -> Try.of(() -> Thread.sleep(100)).map(nv -> false).getUnchecked())).map(nv -> Try.of(input::readLine).map(ConsoleEvent::new).onSuccess(event -> Try.of(() -> eventManager.call(event)).onFailure(Throwable::printStackTrace).onFailure(throwable -> System.out.println("Exception caught in command handler."))).map(so -> false).orElse(true)).orElse(true)));

            /*
            Load up system properties.
             */
            System.out.println("Startup >> Loading system properties.");
            systemProperties = new SystemProperties();

            /*
             Load up identity properties.
             */
            System.out.println("Startup >> Loading " + systemProperties.get(SystemProperties.IDENTITY) + " identity properties.");
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
                System.out.println("Startup >> Ready event fired for shard ID: " + shardId);
                jda.getPresence().setPresence(OnlineStatus.IDLE, Activity.watching("Shard initialized! Waiting for other shards..."));
                if (!readyNotifier.isDone() && shardManager.getShardsQueued() <= 0)
                    readyNotifier.complete(true);
            };

            /*
            Load up the shard manager.
             */
            System.out.println("Startup >> Building and initializing the shard manager.");
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
                    .addEventListeners(new ReadyListener())
                    .setStatus(OnlineStatus.DO_NOT_DISTURB)
                    .setActivity(Activity.playing("Starting up..."))
                    .build();

            /*
            Await all shards to finish starting up.
            */
            readyNotifier.get();

            /*
            Fire the system startup completed event.
             */
            eventManager.call(new SystemStartedEvent());

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

}
