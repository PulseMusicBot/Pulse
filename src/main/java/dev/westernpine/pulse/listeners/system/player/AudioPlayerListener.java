package dev.westernpine.pulse.listeners.system.player;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import dev.westernpine.eventapi.objects.EventHandler;
import dev.westernpine.eventapi.objects.Listener;
import dev.westernpine.lib.audio.AudioFactory;
import dev.westernpine.lib.object.TriState;
import dev.westernpine.lib.util.Formatter;
import dev.westernpine.lib.util.ImageCrawler;
import dev.westernpine.lib.util.jda.Embeds;
import dev.westernpine.lib.util.jda.Messenger;
import dev.westernpine.pulse.Pulse;
import dev.westernpine.pulse.controller.Controller;
import dev.westernpine.pulse.controller.EndCase;
import dev.westernpine.pulse.controller.settings.setting.Setting;
import dev.westernpine.pulse.events.system.player.*;
import net.dv8tion.jda.api.EmbedBuilder;

public class AudioPlayerListener implements Listener {

    @EventHandler
    public void onPlayerDestroyed(PlayerDestroyedEvent event) {
        Controller controller = event.getController();
        EndCase endCase = event.getEndCase();
        if (event.wasConnected() && !endCase.getReason().isEmpty())
            controller.getLastChannel().ifPresent(channel -> Messenger.sendMessage(channel, Embeds.info(":outbox_tray: Disconnected.", endCase.getReason(), Pulse.color(controller.getGuild())), 15));
    }

    @EventHandler
    public void onFinishedPlaying(FinishedPlayingEvent event) {
        Controller controller = event.getController();
        controller.getLastChannel().ifPresent(channel -> Messenger.sendMessage(channel, Embeds.success("Finished playing!", "The end of the queue was reached."), 15));
    }

    @EventHandler
    public void onPreviousQueueReachedEnd(PreviousQueueReachedEndEvent event) {
        Controller controller = event.getController();
        controller.getLastChannel().ifPresent(channel -> Messenger.sendMessage(channel, Embeds.success("Finished playing!", "The start of the queue was reached."), 15));
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
        AudioTrack audioTrack = event.getAudioTrack();
        int lastTrack = controller.getLastTrack();

        boolean trackUpdates = controller.getSettings().get(Setting.TRACK_UPDATES).toBoolean();
        boolean lastTrackNull = lastTrack == 0;
        boolean hashIsntLast = AudioFactory.hashAudioObject(audioTrack) != lastTrack;
        boolean queueRepeating = controller.getRepeating().isFalse() || controller.getSettings().get(Setting.TWENTRY_FOUR_SEVEN).toBoolean();
        boolean totalQueueIsntEmpty = controller.getTotalQueueSize() != 0;

        if (trackUpdates && (
                lastTrackNull
                        || hashIsntLast
                        || (queueRepeating && totalQueueIsntEmpty)))
            controller.getLastChannel().ifPresent(channel -> {
                EmbedBuilder embedBuilder = Embeds.play("Now playing...", Formatter.formatInfo(event.getAudioTrack().getInfo()), event.getAudioTrack().getDuration(), Pulse.color(controller.getGuild()));
                TriState imageSize = controller.getSettings().get(Setting.IMAGE_SIZE).toTriState();
                if (!imageSize.isFalse()) {
                    String imageUrl = ImageCrawler.findURL(audioTrack.getInfo().uri);
                    embedBuilder = imageSize.isNone() ? embedBuilder.setThumbnail(imageUrl) : embedBuilder.setImage(imageUrl);
                }
                Messenger.sendMessage(channel, embedBuilder, 15);
            });
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
