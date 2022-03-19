package dev.westernpine.pulse.interactions.command.commands.informative;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.westernpine.lib.interaction.component.command.SlashCommandComponentHandler;
import dev.westernpine.lib.util.Formatter;
import dev.westernpine.lib.util.Numbers;
import dev.westernpine.lib.util.Splitter;
import dev.westernpine.lib.util.jda.Embeds;
import dev.westernpine.lib.util.jda.Messenger;
import dev.westernpine.pulse.Pulse;
import dev.westernpine.pulse.controller.Controller;
import dev.westernpine.pulse.controller.ControllerFactory;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class Queue implements SlashCommandComponentHandler {

    /**
     * @return How the command should be used.
     */
    @Override
    public String[] usages() {
        return new String[]{"queue [page]"};
    }

    /**
     * @return The command signifier string.
     */
    @Override
    public String command() {
        return "queue";
    }

    /**
     * @return The command description.
     */
    @Override
    public String description() {
        return "Returns the currently queue at the specified page if any.";
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
        options.add(new OptionData(OptionType.INTEGER, "page", "The page of the queue you would like to view."));
        return options;
    }

    @Override
    public boolean handle(SlashCommandEvent event) {
        Controller controller = ControllerFactory.get(event.getGuild().getId(), true);
        Optional<AudioChannel> connectedChannel = controller.getConnectedChannel();

        if (connectedChannel.isEmpty()) {
            Messenger.replyTo(event, Embeds.error("Unable to check the queue.", "I'm not connected."), 15);
            return false;
        }

        AudioTrack audioTrack = controller.getPlayingTrack();

        if (controller.getQueue().isEmpty()) {
            Messenger.replyTo(event, Embeds.error("Unable to check the queue.", "Insufficient queue size."), 15);
            return false;
        }

        //Actual formatting of the message.
        int pageSize = 5;
        List<List<AudioTrack>> splitQueue = Splitter.split(controller.getQueue(), pageSize);
        int maxPages = splitQueue.size();
        int page = Numbers.setWithin(Long.valueOf(event.getOption("page") == null ? 1L : event.getOption("page").getAsLong()).intValue(), 1, maxPages);
        List<AudioTrack> pageTracks = splitQueue.get(page - 1);
        EmbedBuilder embedBuilder = Embeds.info(":scroll: %s's Queue".formatted(event.getGuild().getName()), "", Pulse.color(event.getGuild()));
        embedBuilder.setFooter("Page: %d/%d".formatted(page, maxPages), event.getGuild().getIconUrl());
        for (int i = 0; i < pageTracks.size(); i++)
            embedBuilder.appendDescription("`%d.` %s\n\n".formatted((pageSize * (page - 1)) + (i + 1), Formatter.formatInfo(pageTracks.get(i).getInfo())));

        controller.setLastChannelId(event.getChannel().getId());
        Messenger.replyTo(event, embedBuilder, 15);
        return true;
    }
}
