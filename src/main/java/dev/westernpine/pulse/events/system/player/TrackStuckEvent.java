package dev.westernpine.pulse.events.system.player;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.westernpine.eventapi.objects.Cancellable;
import dev.westernpine.eventapi.objects.Event;
import dev.westernpine.pulse.controller.Controller;
import dev.westernpine.pulse.controller.handlers.player.PlayerListener;

public class TrackStuckEvent extends Event implements Cancellable {

    private final Controller controller;
    private final PlayerListener playerListener;
    private final AudioPlayer audioPlayer;
    private final AudioTrack audioTrack;
    private final long thresholdMs;
    private final StackTraceElement[] stackTrace;
    private boolean cancelled;

    public TrackStuckEvent(Controller controller, PlayerListener playerListener, AudioPlayer audioPlayer, AudioTrack audioTrack, long thresholdMs, StackTraceElement[] stackTrace) {
        this.controller = controller;
        this.playerListener = playerListener;
        this.audioPlayer = audioPlayer;
        this.audioTrack = audioTrack;
        this.thresholdMs = thresholdMs;
        this.stackTrace = stackTrace;
    }

    public Controller getController() {
        return controller;
    }

    public PlayerListener getPlayerListener() {
        return playerListener;
    }

    public AudioPlayer getAudioPlayer() {
        return audioPlayer;
    }

    public AudioTrack getAudioTrack() {
        return audioTrack;
    }

    public long getThresholdsMs() {
        return thresholdMs;
    }

    public StackTraceElement[] getStackTrace() {
        return stackTrace;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
