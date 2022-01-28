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
import dev.westernpine.pulse.controller.Controller;
import dev.westernpine.pulse.controller.ControllerFactory;
import dev.westernpine.pulse.controller.settings.setting.Setting;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class Play implements SlashCommandComponentHandler {

    public static final TriState asap = TriState.FALSE;

    /**
     * @return How the command should be used.
     */
    @Override
    public String[] usages() {
        return new String[]{"play", "play [query]"};
    }

    /**
     * @return The command signifier string.
     */
    @Override
    public String command() {
        return "play";
    }

    /**
     * @return The command description.
     */
    @Override
    public String description() {
        return "Resume the player, or enqueue a request for later.";
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
        options.add(new OptionData(OptionType.STRING, "query", "Something you would like to search for or enqueue."));
        return options;
    }

    @Override
    public boolean handle(SlashCommandEvent event) {
        Controller controller = ControllerFactory.get(event.getGuild().getId(), true);
        Optional<AudioChannel> connectedChannel = controller.getConnectedChannel();

        if (event.getOption("query") == null) {

            if (connectedChannel.isEmpty()) {
                event.replyEmbeds(Embeds.error("Unable to resume playing.", "I'm not connected.").build()).queue(interactionHook -> interactionHook.deleteOriginal().queueAfter(15, TimeUnit.SECONDS));
                return false;
            }

            if (!controller.getVoiceState(event.getMember()).inAudioChannel() || !controller.getVoiceState(event.getMember()).getChannel().getId().equals(connectedChannel.get().getId())) {
                event.replyEmbeds(Embeds.error("Unable to resume playing.", "You must be in my channel.").build()).queue(interactionHook -> interactionHook.deleteOriginal().queueAfter(15, TimeUnit.SECONDS));
                return false;
            }

            if (controller.getPlayingTrack() == null) {
                event.replyEmbeds(Embeds.error("Unable to resume playing.", "I'm not playing anything.").build()).queue(interactionHook -> interactionHook.deleteOriginal().queueAfter(15, TimeUnit.SECONDS));
                return false;
            }

            if (!controller.isPaused()) {
                event.replyEmbeds(Embeds.error("Unable to resume playing.", "I'm not paused.").build()).queue(interactionHook -> interactionHook.deleteOriginal().queueAfter(15, TimeUnit.SECONDS));
                return false;
            }

            controller.setLastChannelId(event.getChannel().getId());
            controller.setPaused(false);
            event.replyEmbeds(Embeds.success("Resumed playing.", "").build()).queue(interactionHook -> interactionHook.deleteOriginal().queueAfter(15, TimeUnit.SECONDS));

        } else {

            String query = event.getOption("query").getAsString();

            if (!controller.getVoiceState(event.getMember()).inAudioChannel()) {
                event.replyEmbeds(Embeds.error("Unable play request.", "You must be in a channel.").build()).queue(interactionHook -> interactionHook.deleteOriginal().queueAfter(15, TimeUnit.SECONDS));
                return false;
            }

            if (connectedChannel.isPresent()
                    && !connectedChannel.get().getId().equals(controller.getVoiceState(event.getMember()).getChannel().getId())
                    && !controller.getConnectedMembers().isEmpty()
                    && controller.getPlayingTrack() != null) {
                event.replyEmbeds(Embeds.error("Unable play request.", "I'm currently playing for others.").build()).queue(interactionHook -> interactionHook.deleteOriginal().queueAfter(15, TimeUnit.SECONDS));
                return false;
            }

            Platform platform = PlatformFactory.get(controller.getSettings().get(Setting.DEFAULT_PLATFORM).toString());

            SortedPlaylist playlist = Try.to(() -> AudioFactory.query(query).get())
                    .map(AudioFactory::toPlaylist)
                    .orElse(null);

            if (playlist == null)
                playlist = Try.to(() -> AudioFactory.query(platform.getPrefix() + query).get())
                        .map(AudioFactory::toPlaylist)
                        .orElse(null);

            if (playlist == null || playlist.isEmpty()) {
                event.replyEmbeds(Embeds.error("Unable play request.", "No playable results were found.").build()).queue(interactionHook -> interactionHook.deleteOriginal().queueAfter(15, TimeUnit.SECONDS));
                return false;
            }

            if (!Try.to(() -> controller.connect(event.getMember())).isSuccessful()) {
                event.replyEmbeds(Embeds.error("Unable play request.", "I cannot join your channel.").build()).queue(interactionHook -> interactionHook.deleteOriginal().queueAfter(15, TimeUnit.SECONDS));
                return false;
            }


            if (playlist.isSearchResult())
                playlist.setTracks(playlist.getTracks().get(0));


            Request request = RequestFactory.from(event.getOption("query").getAsString());
            Requester requester = RequesterFactory.from(event.getUser());

            playlist.applyUserData(UserDataFactory.from(request, requester, platform));

            controller.setLastChannelId(event.getChannel().getId());
            controller.enqueue(playlist, asap);
            event.replyEmbeds(Embeds.success(
                            playlist.size() == 1 ? "1 Track Enqueued!" : "%d Tracks Enqueued!".formatted(playlist.size()),
                            "")
                    .build()).queue(interactionHook -> interactionHook.deleteOriginal().queueAfter(15, TimeUnit.SECONDS));
        }
        return true;
    }
}
