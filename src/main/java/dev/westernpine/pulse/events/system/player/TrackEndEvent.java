package dev.westernpine.pulse.events.system.player;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import dev.westernpine.eventapi.objects.Cancellable;
import dev.westernpine.eventapi.objects.Event;
import dev.westernpine.pulse.controller.Controller;
import dev.westernpine.pulse.controller.handlers.player.PlayerListener;

public class TrackEndEvent extends Event implements Cancellable {

    private final Controller controller;
    private final PlayerListener playerListener;
    private final AudioPlayer audioPlayer;
    private final AudioTrack audioTrack;
    private final AudioTrackEndReason endReason;
    private boolean cancelled;

    public TrackEndEvent(Controller controller, PlayerListener playerListener, AudioPlayer audioPlayer, AudioTrack audioTrack, AudioTrackEndReason endReason) {
        this.controller = controller;
        this.playerListener = playerListener;
        this.audioPlayer = audioPlayer;
        this.audioTrack = audioTrack;
        this.endReason = endReason;
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

    public AudioTrackEndReason getEndReason() {
        return endReason;
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
