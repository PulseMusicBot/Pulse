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

public class Remove implements SlashCommandComponentHandler {

    /**
     * @return How the command should be used.
     */
    @Override
    public String[] usages() {
        return new String[]{"remove <selection>"};
    }

    /**
     * @return The command signifier string.
     */
    @Override
    public String command() {
        return "remove";
    }

    /**
     * @return The command description.
     */
    @Override
    public String description() {
        return "Remove a selection in the queue.";
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
        options.add(new OptionData(OptionType.STRING, "selection", "The selected track or range of tracks to remove.", true));
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
            Messenger.replyTo(event, Embeds.error("Unable to remove.", "I'm not connected."), 15);
            return false;
        }

        if (!controller.getVoiceState(event.getMember()).inAudioChannel() || !connectedChannel.get().getId().equals(controller.getVoiceState(event.getMember()).getChannel().getId())) {
            Messenger.replyTo(event, Embeds.error("Unable to remove.", "We must be in the same channel."), 15);
            return false;
        }

        if (controller.getQueue().isEmpty()) {
            Messenger.replyTo(event, Embeds.error("Unable to remove.", "Insufficient queue size."), 15);
            return false;
        }

        String selection = event.getOption("selection").getAsString();

        int size = controller.getQueue().size();
        int start = -1;
        int items = 0;

        Matcher range = Strings.getRangeMatcher(selection);
        if (Strings.isInteger(selection)) {
            start = Integer.parseInt(selection);
        } else if (range.matches()) {
            int first = Numbers.setWithin(Integer.parseInt(range.group(1)), 1, size);
            int end = Numbers.setWithin(Integer.parseInt(range.group(2)), 1, size);
            start = Math.min(first, end);
            items = Math.abs(first - end);
        } else {
            Messenger.replyTo(event, Embeds.error("Unable to remove.", "Invalid selection. (Single item, or range only [1-5])"), 15);
            return false;
        }
        controller.setLastChannelId(event.getChannel().getId());
        Messenger.replyTo(event, Embeds.success("Removed `%s` %s.".formatted(items, items == 1 ? "item" : "items"), ""), 15);
        return true;
    }
}
