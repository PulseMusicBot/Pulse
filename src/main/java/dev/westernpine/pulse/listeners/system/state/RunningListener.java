package dev.westernpine.pulse.listeners.system.state;

import dev.westernpine.eventapi.objects.EventHandler;
import dev.westernpine.eventapi.objects.Listener;
import dev.westernpine.lib.object.State;
import dev.westernpine.pulse.Pulse;
import dev.westernpine.pulse.controller.ControllerFactory;
import dev.westernpine.pulse.events.system.StateChangeEvent;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

import static dev.westernpine.pulse.logging.Logger.logger;

public class RunningListener implements Listener {

    @EventHandler
    public void onRunning(StateChangeEvent event) {
        if(!event.getNewState().is(State.RUNNING))
            return;
        logger.info("System startup completed!");
        Pulse.shardManager.getShards().forEach(jda -> jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.listening("/help")));
    }

}
