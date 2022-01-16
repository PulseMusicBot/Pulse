package dev.westernpine.pulse.component.button;

import dev.westernpine.lib.interaction.component.button.ButtonComponentHandler;
import dev.westernpine.pulse.component.button.buttons.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ButtonManager {

    public static List<ButtonComponentHandler> buttonHandlers;
    static {
        buttonHandlers = new ArrayList<>();
        buttonHandlers.add(new DocsButton());
        buttonHandlers.add(new FaqButton());
        buttonHandlers.add(new InviteButton());
        buttonHandlers.add(new SupportButton());
        buttonHandlers.add(new WebsiteButton());
    }

    public static List<ButtonComponentHandler> getComponentHandlers() {
        return buttonHandlers;
    }

    public static Optional<ButtonComponentHandler> get(String id) {
        return buttonHandlers.stream().filter(handler -> handler.id().equalsIgnoreCase(id)).findAny();
    }
}
