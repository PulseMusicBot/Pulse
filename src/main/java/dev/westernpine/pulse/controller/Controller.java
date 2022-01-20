package dev.westernpine.pulse.controller;

import dev.westernpine.pulse.Pulse;
import net.dv8tion.jda.api.entities.Guild;

import java.util.*;

public class Controller {

    //TODO: Make this class into a manager as well as a singleton object.
    // Let each controller store a "lastValidAccess" value that a timer can determine if it is viable to disconnect or not, then to delete or not (determined by a bots voice state).
    // Dont forget to include settings...
    // Audio Interface as well.
    // serializeable controller to save the current state on shutdown.
    // maximum allowed enques.

    //TODO: Store tracks as plain wrappers... for EVERY track.

    public static final long MAX_LIFETIME = 15*60;

    private static Map<String, Controller> controllers = new HashMap<>();

    static {
        Pulse.scheduler.runLaterRepeatingAsync(() -> {
            //TODO: Work on audio capabilities so we can track if controller needs to be deleted or cached.
        }, 0L, 1000L);
    }

    public Controller get(String guildId, AccessReason reason) {
        Controller controller = null;
        boolean exists = controllers.containsKey(guildId);
        if(exists) {
            controller = controllers.get(guildId);
        } else {
            controller = new Controller();
            controller.guildId = guildId;
        }
        if(reason.shouldOverride())
            controller.lastAccessReason = reason;
        if(!exists && reason.shouldSave())
            controllers.put(guildId, controller);
        if(reason.shouldResetLifetime())
            controller.lifetime = 0L;
        return controller;
    }

    //TODO: JsonInitialization
    public Controller fromJson(String json, AccessReason reason) {
        return null;
    }

    public boolean isSaved(String guildId) {
        return controllers.containsKey(guildId);
    }

    /*
    Instance Starts Here.
     */

    //TODO: constructor.

    private String guildId;

    private AccessReason lastAccessReason = AccessReason.INITIALIZATION;

    private long lifetime = 0L;

    private Controller() {

    }

    public String getGuildId() {
        return guildId;
    }

    public Guild getGuild() {
        return Pulse.shardManager.getGuildById(guildId);
    }

    public AccessReason getLastAccessReason() {
        return lastAccessReason;
    }

    public long getLifetime() {
        return lifetime;
    }
}
