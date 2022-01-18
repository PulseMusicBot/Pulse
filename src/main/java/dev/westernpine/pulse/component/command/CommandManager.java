package dev.westernpine.pulse.component.command;

import dev.westernpine.lib.interaction.component.command.SlashCommandComponentHandler;
import dev.westernpine.pulse.component.command.commands.Commands;
import dev.westernpine.pulse.component.command.commands.Help;

import java.util.*;
import java.util.stream.Collectors;

//TODO: Turn this into an extendable object! Reuse methods and initializers.

public class CommandManager {

    public static List<SlashCommandComponentHandler> slashCommandComponentHandlers;
    public static LinkedHashMap<String, SlashCommandComponentHandler> sortedCategory;
    static {
        slashCommandComponentHandlers = new ArrayList<>();
        slashCommandComponentHandlers.add(new Help());
        slashCommandComponentHandlers.add(new Commands());

        LinkedHashMap<String, LinkedList<SlashCommandComponentHandler>> commandCategories = new LinkedHashMap<>();
        for(SlashCommandComponentHandler handler : getComponentHandlers()) {
            String category = handler.category();
            if(!commandCategories.containsKey(category))
                commandCategories.put(category, new LinkedList<>());
            commandCategories.get(category).add(handler);
        }
        LinkedHashMap<String, LinkedList<SlashCommandComponentHandler>> categories = new LinkedHashMap<>(commandCategories);
        commandCategories.clear();
        categories.keySet().stream().sorted().forEach(key ->  commandCategories.put(key, categories.get(key)));
        for(Map.Entry<String, LinkedList<SlashCommandComponentHandler>> entry : commandCategories.entrySet()) {
            String category = entry.getKey();
            LinkedList<SlashCommandComponentHandler> commands = new LinkedList<>();
            Map<String, SlashCommandComponentHandler> commandMap = entry.getValue().stream().collect(Collectors.toMap(SlashCommandComponentHandler::command, command -> command));
            commandMap.keySet().stream().sorted().forEach(key -> commands.add(commandMap.get(key)));
            commandCategories.put(category, commands);
        }

    }

    public static List<SlashCommandComponentHandler> getComponentHandlers() {
        return slashCommandComponentHandlers;
    }

    public static Optional<SlashCommandComponentHandler> get(String command) {
        return slashCommandComponentHandlers.stream().filter(handler -> handler.command().equalsIgnoreCase(command)).findAny();
    }
}
