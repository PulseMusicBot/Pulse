package dev.westernpine.pulse.interactions.command.commands.queue;

import dev.westernpine.lib.interaction.component.command.SlashCommandComponentHandler;
import dev.westernpine.lib.util.jda.Embeds;
import dev.westernpine.lib.util.jda.Messenger;
import dev.westernpine.pulse.controller.Controller;
import dev.westernpine.pulse.controller.ControllerFactory;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.LinkedList;
import java.util.Optional;

public class Flip implements SlashCommandComponentHandler {

    /**
     * @return How the command should be used.
     */
    @Override
    public String[] usages() {
        return new String[]{"flip"};
    }

    /**
     * @return The command signifier string.
     */
    @Override
    public String command() {
        return "flip";
    }

    /**
     * @return The command description.
     */
    @Override
    public String description() {
        return "Flip the future queue.";
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
            Messenger.replyTo(event, Embeds.error("Unable to flip.", "I'm not connected."), 15);
            return false;
        }

        if (!controller.getVoiceState(event.getMember()).inAudioChannel() || !connectedChannel.get().getId().equals(controller.getVoiceState(event.getMember()).getChannel().getId())) {
            Messenger.replyTo(event, Embeds.error("Unable to flip.", "We must be in the same channel."), 15);
            return false;
        }

        if (controller.getQueue().isEmpty()) {
            Messenger.replyTo(event, Embeds.error("Unable to flip.", "The future queue is empty."), 15);
            return false;
        }

        controller.setLastChannelId(event.getChannel().getId());
        controller.flip();
        Messenger.replyTo(event, Embeds.success("Flipped the future queue.", ""), 15);
        return true;
    }
}
