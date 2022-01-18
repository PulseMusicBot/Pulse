package dev.westernpine.pulse.component.button;

import dev.westernpine.lib.interaction.component.button.ButtonComponentHandler;
import dev.westernpine.pulse.component.button.buttons.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class ButtonManager {

    private static final LinkedList<ButtonComponentHandler> buttonHandlers = new LinkedList<>();
    static {
        Stream.of(new DocsButton(),
                        new FaqButton(),
                        new InviteButton(),
                        new SupportButton(),
                        new WebsiteButton())
                .sorted()
                .forEachOrdered(buttonHandlers::add);
    }

    public static LinkedList<ButtonComponentHandler> getComponentHandlers() {
        return buttonHandlers;
    }

    public static Optional<ButtonComponentHandler> get(String id) {
        return buttonHandlers.stream().filter(handler -> handler.id().equalsIgnoreCase(id)).findAny();
    }
}
