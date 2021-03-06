package dev.westernpine.pulse.interactions.button.buttons.links;

import dev.westernpine.lib.interaction.component.button.ButtonComponentHandler;
import dev.westernpine.pulse.Pulse;
import dev.westernpine.pulse.properties.IdentityProperties;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.Component;

public class SupportButton implements ButtonComponentHandler {

    @Override
    public String id() {
        return "support";
    }

    @Override
    public Component toComponent() {
        return Button.link(Pulse.identityProperties.get(IdentityProperties.SUPPORT), label());
    }

    @Override
    public String label() {
        return "Support";
    }

    @Override
    public boolean handle(ButtonClickEvent event) {
        return false;
    }
}
