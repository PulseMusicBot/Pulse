package dev.westernpine.pulse.listeners.system.state;

import dev.westernpine.eventapi.objects.EventHandler;
import dev.westernpine.eventapi.objects.Listener;
import dev.westernpine.lib.object.State;
import dev.westernpine.pulse.events.system.StateChangeEvent;

public class ShutdownListener implements Listener {

    @EventHandler
    public void onShutdown(StateChangeEvent event) {
        if (!event.getNewState().is(State.SHUTDOWN))
            return;
        System.exit(0);
    }

}
