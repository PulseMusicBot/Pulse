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
import dev.westernpine.pulse.controller.settings.setting.Setting;
import dev.westernpine.pulse.properties.IdentityProperties;
import net.dv8tion.jda.api.entities.AudioChannel;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ControllerFactory {

    //TODO:
    // maximum allowed enques.

    public static final long MAX_LIFETIME = 900; //15 minutes of cache time, post timeouts, when not in use at all.

    private static final Map<String, Controller> controllers = new HashMap<>();

    private static ControllersBackend backend;

    static {
        backend = new SqlBackend(Pulse.identityProperties.get(IdentityProperties.CONTROLLERS_SQL_BACKEND), "controllers");
        Pulse.scheduler.runLaterRepeating(() -> {
            Iterator<Map.Entry<String, Controller>> iterator = controllers.entrySet().iterator();
            while(iterator.hasNext()) {
                Map.Entry<String, Controller> entry = iterator.next();
                String guildId = entry.getKey();
                Controller controller = entry.getValue();
                try {
                    if(controller.getAudioManager().isConnected()) {
                        Status status = controller.status;
                        boolean changeableStatus = status.isnt(Status.INACTIVE) && status.isnt(Status.ALONE) && status.isnt(Status.PAUSED);
                        boolean playing = controller.getPlayingTrack() != null;
                        boolean alone = controller.getConnectedMembers().isEmpty();
                        boolean paused = controller.isPaused();
                        boolean activeWorthy = playing && !alone && !paused;
                        boolean tfs = controller.getSettings().get(Setting.TWENTRY_FOUR_SEVEN).toBoolean();
                        boolean endSession = false;
                        if(tfs || (status.isnt(Status.ACTIVE) && (status.is(Status.CACHE) || activeWorthy)))
                            controller.resetStatus(Status.ACTIVE);
                        else {
                            Status newStatus = Status.CACHE;
                            if(!playing) {
                                newStatus = Status.INACTIVE;
                                if(changeableStatus)
                                    controller.resetStatus(newStatus);
                                endSession = controller.setLifetime(controller.lifetime+1, newStatus).lifetime >= MAX_LIFETIME;
                            }
                            if(alone) {
                                newStatus = Status.ALONE;
                                if(changeableStatus)
                                    controller.resetStatus(newStatus);
                                endSession = controller.setLifetime(controller.lifetime+1, newStatus).lifetime >= MAX_LIFETIME;
                            }
                            if(paused) {
                                newStatus = Status.PAUSED;
                                if(changeableStatus)
                                    controller.resetStatus(newStatus);
                                endSession = controller.setLifetime(controller.lifetime+1, newStatus).lifetime >= MAX_LIFETIME;
                            }
                            if(endSession) {
                                controller.destroy(controller.status.getEndCase());
                                controller.resetStatus(Status.CACHE);
//                                continue; // This is technically the last statement in the loop... therefor unnecessary.
                            }
                        }
                    } else {
                        // Manage the cache state of a controller, and remove it when it's cache state times out.
                        if(controller.status == null || !controller.status.equals(Status.CACHE))
                            controller.resetStatus(Status.CACHE);
                        else if(controller.setLifetime(controller.lifetime+1, Status.CACHE).lifetime >= MAX_LIFETIME)
                            iterator.remove();
                    }
                } catch (Exception e) {
                    Try.of(() -> controller.destroy(EndCase.FATAL_ERROR));
                    iterator.remove();
                    e.printStackTrace();
                    System.out.println("A fatal error has occurred while managing the controller for guild: " + guildId);
                }
            }
        }, 0L, 1000L);
        Pulse.shutdownHook = () -> {
            Map<String, String> serialized = controllers
                    .entrySet()
                    .stream()
                    .map(entry -> EntryUtil.remapValue(entry, ControllerFactory::toJson))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            controllers.values().forEach(controller -> controller.destroy(EndCase.BOT_RESTART));
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
        return controller.setLifetime(0, Status.CACHE);
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
        json.addProperty("lifetime", controller.lifetime);
        json.addProperty("activityCheck", controller.status.toString());
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
        long lifetime = controller.get("lifetime").getAsLong();
        Status status = Status.valueOf(controller.get("activityCheck").getAsString());
        SortedPlaylist previousQueue = AudioFactory.fromPlaylistJson(controller.get("previousQueue").toString());
        SortedPlaylist queue = AudioFactory.fromPlaylistJson(controller.get("queue").toString());
        Track track = controller.get("track").toString().isEmpty() ? null : AudioFactory.fromTrackJson(controller.get("track").toString());
        long position = controller.get("position").getAsLong();
        int volume = controller.get("volume").getAsInt();
        boolean paused = controller.get("paused").getAsBoolean();
        boolean alone = controller.get("alone").getAsBoolean();
        return new Controller(guildId, previousQueue, queue, lifetime, status, connectedChannel, lastChannelId, track, position, volume, paused, alone);
    }

}
