package dev.westernpine.pulse.interactions.command.commands.player;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.westernpine.lib.interaction.component.command.SlashCommandComponentHandler;
import dev.westernpine.lib.util.jda.Embeds;
import dev.westernpine.lib.util.jda.Messenger;
import dev.westernpine.pulse.authentication.Authenticator;
import dev.westernpine.pulse.controller.Controller;
import dev.westernpine.pulse.controller.ControllerFactory;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.LinkedList;
import java.util.Optional;

public class Pause implements SlashCommandComponentHandler {

    /**
     * @return How the command should be used.
     */
    @Override
    public String[] usages() {
        return new String[]{"pause"};
    }

    /**
     * @return The command signifier string.
     */
    @Override
    public String command() {
        return "pause";
    }

    /**
     * @return The command description.
     */
    @Override
    public String description() {
        return "Pauses the player.";
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

        if(!Authenticator.isDj(event.getMember(), controller)) {
            Messenger.replyTo(event, Embeds.error("Authentication failed.", "You must be a DJ to use this command."), 15);
            return false;
        }

        if (connectedChannel.isEmpty()) {
            Messenger.replyTo(event, Embeds.error("Unable to pause.", "I'm not connected."), 15);
            return false;
        }

        if (!controller.getVoiceState(event.getMember()).inAudioChannel() || !connectedChannel.get().getId().equals(controller.getVoiceState(event.getMember()).getChannel().getId())) {
            Messenger.replyTo(event, Embeds.error("Unable to set volume.", "We must be in the same channel."), 15);
            return false;
        }

        //playing checks
        AudioTrack audioTrack = controller.getPlayingTrack();

        if (audioTrack == null) {
            Messenger.replyTo(event, Embeds.error("Unable to pause.", "I'm not playing anything."), 15);
            return false;
        }

        if (!audioTrack.isSeekable() || audioTrack.getInfo().isStream) {
            Messenger.replyTo(event, Embeds.error("Unable to pause.", "This track is not seekable."), 15);
            return false;
        }

        if (controller.isPaused()) {
            Messenger.replyTo(event, Embeds.error("Unable to pause.", "I'm already paused."), 15);
            return false;
        }

        controller.setLastChannelId(event.getChannel().getId());
        controller.setPaused(true);
        Messenger.replyTo(event, Embeds.success("Paused.", ""), 15);
        return true;
    }
}
