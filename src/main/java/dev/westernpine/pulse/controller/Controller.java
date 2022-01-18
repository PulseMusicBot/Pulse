package dev.westernpine.pulse.controller;

import com.google.gson.JsonObject;
import dev.westernpine.lib.serialization.JsonSerializable;
import dev.westernpine.pulse.Pulse;
import net.dv8tion.jda.api.entities.Guild;

import java.io.Serializable;
import java.util.*;

public class Controller implements JsonSerializable {

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
            controller = new Controller(guildId);
        }
        if(reason.shouldOverride())
            controller.lastAccessReason = reason;
        if(!exists && reason.shouldSave())
            controllers.put(guildId, controller);
        if(reason.shouldResetLifetime())
            controller.lifetime = 0L;
        return controller;
    }

    public boolean isSaved(String guildId) {
        return controllers.containsKey(guildId);
    }

    /*
    Instance Starts Here.
     */

    private String guildId;

    private AccessReason lastAccessReason = AccessReason.INITIALIZATION;

    private long lifetime = 0L;

    private Controller(String guildId) {

    }

    public Controller() {}

    public void assertInit() {
        if(Objects.isNull(this.guildId))
            throw new InitializationException();
    }

    @Override
    public String serialize() {
        assertInit();
        return null;
    }

    @Override
    public void deserialize(String json) {
    }

    public String getGuildId() {
        assertInit();
        return guildId;
    }

    public Guild getGuild() {
        assertInit();
        return Pulse.shardManager.getGuildById(guildId);
    }

    public AccessReason getLastAccessReason() {
        assertInit();
        return lastAccessReason;
    }

    public long getLifetime() {
        assertInit();
        return lifetime;
    }
}
