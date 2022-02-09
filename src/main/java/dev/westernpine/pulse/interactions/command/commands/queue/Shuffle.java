package dev.westernpine.pulse.interactions.command.commands.queue;

import dev.westernpine.lib.interaction.component.command.SlashCommandComponentHandler;
import dev.westernpine.lib.util.jda.Embeds;
import dev.westernpine.lib.util.jda.Messenger;
import dev.westernpine.pulse.Pulse;
import dev.westernpine.pulse.authentication.Authenticator;
import dev.westernpine.pulse.controller.Controller;
import dev.westernpine.pulse.controller.ControllerFactory;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.LinkedList;
import java.util.Optional;

public class Shuffle implements SlashCommandComponentHandler {

    /**
     * @return How the command should be used.
     */
    @Override
    public String[] usages() {
        return new String[]{"shuffle"};
    }

    /**
     * @return The command signifier string.
     */
    @Override
    public String command() {
        return "shuffle";
    }

    /**
     * @return The command description.
     */
    @Override
    public String description() {
        return "Shuffle the future queue.";
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
            Messenger.replyTo(event, Embeds.error("Unable to shuffle.", "I'm not connected."), 15);
            return false;
        }

        if (!controller.getVoiceState(event.getMember()).inAudioChannel() || !connectedChannel.get().getId().equals(controller.getVoiceState(event.getMember()).getChannel().getId())) {
            Messenger.replyTo(event, Embeds.error("Unable to shuffle.", "We must be in the same channel."), 15);
            return false;
        }

        if (controller.getQueue().isEmpty()) {
            Messenger.replyTo(event, Embeds.error("Unable to shuffle.", "Insufficient queue size."), 15);
            return false;
        }
        controller.setLastChannelId(event.getChannel().getId());
        controller.shuffle();
        Messenger.replyTo(event, Embeds.info(":twisted_rightwards_arrows: Shuffling queue.", "", Pulse.color(event.getGuild())), 15);
        return true;
    }
}
