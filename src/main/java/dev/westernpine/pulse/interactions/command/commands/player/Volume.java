package dev.westernpine.pulse.interactions.command.commands.player;

import dev.westernpine.lib.interaction.component.command.SlashCommandComponentHandler;
import dev.westernpine.lib.util.Numbers;
import dev.westernpine.lib.util.jda.Embeds;
import dev.westernpine.lib.util.jda.Messenger;
import dev.westernpine.pulse.Pulse;
import dev.westernpine.pulse.authentication.Authenticator;
import dev.westernpine.pulse.controller.Controller;
import dev.westernpine.pulse.controller.ControllerFactory;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.LinkedList;
import java.util.Optional;

public class Volume implements SlashCommandComponentHandler {

    /**
     * @return How the command should be used.
     */
    @Override
    public String[] usages() {
        return new String[]{"volume [level]"};
    }

    /**
     * @return The command signifier string.
     */
    @Override
    public String command() {
        return "volume";
    }

    /**
     * @return The command description.
     */
    @Override
    public String description() {
        return "Check or set the volume.";
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
        options.add(new OptionData(OptionType.INTEGER, "level", "The level to set the volume to."));
        return options;
    }

    @Override
    public boolean handle(SlashCommandEvent event) {
        Controller controller = ControllerFactory.get(event.getGuild().getId(), true);
        Optional<AudioChannel> connectedChannel = controller.getConnectedChannel();

        if (event.getOption("level") == null) {

            if (connectedChannel.isEmpty()) {
                Messenger.replyTo(event, Embeds.error("Unable to check volume.", "I'm not connected."), 15);
                return false;
            }

            controller.setLastChannelId(event.getChannel().getId());
            Messenger.replyTo(event, Embeds.info(":loud_sound: Volume: `%d`".formatted(controller.getVolume()), "", Pulse.color(event.getGuild())), 15);
            return true;

        } else {

            if (!Authenticator.isDj(event.getMember(), controller)) {
                Messenger.replyTo(event, Embeds.error("Authentication failed.", "You must be a DJ to use this command."), 15);
                return false;
            }

            if (connectedChannel.isEmpty()) {
                Messenger.replyTo(event, Embeds.error("Unable to set volume.", "I'm not connected."), 15);
                return false;
            }

            if (!controller.getVoiceState(event.getMember()).inAudioChannel() || !connectedChannel.get().getId().equals(controller.getVoiceState(event.getMember()).getChannel().getId())) {
                Messenger.replyTo(event, Embeds.error("Unable to set volume.", "We must be in the same channel."), 15);
                return false;
            }

            controller.setLastChannelId(event.getChannel().getId());
            controller.setVolume(Long.valueOf(Numbers.setWithin(event.getOption("level").getAsLong(), 0, 100)).intValue());
            Messenger.replyTo(event, Embeds.success("Volume set to: `%d`".formatted(controller.getVolume()), ""), 15);
            return true;

        }
    }
}
