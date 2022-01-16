package dev.westernpine.pulse.component.command;

import dev.westernpine.lib.interaction.component.command.SlashCommandComponentHandler;
import dev.westernpine.pulse.component.command.commands.Help;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CommandManager {

    public static List<SlashCommandComponentHandler> slashCommandComponentHandlers;
    static {
        slashCommandComponentHandlers = new ArrayList<>();
        slashCommandComponentHandlers.add(new Help());
    }

    public static List<SlashCommandComponentHandler> getComponentHandlers() {
        return slashCommandComponentHandlers;
    }

    public static Optional<SlashCommandComponentHandler> get(String command) {
        return slashCommandComponentHandlers.stream().filter(handler -> handler.command().equalsIgnoreCase(command)).findAny();
    }
}
