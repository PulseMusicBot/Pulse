package dev.westernpine.pulse.interactions.command.commands.player;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.westernpine.bettertry.Try;
import dev.westernpine.lib.interaction.component.command.SlashCommandComponentHandler;
import dev.westernpine.lib.object.Timestamp;
import dev.westernpine.lib.util.Numbers;
import dev.westernpine.lib.util.jda.Embeds;
import dev.westernpine.lib.util.jda.Messenger;
import dev.westernpine.pulse.controller.Controller;
import dev.westernpine.pulse.controller.ControllerFactory;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class RW implements SlashCommandComponentHandler {

    /**
     * @return How the command should be used.
     */
    @Override
    public String[] usages() {
        return new String[]{"rw [amount]"};
    }

    /**
     * @return The command signifier string.
     */
    @Override
    public String command() {
        return "rw";
    }

    /**
     * @return The command description.
     */
    @Override
    public String description() {
        return "Rewind the current track by 10 seconds as default, or by a specific amount.";
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
        options.add(new OptionData(OptionType.STRING, "amount", "The time to rewind the track by in either seconds or timestamp format."));
        return options;
    }

    @Override
    public boolean handle(SlashCommandEvent event) {
        Controller controller = ControllerFactory.get(event.getGuild().getId(), true);
        Optional<AudioChannel> connectedChannel = controller.getConnectedChannel();

        if (connectedChannel.isEmpty()) {
            Messenger.replyTo(event, Embeds.error("Unable to rewind.", "I'm not connected."), 15);
            return false;
        }

        if (!controller.getVoiceState(event.getMember()).inAudioChannel() || !connectedChannel.get().getId().equals(controller.getVoiceState(event.getMember()).getChannel().getId())) {
            Messenger.replyTo(event, Embeds.error("Unable to set volume.", "We must be in the same channel."), 15);
            return false;
        }

        //playing checks
        AudioTrack audioTrack = controller.getPlayingTrack();

        if (audioTrack == null) {
            Messenger.replyTo(event, Embeds.error("Unable to rewind.", "I'm not playing anything."), 15);
            return false;
        }

        if (!audioTrack.isSeekable() || audioTrack.getInfo().isStream) {
            Messenger.replyTo(event, Embeds.error("Unable to rewind.", "This track is not seekable."), 15);
            return false;
        }

        Timestamp pos = new Timestamp(TimeUnit.MILLISECONDS, 10000L);

        if (event.getOption("amount") != null) {
            String specified = Try.to(() -> event.getOption("amount").getAsString()).orElse("");
            if (!Timestamp.isTimestamp(specified)) {
                Messenger.replyTo(event, Embeds.error("Unable to rewind.", "The provided time is not valid. (Seconds, or Timestamp format)"), 15);
                return false;
            }
            pos = Timestamp.from(specified).convert(TimeUnit.MILLISECONDS);
        }

        long amount = TimeUnit.SECONDS.convert(pos.getDuration(), pos.getTimeUnit());
        pos.setDuration(controller.getPosition() - pos.getDuration());

        if (!Numbers.isWithin(pos.getDuration(), 0L, audioTrack.getDuration())) {
            Messenger.replyTo(event, Embeds.error("Unable to rewind.", "The result of the rewind is outside the scope of the track."), 15);
            return false;
        }

        controller.setLastChannelId(event.getChannel().getId());
        controller.setPosition(pos.getDuration());
        Messenger.replyTo(event, Embeds.success("Rewinding `%d` seconds.".formatted(amount), ""), 15);
        return true;
    }
}
