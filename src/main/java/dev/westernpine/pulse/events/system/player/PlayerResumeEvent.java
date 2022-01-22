package dev.westernpine.pulse.events.system.player;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import dev.westernpine.eventapi.objects.Cancellable;
import dev.westernpine.eventapi.objects.Event;
import dev.westernpine.pulse.controller.Controller;
import dev.westernpine.pulse.controller.handlers.player.PlayerListener;

public class PlayerResumeEvent extends Event implements Cancellable {

    private final Controller controller;
    private final PlayerListener playerListener;
    private final AudioPlayer audioPlayer;
    private boolean cancelled;

    public PlayerResumeEvent(Controller controller, PlayerListener playerListener, AudioPlayer audioPlayer) {
        this.controller = controller;
        this.playerListener = playerListener;
        this.audioPlayer = audioPlayer;
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

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
