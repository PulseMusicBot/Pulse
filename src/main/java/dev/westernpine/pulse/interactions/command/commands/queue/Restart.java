package dev.westernpine.pulse.interactions.command.commands.queue;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.westernpine.lib.interaction.component.command.SlashCommandComponentHandler;
import dev.westernpine.lib.object.TriState;
import dev.westernpine.lib.util.jda.Embeds;
import dev.westernpine.lib.util.jda.Messenger;
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

public class Restart implements SlashCommandComponentHandler {

    private static final Map<String, Boolean> choices = Map.of("track", true, "queue", false);
    private static final OptionData data = new OptionData(OptionType.STRING, "restart-type", "The restart type to apply.");

    static {
        choices.forEach((key, value) -> data.addChoice(key, value.toString()));
    }

    private static String getChoiceKey(boolean value) {
        return choices.entrySet().stream().filter(choice -> choice.getValue().equals(value)).map(Map.Entry::getKey).findAny().get();
    }

    /**
     * @return How the command should be used.
     */
    @Override
    public String[] usages() {
        return new String[]{"restart"};
    }

    /**
     * @return The command signifier string.
     */
    @Override
    public String command() {
        return "restart";
    }

    /**
     * @return The command description.
     */
    @Override
    public String description() {
        return "Restarts the playing track by default, or the queue if specified.";
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
        options.add(data);
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
            Messenger.replyTo(event, Embeds.error("Unable to restart.", "I'm not connected."), 15);
            return false;
        }

        if (!controller.getVoiceState(event.getMember()).inAudioChannel() || !connectedChannel.get().getId().equals(controller.getVoiceState(event.getMember()).getChannel().getId())) {
            Messenger.replyTo(event, Embeds.error("Unable to set volume.", "We must be in the same channel."), 15);
            return false;
        }

        AudioTrack audioTrack = controller.getPlayingTrack();

        OptionMapping option = event.getOption(data.getName());
        if(Optional.ofNullable(option).map(optionMapping -> Boolean.parseBoolean(option.getAsString())).orElse(false)) {
            if (audioTrack == null) {
                Messenger.replyTo(event, Embeds.error("Unable to restart.", "I'm not playing anything."), 15);
                return false;
            }

            if (!audioTrack.isSeekable()) {
                Messenger.replyTo(event, Embeds.error("Unable to restart.", "This track is not seekable."), 15);
                return false;
            }

            controller.setLastChannelId(event.getChannel().getId());
            controller.restartTrack();
            Messenger.replyTo(event, Embeds.success("Restarted track.", ""), 15);
        } else {
            controller.setLastChannelId(event.getChannel().getId());
            controller.restartQueue();
            Messenger.replyTo(event, Embeds.success("Restarted queue.", ""), 15);
        }
        return true;

    }
}
