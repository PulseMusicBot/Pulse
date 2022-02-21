package dev.westernpine.pulse.listeners.system.premium;

import dev.westernpine.eventapi.objects.EventHandler;
import dev.westernpine.eventapi.objects.Listener;
import dev.westernpine.pulse.Pulse;
import dev.westernpine.pulse.controller.EndCase;
import dev.westernpine.pulse.events.system.premium.PremiumUpdateEvent;
import dev.westernpine.pulse.properties.IdentityProperties;

public class PremiumUpdateListener implements Listener {

    @EventHandler
    public void onPremiumUpdate(PremiumUpdateEvent event) {
        if(event.wasPremium() && event.getController().isConnected() && Boolean.parseBoolean(Pulse.identityProperties.get(IdentityProperties.PREMIUM))) {
            event.getController().destroy(EndCase.PREMIUM_EXPIRED);
        }
    }

}
