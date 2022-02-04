package dev.westernpine.pulse.interactions.command.commands.informative;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.westernpine.lib.interaction.component.command.SlashCommandComponentHandler;
import dev.westernpine.lib.object.Scheduler;
import dev.westernpine.lib.object.Timestamp;
import dev.westernpine.lib.object.TriState;
import dev.westernpine.lib.util.Formatter;
import dev.westernpine.lib.util.ImageCrawler;
import dev.westernpine.lib.util.jda.Embeds;
import dev.westernpine.lib.util.jda.Messenger;
import dev.westernpine.pulse.Pulse;
import dev.westernpine.pulse.controller.Controller;
import dev.westernpine.pulse.controller.ControllerFactory;
import dev.westernpine.pulse.controller.settings.setting.Setting;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class Save implements SlashCommandComponentHandler {

    /**
     * @return How the command should be used.
     */
    @Override
    public String[] usages() {
        return new String[]{"save"};
    }

    /**
     * @return The command signifier string.
     */
    @Override
    public String command() {
        return "save";
    }

    /**
     * @return The command description.
     */
    @Override
    public String description() {
        return "Returns the currently playing track to your DM's.";
    }

    /**
     * @return The category of the command.
     */
    @Override
    public String category() {
        return "Informative";
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

        if (connectedChannel.isEmpty()) {
            Messenger.replyTo(event, Embeds.error("Unable to check current track.", "I'm not connected."), 15);
            return false;
        }

        AudioTrack audioTrack = controller.getPlayingTrack();

        if (audioTrack == null) {
            Messenger.replyTo(event, Embeds.error("Unable to check current track.", "I'm not playing anything."), 15);
            return false;
        }

        CompletableFuture<Boolean> dmCompletion = new CompletableFuture<>();
        Scheduler.addRequiredCompletion(dmCompletion);
        event.getUser().openPrivateChannel().queue(privateChannel -> {

            //The actual playing message.
            EmbedBuilder embedBuilder = Embeds.play("Now playing...", Formatter.formatInfo(audioTrack.getInfo()), audioTrack.getDuration(), Pulse.color(controller.getGuild()));
            TriState imageSize = controller.getSettings().get(Setting.IMAGE_SIZE).toTriState();
            if (!imageSize.isFalse()) {
                String imageUrl = ImageCrawler.findURL(audioTrack.getInfo().uri);
                embedBuilder = imageSize.isNone() ? embedBuilder.setThumbnail(imageUrl) : embedBuilder.setImage(imageUrl);
            }
            embedBuilder.appendDescription(("\n`%s` " + Formatter.formatProgressBar(audioTrack.getDuration(), audioTrack.getPosition())).formatted(new Timestamp(TimeUnit.MILLISECONDS, audioTrack.getPosition()).toSmallFrameStamp()));

            privateChannel.sendMessage(Messenger.messageBuilder(embedBuilder).build()).queue(messageSent -> dmCompletion.complete(true), dmCompletion::completeExceptionally);
        }, dmCompletion::completeExceptionally);

        controller.setLastChannelId(event.getChannel().getId());
        Messenger.replyTo(event, dmCompletion.isCompletedExceptionally()
                ? Embeds.error("I was unable to message you.", "")
                : Embeds.success("I messaged you the current track state.", ""), 15);
        return true;
    }
}
