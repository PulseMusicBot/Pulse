package dev.westernpine.pulse.listeners.system.jda;

import dev.westernpine.pulse.Pulse;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ReadyListener extends ListenerAdapter {

    @Override
    public void onReady(ReadyEvent event) {
        Pulse.readyHandler.accept(event);
    }
}
