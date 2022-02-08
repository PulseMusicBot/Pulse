package dev.westernpine.pulse.interactions.command.commands.queue;

import dev.westernpine.lib.interaction.component.command.SlashCommandComponentHandler;
import dev.westernpine.lib.object.TriState;
import dev.westernpine.lib.util.jda.Embeds;
import dev.westernpine.lib.util.jda.Messenger;
import dev.westernpine.pulse.Pulse;
import dev.westernpine.pulse.authentication.Authenticator;
import dev.westernpine.pulse.controller.Controller;
import dev.westernpine.pulse.controller.ControllerFactory;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

public class Repeat implements SlashCommandComponentHandler {

    private static final Map<String, TriState> choices = Map.of("off", TriState.NONE, "track", TriState.TRUE, "queue", TriState.FALSE);

    private static String getChoiceKey(TriState triState) {
        return choices.entrySet().stream().filter(choice -> choice.getValue().equals(triState)).map(Map.Entry::getKey).findAny().get();
    }

    /**
     * @return How the command should be used.
     */
    @Override
    public String[] usages() {
        return new String[]{"repeat [off/track/queue]"};
    }

    /**
     * @return The command signifier string.
     */
    @Override
    public String command() {
        return "repeat";
    }

    /**
     * @return The command description.
     */
    @Override
    public String description() {
        return "Set the repeating state of the player.";
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
        OptionData repeatType = new OptionData(OptionType.STRING, "repeat-type", "The repeat type to set the player to.");
        choices.forEach((key, value) -> repeatType.addChoice(key, value.toString()));
        options.add(repeatType);
        return options;
    }

    @Override
    public boolean handle(SlashCommandEvent event) {
        Controller controller = ControllerFactory.get(event.getGuild().getId(), true);
        Optional<AudioChannel> connectedChannel = controller.getConnectedChannel();

        OptionMapping repeatOption = event.getOption("repeat-type");

        if (repeatOption == null) {

            if (connectedChannel.isEmpty()) {
                Messenger.replyTo(event, Embeds.error("Unable to check repeat.", "I'm not connected."), 15);
                return false;
            }

            TriState repeatType = controller.getRepeating();
            Messenger.replyTo(event, Embeds.info(":repeat: Repeating: `%s`".formatted(getChoiceKey(repeatType)), "", Pulse.color(event.getGuild())), 15);
            return true;
        } else {

            if(!Authenticator.isDj(event.getMember(), controller)) {
                Messenger.replyTo(event, Embeds.error("Authentication failed.", "You must be a DJ to use this command."), 15);
                return false;
            }

            if (connectedChannel.isEmpty()) {
                Messenger.replyTo(event, Embeds.error("Unable to set repeat.", "I'm not connected."), 15);
                return false;
            }

            if (!controller.getVoiceState(event.getMember()).inAudioChannel() || !connectedChannel.get().getId().equals(controller.getVoiceState(event.getMember()).getChannel().getId())) {
                Messenger.replyTo(event, Embeds.error("Unable to set repeat.", "We must be in the same channel."), 15);
                return false;
            }

            TriState repeatType = TriState.valueOf(repeatOption.getAsString());
            String choice = getChoiceKey(repeatType);
            controller.setLastChannelId(event.getChannel().getId());
            controller.setRepeating(repeatType);
            Messenger.replyTo(event, Embeds.info(":repeat: Repeating set to: `%s`".formatted(choice), "", Pulse.color(event.getGuild())), 15);
            return true;
        }
    }
}
