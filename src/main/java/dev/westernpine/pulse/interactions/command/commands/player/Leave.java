package dev.westernpine.pulse.interactions.command.commands.player;

import dev.westernpine.bettertry.Try;
import dev.westernpine.lib.interaction.component.command.SlashCommandComponentHandler;
import dev.westernpine.lib.util.jda.Embeds;
import dev.westernpine.pulse.controller.Controller;
import dev.westernpine.pulse.controller.ControllerFactory;
import dev.westernpine.pulse.controller.EndCase;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class Leave implements SlashCommandComponentHandler {

    /**
     * @return How the command should be used.
     */
    @Override
    public String[] usages() {
        return new String[]{"leave"};
    }

    /**
     * @return The command signifier string.
     */
    @Override
    public String command() {
        return "leave";
    }

    /**
     * @return The command description.
     */
    @Override
    public String description() {
        return "Request the bot to leave your channel.";
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

        if(connectedChannel.isEmpty()) {
            event.replyEmbeds(Embeds.error("Unable to leave.", "I'm not connected.").build()).queue(interactionHook -> interactionHook.deleteOriginal().queueAfter(15, TimeUnit.SECONDS));
            return false;
        }

        if (!controller.getVoiceState(event.getMember()).inAudioChannel()) {
            event.replyEmbeds(Embeds.error("Unable to leave.", "You must be in a channel.").build()).queue(interactionHook -> interactionHook.deleteOriginal().queueAfter(15, TimeUnit.SECONDS));
            return false;
        }

        if (connectedChannel.isPresent()
                && !connectedChannel.get().getId().equals(controller.getVoiceState(event.getMember()).getChannel().getId())
                && !controller.getConnectedMembers().isEmpty()
                && controller.getPlayingTrack() != null) {
            event.replyEmbeds(Embeds.error("Unable to leave.", "I'm currently playing for others.").build()).queue(interactionHook -> interactionHook.deleteOriginal().queueAfter(15, TimeUnit.SECONDS));
            return false;
        }

        controller.setLastChannelId(event.getChannel().getId());
        controller.destroy(EndCase.BY_COMMAND);
        event.replyEmbeds(Embeds.success("Disconnected.", "").build()).queue(interactionHook -> interactionHook.deleteOriginal().queueAfter(15, TimeUnit.SECONDS));
        return true;
    }
}
