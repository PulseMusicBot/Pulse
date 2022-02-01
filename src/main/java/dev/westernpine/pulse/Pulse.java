package dev.westernpine.pulse;

import com.google.gson.Gson;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeSearchProvider;
import dev.westernpine.bettertry.Try;
import dev.westernpine.eventapi.EventManager;
import dev.westernpine.lib.object.Scheduler;
import dev.westernpine.lib.object.State;
import dev.westernpine.pulse.events.system.StateChangeEvent;
import dev.westernpine.pulse.listeners.console.ConsoleListener;
import dev.westernpine.pulse.listeners.system.player.AudioPlayerListener;
import dev.westernpine.pulse.listeners.system.state.InitializeListener;
import dev.westernpine.pulse.listeners.system.state.RunningListener;
import dev.westernpine.pulse.listeners.system.state.ShutdownListener;
import dev.westernpine.pulse.listeners.system.state.StartupListener;
import dev.westernpine.pulse.properties.IdentityProperties;
import dev.westernpine.pulse.properties.SqlProperties;
import dev.westernpine.pulse.properties.SystemProperties;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.sharding.ShardManager;

import javax.annotation.Nullable;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class Pulse {

    /*
    TODO:
     Up Next:
     - Fix SortedPlaylist(AudioPlaylist) constructor not tracking proper selected track index. (PlayFirst check)
     - Add nullable and nonnullable annotations
     - Add documentation!
     - detail logs - requests, joining, leaving
     - Commands (With detailed logs)
     - More sources.
     - Management/Premium status (Reuse old)
     */

    public static final SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

    public static final SimpleDateFormat simpleDateFormatter = new SimpleDateFormat("MM dd yyyy HH-mm-ss");

    public static final Gson gson = new Gson();

    public static final String version = Optional.ofNullable(Pulse.class.getPackage().getImplementationVersion()).filter(String::isEmpty).orElse("Unknown");

    public static final Scheduler scheduler = new Scheduler();

    public static final EventManager eventManager;

    public static final BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
    public static SystemProperties systemProperties;
    public static IdentityProperties identityProperties;
    public static SqlProperties loggingSqlProperties;
    public static CompletableFuture<Boolean> readyNotifier;
    public static Consumer<ReadyEvent> readyHandler;
    public static AudioPlayerManager audioPlayerManager;
    public static YoutubeSearchProvider youtubeSearchProvider;
    public static YoutubeAudioSourceManager youtubeAudioSourceManager;
    public static SoundCloudAudioSourceManager soundCloudAudioSourceManager;
    public static ShardManager shardManager;
    public static LinkedList<Runnable> shutdownHooks = new LinkedList<>();
    private static State state = State.OFFLINE;

    static {
        eventManager = new EventManager();

        eventManager.registerListeners(new InitializeListener());
        eventManager.registerListeners(new StartupListener());
        eventManager.registerListeners(new RunningListener());
        eventManager.registerListeners(new ShutdownListener());
        eventManager.registerListeners(new ConsoleListener());
        eventManager.registerListeners(new AudioPlayerListener());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!state.isActive()) return;
            state = State.SHUTDOWN;
            System.out.println(State.SHUTDOWN.getName() + " >> Initiating system shutdown.");
            Pulse.shutdownHooks.forEach(Runnable::run);
            System.out.println(State.SHUTDOWN.getName() + " >> System shutdown completed. Goodbye!");
            state = State.OFFLINE;
            Try.to(() -> Thread.sleep(1000L)); //Allow JDA to finish everything it needs.
        }));

        setState(State.INITIALIZATION);
    }

    public static State getState() {
        return state;
    }

    public static void setState(State state) {
        if (Pulse.state.isActive() && !state.isActive()) state = State.SHUTDOWN;
        State old = Pulse.state;
        Pulse.state = state;
        eventManager.call(new StateChangeEvent(old, state));
    }

    public static void main(String[] args) {
        setState(State.STARTUP);
    }

    public static Color color(@Nullable Guild guild) {
        return Objects.isNull(guild) ? color() : guild.getSelfMember().getColor();
    }

    public static Color color() {
        return Color.decode(identityProperties.get(IdentityProperties.COLOR));
    }

}
