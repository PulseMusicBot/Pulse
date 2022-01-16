package dev.westernpine.lib.interaction.component.menu;

import dev.westernpine.lib.interaction.component.Componentable;
import dev.westernpine.lib.interaction.component.IdentifiableComponent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;

public interface MenuComponentHandler extends IdentifiableComponent, Componentable {

    public boolean handle(SelectionMenuEvent event);

}
