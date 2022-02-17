package dev.westernpine.pulse.interactions.command.commands.queue;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.westernpine.lib.interaction.component.command.SlashCommandComponentHandler;
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

public class ForceNext implements SlashCommandComponentHandler {

    /**
     * @return How the command should be used.
     */
    @Override
    public String[] usages() {
        return new String[]{"forcenext"};
    }

    /**
     * @return The command signifier string.
     */
    @Override
    public String command() {
        return "forcenext";
    }

    /**
     * @return The command description.
     */
    @Override
    public String description() {
        return "Force-skip to the next track.";
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

        if (!Authenticator.isDj(event.getMember(), controller)) {
            Messenger.replyTo(event, Embeds.error("Authentication failed.", "You must be a DJ to use this command."), 15);
            return false;
        }

        if (connectedChannel.isEmpty()) {
            Messenger.replyTo(event, Embeds.error("Unable to force-skip.", "I'm not connected."), 15);
            return false;
        }

        if (!controller.getVoiceState(event.getMember()).inAudioChannel() || !connectedChannel.get().getId().equals(controller.getVoiceState(event.getMember()).getChannel().getId())) {
            Messenger.replyTo(event, Embeds.error("Unable to force-skip.", "We must be in the same channel."), 15);
            return false;
        }

        AudioTrack audioTrack = controller.getPlayingTrack();
        if (audioTrack == null) {    //If current track is null
            //And future queue isn't empty, or controller is in repeating state and previous queue isnt empty... then automatically skip to next track.
            if ((controller.getPreviousQueue().isEmpty() && !controller.getQueue().isEmpty())
                    || ((controller.getRepeating().isFalse() || controller.getSettings().get(Setting.TWENTY_FOUR_SEVEN).toBoolean()) && !controller.getPreviousQueue().isEmpty())) {
                controller.setLastChannelId(event.getChannel().getId());
                controller.nextTrack();
                Messenger.replyTo(event, Embeds.success("Skipped to the next track.", ""), 15);
                return true;
            } else {    //Otherwise fail.
                Messenger.replyTo(event, Embeds.error("Unable to skip.", "I'm not playing anything, and theres nothing to skip to."), 15);
                return false;
            }
        }

        controller.setLastChannelId(event.getChannel().getId());
        controller.nextTrack();
        Messenger.replyTo(event, Embeds.success("Force-skipped to the next track.", ""), 15);
        return true;
    }
}
