package dev.westernpine.pulse.interactions.command.commands.queue;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.westernpine.lib.audio.track.userdata.UserDataFactory;
import dev.westernpine.lib.audio.track.userdata.requester.Requester;
import dev.westernpine.lib.audio.track.userdata.requester.RequesterFactory;
import dev.westernpine.lib.interaction.component.command.SlashCommandComponentHandler;
import dev.westernpine.lib.util.jda.Embeds;
import dev.westernpine.lib.util.jda.Messenger;
import dev.westernpine.pulse.controller.Controller;
import dev.westernpine.pulse.controller.ControllerFactory;
import dev.westernpine.pulse.controller.settings.setting.Setting;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.LinkedList;
import java.util.Optional;

public class Next implements SlashCommandComponentHandler {

    /**
     * @return How the command should be used.
     */
    @Override
    public String[] usages() {
        return new String[]{"next"};
    }

    /**
     * @return The command signifier string.
     */
    @Override
    public String command() {
        return "next";
    }

    /**
     * @return The command description.
     */
    @Override
    public String description() {
        return "Skip to the next track.";
    }

    /**
     * @return The category of the command.
     */
    @Override
    public String category() {
        return "Queue";
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

        if (connectedChannel.isEmpty()) {
            Messenger.replyTo(event, Embeds.error("Unable to skip.", "I'm not connected."), 15);
            return false;
        }

        if (!controller.getVoiceState(event.getMember()).inAudioChannel() || !connectedChannel.get().getId().equals(controller.getVoiceState(event.getMember()).getChannel().getId())) {
            Messenger.replyTo(event, Embeds.error("Unable to skip.", "We must be in the same channel."), 15);
            return false;
        }

        AudioTrack audioTrack = controller.getPlayingTrack();
        if (audioTrack == null) {    //If current track is null
            //And future queue isn't empty, or controller is in repeating state and previous queue isnt empty... then automatically skip to next track.
            if (!controller.getQueue().isEmpty() || ((controller.getRepeating().isFalse() || controller.getSettings().get(Setting.TWENTRY_FOUR_SEVEN).toBoolean()) && !controller.getPreviousQueue().isEmpty())) {
                controller.setLastChannelId(event.getChannel().getId());
                controller.nextTrack();
                Messenger.replyTo(event, Embeds.success("Skipped to the next track.", ""), 15);
                return true;
            } else {    //Otherwise fail.
                Messenger.replyTo(event, Embeds.error("Unable to skip.", "I'm not playing anything."), 15);
                return false;
            }
        }

        Requester requester = UserDataFactory.from(audioTrack.getUserData()).requester();
        if (requester != null && requester.is(RequesterFactory.from(event.getUser()))) {
            controller.setLastChannelId(event.getChannel().getId());
            controller.nextTrack();
            Messenger.replyTo(event, Embeds.success("Skipped to the next track.", ""), 15);
            return true;
        }

        int current = controller.currentVotesToNext();
        int needed = controller.neededVotes();
        if (!controller.voteNext(event.getMember())) {
            controller.setLastChannelId(event.getChannel().getId());
            Messenger.replyTo(event, Embeds.success("Vote to skip to next track removed.", ""), 15);
            return true;
        }

        current += 1;

        if (current >= needed) {
            controller.setLastChannelId(event.getChannel().getId());
            controller.nextTrack();
            Messenger.replyTo(event, Embeds.success("Skipped to the next track.", "Enough votes accumulated."), 15);
            return true;
        }

        controller.setLastChannelId(event.getChannel().getId());
        Messenger.replyTo(event, Embeds.success("Voted to skip to next track.", ""), 15);
        return true;
    }
}
