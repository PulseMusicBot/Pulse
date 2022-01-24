package dev.westernpine.lib.interaction.component.button;

import dev.westernpine.lib.interaction.component.Componentable;
import dev.westernpine.lib.interaction.component.IdentifiableComponent;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.components.Component;

public interface ButtonComponentHandler extends IdentifiableComponent, Componentable {

    public Component toComponent();

    public String label();

    public boolean handle(ButtonClickEvent event);

}
