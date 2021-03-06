package dev.westernpine.pulse.interactions.button;

import dev.westernpine.lib.interaction.component.button.ButtonComponentHandler;
import dev.westernpine.pulse.interactions.button.buttons.links.InviteButton;
import dev.westernpine.pulse.interactions.button.buttons.links.SupportButton;
import dev.westernpine.pulse.interactions.button.buttons.links.WebsiteButton;

import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Stream;

public class ButtonManager {

    private static final LinkedList<ButtonComponentHandler> buttonHandlers = new LinkedList<>();

    static {
        Stream.of(
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
