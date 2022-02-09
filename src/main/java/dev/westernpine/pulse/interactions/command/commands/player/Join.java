package dev.westernpine.pulse.interactions.command.commands.player;

import dev.westernpine.bettertry.Try;
import dev.westernpine.lib.audio.AudioFactory;
import dev.westernpine.lib.audio.playlist.SortedPlaylist;
import dev.westernpine.lib.audio.track.userdata.UserDataFactory;
import dev.westernpine.lib.audio.track.userdata.platform.Platform;
import dev.westernpine.lib.audio.track.userdata.platform.PlatformFactory;
import dev.westernpine.lib.audio.track.userdata.request.Request;
import dev.westernpine.lib.audio.track.userdata.request.RequestFactory;
import dev.westernpine.lib.audio.track.userdata.requester.Requester;
import dev.westernpine.lib.audio.track.userdata.requester.RequesterFactory;
import dev.westernpine.lib.interaction.component.command.SlashCommandComponentHandler;
import dev.westernpine.lib.object.TriState;
import dev.westernpine.lib.util.jda.Embeds;
import dev.westernpine.lib.util.jda.Messenger;
import dev.westernpine.pulse.authentication.Authenticator;
import dev.westernpine.pulse.controller.Controller;
import dev.westernpine.pulse.controller.ControllerFactory;
import dev.westernpine.pulse.controller.settings.setting.Setting;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.LinkedList;
import java.util.Optional;

import static dev.westernpine.pulse.logging.Logger.logger;

public class Join implements SlashCommandComponentHandler {

    /**
     * @return How the command should be used.
     */
    @Override
    public String[] usages() {
        return new String[]{"join"};
    }

    /**
     * @return The command signifier string.
     */
    @Override
    public String command() {
        return "join";
    }

    /**
     * @return The command description.
     */
    @Override
    public String description() {
        return "Request the bot to join your channel.";
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
        return options;
    }

    @Override
    public boolean handle(SlashCommandEvent event) {
        Controller controller = ControllerFactory.get(event.getGuild().getId(), true);
        Optional<AudioChannel> connectedChannel = controller.getConnectedChannel();

        if (!controller.getVoiceState(event.getMember()).inAudioChannel()) {
            Messenger.replyTo(event, Embeds.error("Unable to join you.", "You must be in a channel."), 15);
            return false;
        }

        if (connectedChannel.isPresent()
                && !connectedChannel.get().getId().equals(controller.getVoiceState(event.getMember()).getChannel().getId())
                && !controller.getConnectedMembers().isEmpty()
                && controller.getPlayingTrack() != null
                && !Authenticator.isDj(event.getMember(), controller)) {
            Messenger.replyTo(event, Embeds.error("Unable to join you.", "I'm currently playing for others."), 15);
            return false;
        }

        if (!Try.to(() -> controller.connect(event.getMember())).isSuccessful()) {
            Messenger.replyTo(event, Embeds.error("Unable to join you.", "I cannot join your channel."), 15);
            return false;
        }

        controller.setLastChannelId(event.getChannel().getId());

        String query = controller.getSettings().get(Setting.JOIN_MUSIC).toString();

        if (!query.isEmpty()) {
            Platform platform = PlatformFactory.get(controller.getSettings().get(Setting.DEFAULT_PLATFORM).toString());

            SortedPlaylist playlist = Try.to(() -> AudioFactory.query(query).get())
                    .map(AudioFactory::toPlaylist)
                    .orElse(null);

            if (playlist == null)
                playlist = Try.to(() -> AudioFactory.query(platform.getPrefix() + query).get())
                        .map(AudioFactory::toPlaylist)
                        .orElse(null);

            if (playlist == null || playlist.isEmpty()) {
                Messenger.replyTo(event, Embeds.error("Unable play default request.", "No playable results were found."), 15);
                return false;
            }

            if (playlist.isSearchResult())
                playlist.setTracks(playlist.getTracks().get(0));

            Request request = RequestFactory.from(query);
            Requester requester = RequesterFactory.from(event.getUser());

            logger.info("%s requested as join music: %s".formatted(requester.getId(), request.getRequest()));

            playlist.applyUserData(UserDataFactory.from(request, requester, platform));
            controller.enqueue(playlist, TriState.FALSE);
        }

        Messenger.replyTo(event, Embeds.success("Connected!", ""), 15);
        return true;
    }
}
