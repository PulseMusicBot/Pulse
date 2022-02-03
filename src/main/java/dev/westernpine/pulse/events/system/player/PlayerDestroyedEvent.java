package dev.westernpine.pulse.events.system.player;

import dev.westernpine.eventapi.objects.Event;
import dev.westernpine.pulse.controller.Controller;
import dev.westernpine.pulse.controller.EndCase;

public class PlayerDestroyedEvent extends Event {

    private Controller controller;
    private EndCase endCase;
    private boolean connected;

    public PlayerDestroyedEvent(Controller controller, EndCase endCase, boolean connected) {
        this.controller = controller;
        this.endCase = endCase;
        this.connected = connected;
    }

    public Controller getController() {
        return controller;
    }

    public EndCase getEndCase() {
        return endCase;
    }

    public boolean wasConnected() {
        return connected;
    }
}
