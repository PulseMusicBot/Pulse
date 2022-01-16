package dev.westernpine.pulse.component.button.buttons;

import dev.westernpine.lib.interaction.component.button.ButtonComponentHandler;
import dev.westernpine.pulse.Pulse;
import dev.westernpine.pulse.properties.IdentityProperties;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.Component;

public class InviteButton implements ButtonComponentHandler {

    @Override
    public String id() {
        return "invite";
    }

    @Override
    public Component toComponent() {
        return Button.link("https://%s/invite".formatted(Pulse.identityProperties.get(IdentityProperties.HOST)), label());
    }

    @Override
    public String label() {
        return "Invite";
    }

    @Override
    public boolean handle(ButtonClickEvent event) {
        return false;
    }
}
