package dev.westernpine.pulse.interactions.command.commands.player;

import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import dev.westernpine.bettertry.Try;
import dev.westernpine.lib.interaction.component.command.SlashCommandComponentHandler;
import dev.westernpine.lib.object.TriState;
import dev.westernpine.lib.player.audio.AudioFactory;
import dev.westernpine.lib.player.audio.playlist.Playlist;
import dev.westernpine.lib.player.audio.playlist.PlaylistFactory;
import dev.westernpine.lib.player.audio.track.userdata.UserDataFactory;
import dev.westernpine.lib.player.audio.track.userdata.platform.Platform;
import dev.westernpine.lib.player.audio.track.userdata.platform.PlatformManager;
import dev.westernpine.lib.player.audio.track.userdata.request.Request;
import dev.westernpine.lib.player.audio.track.userdata.request.RequestFactory;
import dev.westernpine.lib.player.audio.track.userdata.requester.Requester;
import dev.westernpine.lib.player.audio.track.userdata.requester.RequesterFactory;
import dev.westernpine.lib.util.jda.Embeds;
import dev.westernpine.lib.util.jda.Messenger;
import dev.westernpine.pulse.Pulse;
import dev.westernpine.pulse.authentication.Authenticator;
import dev.westernpine.pulse.controller.Controller;
import dev.westernpine.pulse.controller.ControllerFactory;
import dev.westernpine.pulse.controller.settings.setting.Setting;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.LinkedList;
import java.util.Optional;

import static dev.westernpine.pulse.logging.Logger.logger;

public class PlayFirst implements SlashCommandComponentHandler {

    public static final TriState asap = TriState.NONE;

    /**
     * @return How the command should be used.
     */
    @Override
    public String[] usages() {
        return new String[]{"playfirst [query]"};
    }

    /**
     * @return The command signifier string.
     */
    @Override
    public String command() {
        return "playfirst";
    }

    /**
     * @return The command description.
     */
    @Override
    public String description() {
        return "Insert a request at the first position in the queue.";
    }

    /**
     * @return The category of the command.
     */
    @Override
    public String category() {
        return "Player";
    }

    @Override
    public LinkedList<OptionData> options() {
        LinkedList<OptionData> options = new LinkedList<>();
        options.add(new OptionData(OptionType.STRING, "query", "Something you would like to search for or enqueue.", true));
        return options;
    }

    @Override
    public boolean handle(SlashCommandEvent event) {
        Controller controller = ControllerFactory.get(event.getGuild().getId(), true);
        Optional<AudioChannel> connectedChannel = controller.getConnectedChannel();

        String query = event.getOption("query").getAsString();

        if (!controller.getVoiceState(event.getMember()).inAudioChannel()) {
            Messenger.replyTo(event, Embeds.error("Unable play request.", "You must be in a channel."), 15);
            return false;
        }

        if (connectedChannel.isPresent()
                && !connectedChannel.get().getId().equals(controller.getVoiceState(event.getMember()).getChannel().getId())
                && !controller.getConnectedMembers().isEmpty()
                && controller.getPlayingTrack() != null
                && !Authenticator.isDj(event.getMember(), controller)) {
            Messenger.replyTo(event, Embeds.error("Unable play request.", "I'm currently playing for others."), 15);
            return false;
        }

        Platform platform = PlatformManager.getFromSource(controller.getSettings().get(Setting.DEFAULT_PLATFORM).toString());

        Playlist playlist = Try.to(() -> AudioFactory.query(Pulse.audioPlayerManager, query).get())
                .flatMap(result -> result != null && result != AudioReference.NO_TRACK
                        ? Try.successful(result)
                        : Try.failure(null))
                .orElseTry(() -> AudioFactory.query(Pulse.audioPlayerManager, platform.getSearchPrefix() + query).get())
                .flatMap(result -> result != null && result != AudioReference.NO_TRACK
                        ? Try.successful(result)
                        : Try.failure(null))
                .map(AudioFactory::toPlaylist)
                .map(PlaylistFactory::from)
                .orElse(null);

        if (playlist == null || playlist.isEmpty()) {
            Messenger.replyTo(event, Embeds.error("Unable play request.", "No playable results were found."), 15);
            return false;
        }

        if (!Try.to(() -> controller.connect(event.getMember())).isSuccessful()) {
            Messenger.replyTo(event, Embeds.error("Unable play request.", "I cannot join your channel."), 15);
            return false;
        }

        if (playlist.isSearchResult())
            playlist.setTracks(playlist.getTracks().get(0));

        Request request = RequestFactory.from(event.getOption("query").getAsString());
        Requester requester = RequesterFactory.from(event.getUser());

        logger.info("%s requested: %s".formatted(requester.getId(), request.getRequest()));

        playlist.applyUserData(UserDataFactory.from(request, requester, platform));

        controller.setLastChannelId(event.getChannel().getId());
        int enqueued = controller.enqueue(playlist, asap);
        Messenger.replyTo(event, Embeds.success(enqueued == 1 ? "1 Track Enqueued!" : "%d Tracks Enqueued!".formatted(enqueued), ""), 15);
        return true;
    }
}
