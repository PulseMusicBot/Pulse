package dev.westernpine.pulse.component.menu;

import dev.westernpine.lib.interaction.component.button.ButtonComponentHandler;
import dev.westernpine.lib.interaction.component.menu.MenuComponentHandler;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class MenuManager {

    private static final LinkedList<MenuComponentHandler> menuHandlers = new LinkedList<>();
    static {
//        Stream.of()
//                .sorted()
//                .forEachOrdered(menuHandlers::add);
    }

    public static LinkedList<MenuComponentHandler> getComponentHandlers() {
        return menuHandlers;
    }

    public static Optional<MenuComponentHandler> get(String id) {
        return menuHandlers.stream().filter(handler -> handler.id().equalsIgnoreCase(id)).findAny();
    }
}
