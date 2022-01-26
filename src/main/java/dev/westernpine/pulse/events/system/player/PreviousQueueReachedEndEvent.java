package dev.westernpine.pulse.events.system.player;

import dev.westernpine.eventapi.objects.Event;
import dev.westernpine.pulse.controller.Controller;

public class PreviousQueueReachedEndEvent extends Event {

    private Controller controller;

    public PreviousQueueReachedEndEvent(Controller controller) {
        this.controller = controller;
    }

    public Controller getController() {
        return controller;
    }
}
