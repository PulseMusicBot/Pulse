package dev.westernpine.pulse.component.command.commands;

import dev.westernpine.lib.interaction.component.command.SlashCommandComponentHandler;
import dev.westernpine.pulse.component.command.CommandManager;
import dev.westernpine.pulse.listeners.system.jda.InteractionListener;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.internal.interactions.CommandInteractionImpl;

import java.util.*;
import java.util.stream.Collectors;

public class Commands implements SlashCommandComponentHandler {
    @Override
    public String[] usages() {
        return new String[] {"commands"};
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
    public String category() { return "Utility"; }

    @Override
    public LinkedList<OptionData> options() {
        return new LinkedList<>();
    }

    @Override
    public boolean handle(SlashCommandEvent event) {
        if(!event.getName().equals(command()))
            return false;



        EmbedBuilder commandsEmbed = new EmbedBuilder().setTitle("All Commands");
        event.replyEmbeds(commandsEmbed.build()).queue();
        return true;
    }
}
