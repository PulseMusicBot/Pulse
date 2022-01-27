package dev.westernpine.pulse.interactions.menu;

import dev.westernpine.lib.interaction.component.menu.MenuComponentHandler;

import java.util.LinkedList;
import java.util.Optional;

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
