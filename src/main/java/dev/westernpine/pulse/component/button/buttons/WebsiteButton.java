package dev.westernpine.pulse.component.button.buttons;

import dev.westernpine.lib.interaction.component.button.ButtonComponentHandler;
import dev.westernpine.pulse.Pulse;
import dev.westernpine.pulse.properties.IdentityProperties;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.Component;

public class WebsiteButton implements ButtonComponentHandler {

    @Override
    public String id() {
        return "website";
    }

    @Override
    public Component toComponent() {
        return Button.link("https://%s".formatted(Pulse.identityProperties.get(IdentityProperties.HOST)), label());
    }

    @Override
    public String label() {
        return "Website";
    }

    @Override
    public boolean handle(ButtonClickEvent event) {
        return false;
    }
}
