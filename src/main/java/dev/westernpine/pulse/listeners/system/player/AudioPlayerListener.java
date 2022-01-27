package dev.westernpine.pulse.listeners.system.player;

import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import dev.westernpine.eventapi.objects.EventHandler;
import dev.westernpine.eventapi.objects.Listener;
import dev.westernpine.lib.audio.AudioFactory;
import dev.westernpine.lib.util.Formatter;
import dev.westernpine.lib.util.jda.Embeds;
import dev.westernpine.lib.util.jda.Messenger;
import dev.westernpine.pulse.Pulse;
import dev.westernpine.pulse.controller.Controller;
import dev.westernpine.pulse.controller.settings.setting.Setting;
import dev.westernpine.pulse.events.system.player.*;

public class AudioPlayerListener implements Listener {

    @EventHandler
    public void onFinishedPlaying(FinishedPlayingEvent event) {
        Controller controller = event.getController();
        controller.getLastChannel().ifPresent(channel ->
                Messenger.sendMessage(channel, Embeds.small(":heavy_check_mark: Finished playing all requests!", Pulse.color(controller.getGuild())), 15));
    }

    @EventHandler
    public void onPreviousQueueReachedEnd(PreviousQueueReachedEndEvent event) {
        Controller controller = event.getController();
        controller.getLastChannel().ifPresent(channel ->
                Messenger.sendMessage(channel, Embeds.small(":heavy_check_mark: Reached the start of the queue.", Pulse.color(controller.getGuild())), 15));
    }

    @EventHandler
    public void onPlayerPause(PlayerPauseEvent event) {
        //Do nothing.
    }

    @EventHandler
    public void onPlayerResume(PlayerResumeEvent event) {
        //Do nothing.
    }

    @EventHandler
    public void onTrackStart(TrackStartEvent event) {
        Controller controller = event.getController();
        int lastTrack = controller.getLastTrack();

        boolean trackUpdates = controller.getSettings().get(Setting.TRACK_UPDATES).toBoolean();
        boolean lastTrackNull = lastTrack == 0;
        boolean hashIsntLast = AudioFactory.hashAudioObject(event.getAudioTrack()) != lastTrack;
        boolean queueRepeating = controller.getRepeating().isFalse() || controller.getSettings().get(Setting.TWENTRY_FOUR_SEVEN).toBoolean();
        boolean totalQueueIsntEmpty = controller.getTotalQueueSize() != 0;

        if (trackUpdates && (
                lastTrackNull
                        || hashIsntLast
                        || (queueRepeating && totalQueueIsntEmpty)))
            controller.getLastChannel().ifPresent(channel ->
                    Messenger.sendMessage(channel, Embeds.play("Now playing...", Formatter.formatInfo(event.getAudioTrack().getInfo()), event.getAudioTrack().getDuration(), Pulse.color(controller.getGuild())), 15));
    }

    @EventHandler
    public void onTrackEnd(TrackEndEvent event) {
        if (!event.getController().getAudioManager().isConnected())
            return;

        if (event.getEndReason().equals(AudioTrackEndReason.LOAD_FAILED))
            return; // Handled in #onTrackException

        if (event.getEndReason().mayStartNext)
            event.getController().finished(event.getAudioTrack());
    }

    @EventHandler
    public void onTrackException(TrackExceptionEvent event) {
        if (!Pulse.getState().isActive())
            return;

        Controller controller = event.getController();

        controller.getLastChannel().ifPresent(channel ->
                Messenger.sendMessage(channel, Embeds.error("Media load error:", event.getException().getMessage()), 15));
        controller.nextTrack(true);
    }

    @EventHandler
    public void onTrackStuck(TrackStuckEvent event) {
        Controller controller = event.getController();

        controller.getLastChannel().ifPresent(channel ->
                Messenger.sendMessage(channel, Embeds.error("Media load error:", "Unable to retrieve audio data."), 15));
        controller.nextTrack(true);
    }

}
