package dev.westernpine.pulse.component.menu;

import dev.westernpine.lib.interaction.component.button.ButtonComponentHandler;
import dev.westernpine.lib.interaction.component.menu.MenuComponentHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MenuManager {

    public static List<MenuComponentHandler> menuHandlers;
    static {
        menuHandlers = new ArrayList<>();
    }

    public static List<MenuComponentHandler> getComponentHandlers() {
        return menuHandlers;
    }

    public static Optional<MenuComponentHandler> get(String id) {
        return menuHandlers.stream().filter(handler -> handler.id().equalsIgnoreCase(id)).findAny();
    }
}
