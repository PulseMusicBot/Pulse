package dev.westernpine.pulse.interactions.command.commands.help;

import dev.westernpine.lib.interaction.component.command.SlashCommandComponentHandler;
import dev.westernpine.lib.util.jda.Messenger;
import dev.westernpine.pulse.Pulse;
import dev.westernpine.pulse.interactions.command.CommandManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.LinkedList;

public class Commands implements SlashCommandComponentHandler {
    @Override
    public String[] usages() {
        return new String[]{"commands"};
    }

    @Override
    public String command() {
        return "commands";
    }

    @Override
    public String description() {
        return "Lists all available commands.";
    }

    @Override
    public String category() {
        return "Help";
    }

    @Override
    public LinkedList<OptionData> options() {
        return new LinkedList<>();
    }

    @Override
    public boolean handle(SlashCommandEvent event) {
        EmbedBuilder embed = new EmbedBuilder().setTitle("All Commands");
        CommandManager.getSortedComponentHandlers().forEach((category, commands) -> embed.addField(category, String.join(", ", commands.stream().map(command -> "`" + command.id() + "`").toArray(String[]::new)), false));
        embed.setColor(Pulse.color());
        embed.setFooter("To learn more about a command, type \"/help [command]\"");
        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
        return true;
    }
}
