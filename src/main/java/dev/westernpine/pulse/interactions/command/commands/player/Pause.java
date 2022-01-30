package dev.westernpine.pulse.interactions.command.commands.player;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
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

public class Pause implements SlashCommandComponentHandler {

    public static final TriState asap = TriState.FALSE;

    /**
     * @return How the command should be used.
     */
    @Override
    public String[] usages() {
        return new String[]{"pause"};
    }

    /**
     * @return The command signifier string.
     */
    @Override
    public String command() {
        return "pause";
    }

    /**
     * @return The command description.
     */
    @Override
    public String description() {
        return "Pauses the player.";
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

        if(connectedChannel.isEmpty()) {
            event.replyEmbeds(Embeds.error("Unable to pause.", "I'm not connected.").build()).queue(interactionHook -> interactionHook.deleteOriginal().queueAfter(15, TimeUnit.SECONDS));
            return false;
        }

        if (!controller.getVoiceState(event.getMember()).inAudioChannel()) {
            event.replyEmbeds(Embeds.error("Unable to pause.", "You must be in a channel.").build()).queue(interactionHook -> interactionHook.deleteOriginal().queueAfter(15, TimeUnit.SECONDS));
            return false;
        }

        //playing checks
        AudioTrack audioTrack = controller.getPlayingTrack();

        if(audioTrack == null) {
            event.replyEmbeds(Embeds.error("Unable to pause.", "I'm not playing anything.").build()).queue(interactionHook -> interactionHook.deleteOriginal().queueAfter(15, TimeUnit.SECONDS));
            return false;
        }

        if(!audioTrack.isSeekable() || audioTrack.getInfo().isStream) {
            event.replyEmbeds(Embeds.error("Unable to pause.", "This track is not seekable.").build()).queue(interactionHook -> interactionHook.deleteOriginal().queueAfter(15, TimeUnit.SECONDS));
            return false;
        }

        if (connectedChannel.isPresent()
                && !connectedChannel.get().getId().equals(controller.getVoiceState(event.getMember()).getChannel().getId())
                && !controller.getConnectedMembers().isEmpty()
                && controller.getPlayingTrack() != null) {
            event.replyEmbeds(Embeds.error("Unable to pause.", "I'm currently playing for others.").build()).queue(interactionHook -> interactionHook.deleteOriginal().queueAfter(15, TimeUnit.SECONDS));
            return false;
        }

        if(controller.isPaused()) {
            event.replyEmbeds(Embeds.error("Unable to pause.", "I'm already paused.").build()).queue(interactionHook -> interactionHook.deleteOriginal().queueAfter(15, TimeUnit.SECONDS));
            return false;
        }

        controller.setLastChannelId(event.getChannel().getId());
        controller.setPaused(true);
        event.replyEmbeds(Embeds.success("Paused.", "").build()).queue(interactionHook -> interactionHook.deleteOriginal().queueAfter(15, TimeUnit.SECONDS));
        return true;
    }
}
