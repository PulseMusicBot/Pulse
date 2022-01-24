package dev.westernpine.pulse.controller.handlers.player;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import dev.westernpine.pulse.Pulse;
import dev.westernpine.pulse.controller.Controller;
import dev.westernpine.pulse.events.system.player.*;

public class PlayerListener extends AudioEventAdapter {

    private Controller controller;

    public PlayerListener(Controller controller) {
        this.controller = controller;
    }

    public Controller getController() {
        return this.controller;
    }

    public void onPlayerPause(AudioPlayer player) {
        Pulse.eventManager.call(new PlayerPauseEvent(controller, this, player));
    }

    public void onPlayerResume(AudioPlayer player) {
        Pulse.eventManager.call(new PlayerResumeEvent(controller, this, player));
    }

    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        Pulse.eventManager.call(new TrackStartEvent(controller, this, player, track));
    }

    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        Pulse.eventManager.call(new TrackEndEvent(controller, this, player, track, endReason));
    }

    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        Pulse.eventManager.call(new TrackExceptionEvent(controller, this, player, track, exception));
    }

    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs, StackTraceElement[] stackTrace) {
        Pulse.eventManager.call(new TrackStuckEvent(controller, this, player, track, thresholdMs, stackTrace));
    }

}
