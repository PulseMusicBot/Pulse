package dev.westernpine.pulse.interactions.command.commands.player;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.westernpine.bettertry.Try;
import dev.westernpine.lib.interaction.component.command.SlashCommandComponentHandler;
import dev.westernpine.lib.object.Timestamp;
import dev.westernpine.lib.util.jda.Embeds;
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
        options.add(new OptionData(OptionType.STRING, "amount", "The amount in seconds, or timestamp format to fast forward a track by."));
        return options;
    }

    @Override
    public boolean handle(SlashCommandEvent event) {
        Controller controller = ControllerFactory.get(event.getGuild().getId(), true);
        Optional<AudioChannel> connectedChannel = controller.getConnectedChannel();

        if(connectedChannel.isEmpty()) {
            event.replyEmbeds(Embeds.error("Unable to fast-forward.", "I'm not connected.").build()).queue(interactionHook -> interactionHook.deleteOriginal().queueAfter(15, TimeUnit.SECONDS));
            return false;
        }

        if (!controller.getVoiceState(event.getMember()).inAudioChannel()) {
            event.replyEmbeds(Embeds.error("Unable to fast-forward.", "You must be in a channel.").build()).queue(interactionHook -> interactionHook.deleteOriginal().queueAfter(15, TimeUnit.SECONDS));
            return false;
        }

        //playing checks
        AudioTrack audioTrack = controller.getPlayingTrack();

        if(audioTrack == null) {
            event.replyEmbeds(Embeds.error("Unable to fast-forward.", "I'm not playing anything.").build()).queue(interactionHook -> interactionHook.deleteOriginal().queueAfter(15, TimeUnit.SECONDS));
            return false;
        }

        if(!audioTrack.isSeekable() || audioTrack.getInfo().isStream) {
            event.replyEmbeds(Embeds.error("Unable to fast-forward.", "This track is not seekable.").build()).queue(interactionHook -> interactionHook.deleteOriginal().queueAfter(15, TimeUnit.SECONDS));
            return false;
        }

        if (connectedChannel.isPresent()
                && !connectedChannel.get().getId().equals(controller.getVoiceState(event.getMember()).getChannel().getId())
                && !controller.getConnectedMembers().isEmpty()
                && controller.getPlayingTrack() != null) {
            event.replyEmbeds(Embeds.error("Unable to fast-forward.", "I'm currently playing for others.").build()).queue(interactionHook -> interactionHook.deleteOriginal().queueAfter(15, TimeUnit.SECONDS));
            return false;
        }

        Timestamp pos = new Timestamp(TimeUnit.MILLISECONDS, 10000L);

        if (event.getOption("amount") != null) {
            String specified = Try.to(() -> event.getOption("amount").getAsString()).orElse("");
            if(!Timestamp.isTimestamp(specified)) {
                event.replyEmbeds(Embeds.error("Unable to fast-forward.", "The provided time is not valid. (Seconds, or Timestamp format [hh:mm:ss])").build()).queue(interactionHook -> interactionHook.deleteOriginal().queueAfter(15, TimeUnit.SECONDS));
                return false;
            }
            pos = Timestamp.from(specified).convert(TimeUnit.MILLISECONDS);
        }

        long amount = TimeUnit.SECONDS.convert(pos.getDuration(), pos.getTimeUnit());
        pos.setDuration(controller.getPosition() - pos.getDuration());

        if(pos.getDuration() < 0L) {
            event.replyEmbeds(Embeds.error("Unable to fast-forward.", "The result of the rewind is outside the length of the track.").build()).queue(interactionHook -> interactionHook.deleteOriginal().queueAfter(15, TimeUnit.SECONDS));
            return false;
        }

        controller.setLastChannelId(event.getChannel().getId());
        controller.setPosition(pos.getDuration());
        event.replyEmbeds(Embeds.success("Rewinding `%d` seconds.".formatted(amount), "").build()).queue(interactionHook -> interactionHook.deleteOriginal().queueAfter(15, TimeUnit.SECONDS));
        return true;
    }
}
