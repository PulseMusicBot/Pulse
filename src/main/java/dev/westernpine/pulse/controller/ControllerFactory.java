package dev.westernpine.pulse.controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.westernpine.bettertry.Try;
import dev.westernpine.lib.audio.AudioFactory;
import dev.westernpine.lib.audio.playlist.SortedPlaylist;
import dev.westernpine.lib.audio.track.Track;
import dev.westernpine.lib.util.EntryUtil;
import dev.westernpine.pulse.Pulse;
import dev.westernpine.pulse.controller.backend.ControllersBackend;
import dev.westernpine.pulse.controller.backend.SqlBackend;
import dev.westernpine.pulse.properties.IdentityProperties;
import net.dv8tion.jda.api.entities.AudioChannel;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ControllerFactory {

    //TODO: Make this class into a manager as well as a singleton object.
    // Let each controller store a "lastValidAccess" value that a timer can determine if it is viable to disconnect or not, then to delete or not (determined by a bots voice state).
    // Dont forget to include settings...
    // Audio Interface as well.
    // serializeable controller to save the current state on shutdown.
    // maximum allowed enques.

    //TODO: Store tracks as plain wrappers... for EVERY track.

    public static final long MAX_LIFETIME = 15 * 60;

    private static final Map<String, Controller> controllers = new HashMap<>();

    private static ControllersBackend backend;

    //TODO: Find guild ready listener, check if backend has guild, if so, initialize from there.
    // NO! This has so many ways it can fault... We need to just initialize the guild on controller factory initialization, and handle the fail from there.
    // Make seperate method for initializing controllers, as we need to wait for JDA to finish setting up first.

    static {
        backend = new SqlBackend(Pulse.identityProperties.get(IdentityProperties.CONTROLLERS_SQL_BACKEND), "controllers");
        Pulse.scheduler.runLaterRepeatingAsync(() -> {
            //TODO: Work on audio capabilities so we can track if controller needs to be deleted or cached.
        }, 0L, 1000L);
        Pulse.shutdownHook = () -> {
            Map<String, String> serialized = controllers
                    .entrySet()
                    .stream()
                    .map(entry -> EntryUtil.remapValue(entry, ControllerFactory::toJson))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            controllers.values().forEach(Controller::destroy);
            controllers.clear();
            backend.save(serialized);
            if (!backend.isClosed()) {
                System.out.println("System Shutdown >> Closing controller backend.");
                Try.of(() -> backend.close()).onFailure(Throwable::printStackTrace);
            }
            Pulse.shutdownHook.run();
        };
    }

    public static void initializeBackend() {
        Map<String, String> serialized = backend.load();
        //TODO: Do json stuff first to figure out how to process...
    }

    public static Controller get(String guildId, AccessReason reason) {
        Controller controller = null;
        boolean exists = controllers.containsKey(guildId);
        if (exists) {
            controller = controllers.get(guildId);
        } else {
            controller = new Controller(guildId);
        }
        if (reason.shouldOverride())
            controller.lastAccessReason = reason;
        if (!exists && reason.shouldSave())
            controllers.put(guildId, controller);
        if (reason.shouldResetLifetime())
            controller.lifetime = 0L;
        return controller;
    }

    public boolean isSaved(String guildId) {
        return controllers.containsKey(guildId);
    }

    public static String toJson(Controller controller) {
        JsonObject json = new JsonObject();
        json.addProperty("guildId", controller.getGuildId());
        AudioChannel connectedChannel = controller.getAudioManager().getConnectedChannel();
        json.addProperty("connectedChannel", connectedChannel == null ? "" : connectedChannel.getId());
        json.addProperty("lastChannelId", controller.getLastChannelId() == null ? "" : controller.getLastChannelId());
        json.addProperty("lastAccessReason", controller.getLastAccessReason().toString());
        json.addProperty("lifetime", controller.getLifetime());
        json.addProperty("previousQueue", AudioFactory.toJson(controller.getPreviousQueue()));
        json.addProperty("queue", AudioFactory.toJson(controller.getQueue()));
        json.addProperty("track", controller.getPlayingTrack() == null ? "" : AudioFactory.toJson(controller.getPlayingTrack()));
        json.addProperty("position", controller.getPlayingTrack() == null ? -1L : controller.getPlayingTrack().getPosition());
        json.addProperty("volume", controller.getVolume());
        json.addProperty("paused", controller.isPaused());
        json.addProperty("alone", controller.wasAlone());
        return json.toString();
    }

    //TODO: JsonInitialization
    public static Controller fromJson(String json) {
        JsonObject controller = JsonParser.parseString(json).getAsJsonObject();
        String guildId = controller.get("guildId").getAsString();
        String connectedChannel = controller.get("connectedChannel").getAsString();
        connectedChannel = connectedChannel.isEmpty() ? null : connectedChannel;
        String lastChannelId = controller.get("lastChannelId").getAsString();
        lastChannelId = lastChannelId.isEmpty() ? null : lastChannelId;
        AccessReason lastAccessReason = AccessReason.valueOf(controller.get("lastAccessReason").getAsString());
        long lifetime = controller.get("lifetime").getAsLong();
        SortedPlaylist previousQueue = AudioFactory.fromPlaylistJson(controller.get("previousQueue").toString());
        SortedPlaylist queue = AudioFactory.fromPlaylistJson(controller.get("queue").toString());
        Track track = controller.get("track").toString().isEmpty() ? null : AudioFactory.fromTrackJson(controller.get("track").toString());
        long position = controller.get("position").getAsLong();
        int volume = controller.get("volume").getAsInt();
        boolean paused = controller.get("paused").getAsBoolean();
        boolean alone = controller.get("alone").getAsBoolean();
        return new Controller(guildId, previousQueue, queue, lastAccessReason, lifetime).initialize();
    }

}


























