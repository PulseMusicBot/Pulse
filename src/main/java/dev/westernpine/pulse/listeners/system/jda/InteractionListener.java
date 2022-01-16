package dev.westernpine.pulse.listeners.system.jda;

import dev.westernpine.lib.interaction.component.button.ButtonComponentHandler;
import dev.westernpine.lib.interaction.component.command.SlashCommandComponentHandler;
import dev.westernpine.lib.interaction.component.menu.MenuComponentHandler;
import dev.westernpine.pulse.component.button.ButtonManager;
import dev.westernpine.pulse.component.command.CommandManager;
import dev.westernpine.pulse.component.menu.MenuManager;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class InteractionListener extends ListenerAdapter {

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        for (SlashCommandComponentHandler handler : CommandManager.getComponentHandlers())
            if (handler.handle(event))
                break;
    }

    @Override
    public void onButtonClick(ButtonClickEvent event) {
        for (ButtonComponentHandler handler : ButtonManager.getComponentHandlers())
            if (handler.handle(event))
                break;
    }

    @Override
    public void onSelectionMenu(SelectionMenuEvent event) {
        for (MenuComponentHandler handler : MenuManager.getComponentHandlers())
            if (handler.handle(event))
                break;
    }

}
