package dev.westernpine.pulse.interactions.command.commands.player;

import dev.westernpine.bettertry.Try;
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
                && controller.getPlayingTrack() != null) {
            Messenger.replyTo(event, Embeds.error("Unable to join you.", "I'm currently playing for others."), 15);
            return false;
        }

        if (!Try.to(() -> controller.connect(event.getMember())).isSuccessful()) {
            Messenger.replyTo(event, Embeds.error("Unable to join you.", "I cannot join your channel."), 15);
            return false;
        }

        controller.setLastChannelId(event.getChannel().getId());
        Messenger.replyTo(event, Embeds.success("Connected!", ""), 15);
        return true;
    }
}
