package dev.westernpine.pulse.listeners.system.jda;

import dev.westernpine.lib.interaction.component.button.ButtonComponentHandler;
import dev.westernpine.lib.interaction.component.command.SlashCommandComponentHandler;
import dev.westernpine.lib.interaction.component.menu.MenuComponentHandler;
import dev.westernpine.pulse.interactions.button.ButtonManager;
import dev.westernpine.pulse.interactions.command.CommandManager;
import dev.westernpine.pulse.interactions.menu.MenuManager;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import static dev.westernpine.pulse.logging.Logger.logger;

public class InteractionListener extends ListenerAdapter {

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        for (SlashCommandComponentHandler handler : CommandManager.getComponentHandlers()) {
            if (event.getName().equals(handler.command())) {
                handler.handle(event);
                break;
            }
        }
        if (!event.isAcknowledged())
            logger.warning("A slash command event was not acknowledged: " + event.getCommandString());
    }

    @Override
    public void onButtonClick(ButtonClickEvent event) {
        for (ButtonComponentHandler handler : ButtonManager.getComponentHandlers()) {
            if (event.getComponentId().equals(handler.id())) {
                handler.handle(event);
                break;
            }
        }
        if (!event.isAcknowledged())
            logger.warning("A button click event was not acknowledged: " + event.getComponentId());
    }

    @Override
    public void onSelectionMenu(SelectionMenuEvent event) {
        for (MenuComponentHandler handler : MenuManager.getComponentHandlers()) {
            if (event.getComponentId().equals(handler.id())) {
                handler.handle(event);
                break;
            }
        }
        if (!event.isAcknowledged())
            logger.warning("A menu selection event was not acknowledged: " + event.getComponentId());
    }

}
