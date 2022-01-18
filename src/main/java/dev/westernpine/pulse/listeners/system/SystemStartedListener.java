package dev.westernpine.pulse.listeners.system;

import dev.westernpine.eventapi.objects.EventHandler;
import dev.westernpine.eventapi.objects.Listener;
import dev.westernpine.pulse.Pulse;
import dev.westernpine.pulse.events.system.SystemStartedEvent;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

public class SystemStartedListener implements Listener {

    @EventHandler
    public void onSystemStartedEvent(SystemStartedEvent event) {
        System.out.println("Startup >> System startup completed!");
        Pulse.shardManager.getShards().forEach(jda -> jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.listening("/help")));
    }

}
