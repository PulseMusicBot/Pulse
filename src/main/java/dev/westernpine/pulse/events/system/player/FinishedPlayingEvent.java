package dev.westernpine.pulse.events.system.player;

import dev.westernpine.eventapi.objects.Event;
import dev.westernpine.pulse.controller.Controller;

public class FinishedPlayingEvent extends Event {

    private Controller controller;

    public FinishedPlayingEvent(Controller controlller) {
        this.controller = controlller;
    }

    public Controller getController() {
        return controller;
    }

}
