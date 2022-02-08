package dev.westernpine.pulse.interactions.command.commands.queue;

import dev.westernpine.lib.interaction.component.command.SlashCommandComponentHandler;
import dev.westernpine.lib.util.Numbers;
import dev.westernpine.lib.util.jda.Embeds;
import dev.westernpine.lib.util.jda.Messenger;
import dev.westernpine.pulse.authentication.Authenticator;
import dev.westernpine.pulse.controller.Controller;
import dev.westernpine.pulse.controller.ControllerFactory;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.LinkedList;
import java.util.Optional;

public class SkipTo implements SlashCommandComponentHandler {

    /**
     * @return How the command should be used.
     */
    @Override
    public String[] usages() {
        return new String[]{"skipto <position>"};
    }

    /**
     * @return The command signifier string.
     */
    @Override
    public String command() {
        return "skipto";
    }

    /**
     * @return The command description.
     */
    @Override
    public String description() {
        return "Skip to a position in the queue.";
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
        options.add(new OptionData(OptionType.INTEGER, "position", "The position in the queue to skip to.", true));
        return options;
    }

    @Override
    public boolean handle(SlashCommandEvent event) {
        Controller controller = ControllerFactory.get(event.getGuild().getId(), true);
        Optional<AudioChannel> connectedChannel = controller.getConnectedChannel();

        if(!Authenticator.isDj(event.getMember(), controller)) {
            Messenger.replyTo(event, Embeds.error("Authentication failed.", "You must be a DJ to use this command."), 15);
            return false;
        }

        if (connectedChannel.isEmpty()) {
            Messenger.replyTo(event, Embeds.error("Unable to skip-to item.", "I'm not connected."), 15);
            return false;
        }

        if (!controller.getVoiceState(event.getMember()).inAudioChannel() || !connectedChannel.get().getId().equals(controller.getVoiceState(event.getMember()).getChannel().getId())) {
            Messenger.replyTo(event, Embeds.error("Unable to skip-to item.", "We must be in the same channel."), 15);
            return false;
        }

        if (controller.getQueue().isEmpty()) {
            Messenger.replyTo(event, Embeds.error("Unable to skip-to item.", "Insufficient queue size."), 15);
            return false;
        }

        int position = Numbers.setWithin(Long.valueOf(event.getOption("position").getAsLong()).intValue(), 1, controller.getQueue().size());
        controller.setLastChannelId(event.getChannel().getId());
        controller.skipTo(position);
        Messenger.replyTo(event, Embeds.success("Skipped to item `%d` in the queue.".formatted(position), ""), 15);
        return true;
    }
}
