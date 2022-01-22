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
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ControllerFactory {

    //TODO: Make this class into a manager as well as a singleton object.
    // Let each controller store a "lastValidAccess" value that a timer can determine if it is viable to disconnect or not, then to delete or not (determined by a bots voice state).
    // Dont forget to include settings...
    // Audio Interface as well.
    // serializeable controller to save the current state on shutdown.
    // maximum allowed enques.

    //TODO: Store tracks as plain wrappers... for EVERY track.

    public static final long DEFAULT_TTL = 900; //15 minutes of cache time, post timeouts, when not in use at all.

    private static final Map<String, Controller> controllers = new HashMap<>();

    private static ControllersBackend backend;

    static {
        backend = new SqlBackend(Pulse.identityProperties.get(IdentityProperties.CONTROLLERS_SQL_BACKEND), "controllers");
        Pulse.scheduler.runLaterRepeating(() -> {
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
        backend.load().forEach((guildId, controller) -> controllers.put(guildId, fromJson(controller)));
    }

    public static boolean isCached(String guildId) {
        return controllers.containsKey(guildId);
    }

    public static Controller get(String guildId, boolean cache) {
        boolean exists = controllers.containsKey(guildId);
        Controller controller = exists ? controllers.get(guildId) : new Controller(guildId);
        if (cache)
            controllers.put(guildId, controller);
        return controller.setTtl(DEFAULT_TTL, ActivityCheck.CACHE);
    }

    public static void ifCached(String guildId, Consumer<Controller> controllerConsumer) {
        if(isCached(guildId))
            controllerConsumer.accept(get(guildId, false));
    }

    public static <T> Optional<T> ifCached(String guildId, Function<Controller, T> controllerFunction) {
        if(isCached(guildId))
            return Optional.of(controllerFunction.apply(get(guildId, false)));
        return Optional.empty();
    }

    public static String toJson(Controller controller) {
        JsonObject json = new JsonObject();
        json.addProperty("guildId", controller.getGuildId());
        AudioChannel connectedChannel = controller.getAudioManager().getConnectedChannel();
        json.addProperty("connectedChannel", connectedChannel == null ? "" : connectedChannel.getId());
        json.addProperty("lastChannelId", controller.getLastChannelId() == null ? "" : controller.getLastChannelId());
        json.addProperty("ttl", controller.ttl);
        json.addProperty("activityCheck", controller.activityCheck.toString());
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
        long ttl = controller.get("ttl").getAsLong();
        ActivityCheck activityCheck = ActivityCheck.valueOf(controller.get("activityCheck").getAsString());
        SortedPlaylist previousQueue = AudioFactory.fromPlaylistJson(controller.get("previousQueue").toString());
        SortedPlaylist queue = AudioFactory.fromPlaylistJson(controller.get("queue").toString());
        Track track = controller.get("track").toString().isEmpty() ? null : AudioFactory.fromTrackJson(controller.get("track").toString());
        long position = controller.get("position").getAsLong();
        int volume = controller.get("volume").getAsInt();
        boolean paused = controller.get("paused").getAsBoolean();
        boolean alone = controller.get("alone").getAsBoolean();
        return new Controller(guildId, previousQueue, queue, ttl, activityCheck, connectedChannel, lastChannelId, track, position, volume, paused, alone);
    }

}


























