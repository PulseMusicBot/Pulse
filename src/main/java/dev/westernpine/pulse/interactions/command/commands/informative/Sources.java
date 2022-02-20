package dev.westernpine.pulse.interactions.command.commands.informative;

import dev.westernpine.lib.interaction.component.command.SlashCommandComponentHandler;
import dev.westernpine.lib.player.audio.track.userdata.platform.Platform;
import dev.westernpine.lib.player.audio.track.userdata.platform.PlatformManager;
import dev.westernpine.lib.util.Strings;
import dev.westernpine.lib.util.jda.Embeds;
import dev.westernpine.lib.util.jda.Messenger;
import dev.westernpine.pulse.Pulse;
import dev.westernpine.pulse.controller.Controller;
import dev.westernpine.pulse.controller.ControllerFactory;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class Sources implements SlashCommandComponentHandler {

    /**
     * @return How the command should be used.
     */
    @Override
    public String[] usages() {
        return new String[]{"sources"};
    }

    /**
     * @return The command signifier string.
     */
    @Override
    public String command() {
        return "sources";
    }

    /**
     * @return The command description.
     */
    @Override
    public String description() {
        return "Returns a list of all available sources and their abilities.";
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

        List<String> searchableAndSimilar = PlatformManager.getPlatforms()
                .stream()
                .filter(platform -> platform.canSearch() && platform.canSearchSimilar())
                .map(platform -> "`" + platform.getOfficialName() + "`")
                .toList();

        List<String> searchable = PlatformManager.getPlatforms()
                .stream()
                .filter(platform -> platform.canSearch() && !platform.canSearchSimilar())
                .map(platform -> "`" + platform.getOfficialName() + "`")
                .toList();

        List<String> similar = PlatformManager.getPlatforms()
                .stream()
                .filter(platform -> !platform.canSearch() && platform.canSearchSimilar())
                .map(platform -> "`" + platform.getOfficialName() + "`")
                .toList();

        List<String> others = PlatformManager.getPlatforms()
                .stream()
                .filter(platform -> !platform.canSearch() && !platform.canSearchSimilar())
                .map(platform -> "`" + platform.getOfficialName() + "`")
                .toList();

        controller.setLastChannelId(event.getChannel().getId());

        //The actual playing message.
        EmbedBuilder embedBuilder = Embeds.info(":information_source: Sources", "", Pulse.color(event.getGuild()));
        addFieldIfPresent(embedBuilder, "Searchable & Similar-Searchable", searchableAndSimilar);
        addFieldIfPresent(embedBuilder, "Only Searchable", searchable);
        addFieldIfPresent(embedBuilder, "Only Similar-Searchable", similar);
        addFieldIfPresent(embedBuilder, "Others", others);
        embedBuilder.setFooter("Total Sources: " + PlatformManager.getPlatforms().size());
        Messenger.replyTo(event, embedBuilder, 15);
        return true;
    }

    public void addFieldIfPresent(EmbedBuilder embedBuilder, String title, List<String> items) {
        if(items.isEmpty())
            return;
        embedBuilder.addField(title, String.join(", ", items), false);
    }
}
