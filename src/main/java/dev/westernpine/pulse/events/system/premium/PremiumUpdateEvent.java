package dev.westernpine.pulse.events.system.premium;

import dev.westernpine.eventapi.objects.Event;
import dev.westernpine.pulse.controller.Controller;

public class PremiumUpdateEvent extends Event {

    private final Controller controller;
    private final boolean wasPremium;
    private final boolean isPremium;

    public PremiumUpdateEvent(Controller controller, boolean wasPremium, boolean isPremium) {
        this.controller = controller;
        this.wasPremium = wasPremium;
        this.isPremium = isPremium;
    }

    public Controller getController() {
        return controller;
    }

    public boolean wasPremium() {
        return wasPremium;
    }

    public boolean isPremium() {
        return isPremium;
    }
}
