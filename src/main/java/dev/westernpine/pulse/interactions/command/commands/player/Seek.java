package dev.westernpine.pulse.interactions.command.commands.player;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.westernpine.bettertry.Try;
import dev.westernpine.lib.interaction.component.command.SlashCommandComponentHandler;
import dev.westernpine.lib.object.Timestamp;
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
import java.util.concurrent.TimeUnit;

public class Seek implements SlashCommandComponentHandler {

    /**
     * @return How the command should be used.
     */
    @Override
    public String[] usages() {
        return new String[]{"seek [time]"};
    }

    /**
     * @return The command signifier string.
     */
    @Override
    public String command() {
        return "seek";
    }

    /**
     * @return The command description.
     */
    @Override
    public String description() {
        return "Seek to a specified time in the track.";
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
        options.add(new OptionData(OptionType.STRING, "time", "The time to seek to in the track in either seconds or timestamp format.", true));
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
            Messenger.replyTo(event, Embeds.error("Unable to seek.", "I'm not connected."), 15);
            return false;
        }

        if (!controller.getVoiceState(event.getMember()).inAudioChannel() || !connectedChannel.get().getId().equals(controller.getVoiceState(event.getMember()).getChannel().getId())) {
            Messenger.replyTo(event, Embeds.error("Unable to seek.", "We must be in the same channel."), 15);
            return false;
        }

        //playing checks
        AudioTrack audioTrack = controller.getPlayingTrack();

        if (audioTrack == null) {
            Messenger.replyTo(event, Embeds.error("Unable to seek.", "I'm not playing anything."), 15);
            return false;
        }

        if (!audioTrack.isSeekable() || audioTrack.getInfo().isStream) {
            Messenger.replyTo(event, Embeds.error("Unable to seek.", "This track is not seekable."), 15);
            return false;
        }

        String specified = Try.to(() -> event.getOption("time").getAsString()).orElse("");
        if (!Timestamp.isTimestamp(specified)) {
            Messenger.replyTo(event, Embeds.error("Unable to seek.", "The provided time is not valid. (Seconds, or Timestamp format)"), 15);
            return false;
        }
        Timestamp pos = Timestamp.from(specified).convert(TimeUnit.MILLISECONDS);

        if (!Numbers.isWithin(pos.getDuration(), 0L, audioTrack.getDuration())) {
            Messenger.replyTo(event, Embeds.error("Unable to seek.", "The result of the seek is outside the scope of the track."), 15);
            return false;
        }

        controller.setLastChannelId(event.getChannel().getId());
        controller.setPosition(pos.getDuration());
        Messenger.replyTo(event, Embeds.success("Seeking to: `%s`".formatted(pos.toSmallFrameStamp()), ""), 15);
        return true;
    }
}
