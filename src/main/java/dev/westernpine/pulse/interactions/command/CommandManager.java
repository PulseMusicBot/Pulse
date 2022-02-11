package dev.westernpine.pulse.interactions.command;

import dev.westernpine.lib.interaction.component.command.SlashCommandComponentHandler;
import dev.westernpine.pulse.interactions.command.commands.help.Commands;
import dev.westernpine.pulse.interactions.command.commands.help.Help;
import dev.westernpine.pulse.interactions.command.commands.informative.NowPlaying;
import dev.westernpine.pulse.interactions.command.commands.informative.Queue;
import dev.westernpine.pulse.interactions.command.commands.informative.Save;
import dev.westernpine.pulse.interactions.command.commands.informative.Status;
import dev.westernpine.pulse.interactions.command.commands.management.*;
import dev.westernpine.pulse.interactions.command.commands.player.*;
import dev.westernpine.pulse.interactions.command.commands.queue.*;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class CommandManager {

    private static final LinkedList<SlashCommandComponentHandler> slashCommandComponentHandlers = new LinkedList<>();
    private static final LinkedHashMap<String, LinkedList<SlashCommandComponentHandler>> sortedSlashCommandComponentHandlers = new LinkedHashMap<>();

    static {
        Stream.of(
                        //Help
                        new Help(),
                        new Commands(),

                        //Informative
                        new NowPlaying(),
                        new Save(),
                        new Status(),
                        new Queue(),

                        //Management
                        new DefaultPlatform(),
                        new DefaultVolume(),
                        new DJMode(),
                        new DJRole(),
                        new ImageSize(),
                        new ShufflePlaylists(),
                        new VoiceDetection(),
                        new TrackUpdates(),
                        new DisconnectCleanup(),
                        new TwentryFourSeven(),
                        new JoinMusic(),

                        //Player
                        new Join(),
                        new Leave(),
                        new Stop(),
                        new Pause(),
                        new Play(),
                        new PlayFirst(),
                        new PlayNow(),
                        new FF(),
                        new RW(),
                        new Restart(),
                        new Volume(),
                        new Seek(),

                        //Queue
                        new Clear(),
                        new Flip(),
                        new ForceNext(),
                        new ForceBack(),
                        new Next(),
                        new Back(),
                        new Move(),
                        new Remove(),
                        new Repeat(),
                        new Shuffle(),
                        new SkipTo()
                )
                .sorted()
                .forEachOrdered(slashCommandComponentHandlers::add);
        Executors.newScheduledThreadPool(1);
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
