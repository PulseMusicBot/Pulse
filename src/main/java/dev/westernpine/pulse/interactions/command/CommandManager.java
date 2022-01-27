package dev.westernpine.pulse.interactions.command;

import dev.westernpine.lib.interaction.component.command.SlashCommandComponentHandler;
import dev.westernpine.pulse.interactions.command.commands.Commands;
import dev.westernpine.pulse.interactions.command.commands.Help;
import dev.westernpine.pulse.interactions.command.commands.Play;

import java.util.*;
import java.util.stream.Stream;

public class CommandManager {

    private static final LinkedList<SlashCommandComponentHandler> slashCommandComponentHandlers = new LinkedList<>();
    private static final LinkedHashMap<String, LinkedList<SlashCommandComponentHandler>> sortedSlashCommandComponentHandlers = new LinkedHashMap<>();

    static {
        Stream.of(new Help(),
                        new Commands(),
                        new Play())
                .sorted()
                .forEachOrdered(slashCommandComponentHandlers::add);

        //Sort commands into their respective categories.
        Map<String, LinkedList<SlashCommandComponentHandler>> categorized = new HashMap<>();
        CommandManager.getComponentHandlers().stream().forEachOrdered(command -> {
            String category = command.category();
            if (!categorized.containsKey(category))
                categorized.put(category, new LinkedList<>());
            categorized.get(category).add(command);
        });
        //Alphabetically sort categories. No need to re-sort all commands for each category, as they still maintain their order when being added to the map.
        categorized.entrySet().stream().sorted((first, second) -> first.getKey().compareTo(second.getKey())).forEachOrdered(entry -> sortedSlashCommandComponentHandlers.put(entry.getKey(), entry.getValue()));


    }

    public static LinkedList<SlashCommandComponentHandler> getComponentHandlers() {
        return slashCommandComponentHandlers;
    }

    public static LinkedHashMap<String, LinkedList<SlashCommandComponentHandler>> getSortedComponentHandlers() {
        return sortedSlashCommandComponentHandlers;
    }

    public static Optional<SlashCommandComponentHandler> get(String command) {
        return slashCommandComponentHandlers.stream().filter(handler -> handler.command().equalsIgnoreCase(command)).findAny();
    }
}
