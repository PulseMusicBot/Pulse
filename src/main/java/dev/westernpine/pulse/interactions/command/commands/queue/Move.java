package dev.westernpine.pulse.interactions.command.commands.queue;

import dev.westernpine.lib.interaction.component.command.SlashCommandComponentHandler;
import dev.westernpine.lib.util.Numbers;
import dev.westernpine.lib.util.Strings;
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
import java.util.regex.Matcher;

public class Move implements SlashCommandComponentHandler {

    /**
     * @return How the command should be used.
     */
    @Override
    public String[] usages() {
        return new String[]{"move <selection> <position>"};
    }

    /**
     * @return The command signifier string.
     */
    @Override
    public String command() {
        return "move";
    }

    /**
     * @return The command description.
     */
    @Override
    public String description() {
        return "Move a selection to the specified position in the queue.";
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
        options.add(new OptionData(OptionType.STRING, "selection", "The selected track or range of tracks to move.", true));
        options.add(new OptionData(OptionType.INTEGER, "position", "The position to move the selected tracks to.", true));
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
            Messenger.replyTo(event, Embeds.error("Unable to move.", "I'm not connected."), 15);
            return false;
        }

        if (!controller.getVoiceState(event.getMember()).inAudioChannel() || !connectedChannel.get().getId().equals(controller.getVoiceState(event.getMember()).getChannel().getId())) {
            Messenger.replyTo(event, Embeds.error("Unable to move.", "We must be in the same channel."), 15);
            return false;
        }

        if (controller.getQueue().isEmpty()) {
            Messenger.replyTo(event, Embeds.error("Unable to move.", "Insufficient queue size."), 15);
            return false;
        }

        String selection = event.getOption("selection").getAsString();

        int size = controller.getQueue().size();
        int start = -1;
        int items = 1;

        Matcher range = Strings.getRangeMatcher(selection);
        if (Strings.isInteger(selection)) {
            start = Integer.parseInt(selection);
        } else if (range.matches()) {
            int first = Numbers.setWithin(Integer.parseInt(range.group(1)), 1, size);
            int end = Numbers.setWithin(Integer.parseInt(range.group(2)), 1, size);
            start = Math.min(first, end);
            items = Math.abs(start - end);
        } else {
            Messenger.replyTo(event, Embeds.error("Unable to move.", "Invalid selection. (Single item, or range only [1-5])"), 15);
            return false;
        }

        int position = Numbers.setWithin(Long.valueOf(event.getOption("position").getAsLong()).intValue(), 1, size);
        controller.setLastChannelId(event.getChannel().getId());
        controller.move(start - 1, items, position - 1);    // -1 Because indexes are 0-based.
        Messenger.replyTo(event, Embeds.success("Moved `%s` %s to `%s`.".formatted(items, items == 1 ? "item" : "items", position), ""), 15);
        return true;
    }
}
