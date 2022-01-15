package dev.westernpine.pulse.listeners.system;

import dev.westernpine.eventapi.objects.EventHandler;
import dev.westernpine.eventapi.objects.Listener;
import dev.westernpine.pulse.events.system.SystemStartedEvent;

public class SystemStartedListener implements Listener {

    @EventHandler
    public void onSystemStartedEvent(SystemStartedEvent event) {
        System.out.println("System startup completed!");
    }

}
