package dev.westernpine.pulse.controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.westernpine.bettertry.Try;
import dev.westernpine.lib.object.TriState;
import dev.westernpine.lib.player.audio.playlist.Playlist;
import dev.westernpine.lib.player.audio.playlist.PlaylistFactory;
import dev.westernpine.lib.player.audio.track.Track;
import dev.westernpine.lib.player.audio.track.TrackFactory;
import dev.westernpine.lib.util.EntryUtil;
import dev.westernpine.pulse.Pulse;
import dev.westernpine.pulse.controller.backend.ControllersBackend;
import dev.westernpine.pulse.controller.backend.SqlBackend;
import dev.westernpine.pulse.controller.settings.setting.Setting;
import dev.westernpine.pulse.properties.IdentityProperties;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.entities.ISnowflake;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static dev.westernpine.pulse.logging.Logger.logger;

public class ControllerFactory {

    public static final long MAX_LIFETIME = 900; //15 minutes of cache time, post timeouts, when not in use at all.

    private static final Map<String, Controller> controllers = new HashMap<>();

    private static ControllersBackend backend;

    static {
        backend = new SqlBackend(Pulse.identityProperties.get(IdentityProperties.CONTROLLERS_SQL_BACKEND), "controllers");
        Pulse.scheduler.runLaterRepeating(() -> {
            Iterator<Map.Entry<String, Controller>> iterator = controllers.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Controller> entry = iterator.next();
                String guildId = entry.getKey();
                Controller controller = entry.getValue();
                try {
                    if (controller.isConnected()) {
                        Status status = controller.status;
                        boolean playing = controller.getPlayingTrack() != null;
                        boolean alone = controller.getConnectedMembers().isEmpty();
                        boolean paused = controller.isPaused();
                        boolean activeWorthy = playing && !alone && !paused;
                        boolean tfs = controller.getSettings().get(Setting.TWENTY_FOUR_SEVEN).toBoolean();
                        boolean endSession = false;
                        if (tfs || (status.isnt(Status.ACTIVE) && (status.is(Status.CACHE) || activeWorthy)))
                            controller.resetStatus(Status.ACTIVE);
                        else {
                            Status newStatus = Status.CACHE;
                            if (!playing)
                                endSession = controller.incrementLifetimeWithStatusIfCurrentIsChangeable(Status.INACTIVE);
                            if (alone)
                                endSession = controller.incrementLifetimeWithStatusIfCurrentIsChangeable(Status.ALONE);
                            if (paused)
                                endSession = controller.incrementLifetimeWithStatusIfCurrentIsChangeable(Status.PAUSED);
                            if (endSession) {
                                controller.destroy(controller.status.getEndCase());
                                controller.resetStatus(Status.CACHE);
//                                continue; // This is technically the last statement in the loop... therefor "continue" is unnecessary.
                            }
                        }
                    } else {
                        // Manage the cache state of a controller, and remove it when it's cache state times out.
                        if (controller.status == null || !controller.status.equals(Status.CACHE))
                            controller.resetStatus(Status.CACHE);
                        else if (controller.setLifetime(controller.lifetime + 1, Status.CACHE).lifetime >= MAX_LIFETIME)
                            iterator.remove();
                    }
                } catch (Exception e) {
                    Try.to(() -> controller.destroy(EndCase.FATAL_ERROR));
                    iterator.remove();
                    e.printStackTrace();
                    logger.severe("An error has occurred while managing the controller for guild: " + guildId);
                }
            }
        }, 0L, 1000L);
        Pulse.shutdownHooks.addFirst(() -> {
            if (backend.isAvailable()) {
                Map<String, String> serialized = controllers
                        .entrySet()
                        .stream()
                        .filter(entry -> entry.getValue().getAudioPlayer() != null)
                        .map(entry -> Try.to(() -> EntryUtil.remapValue(entry, ControllerFactory::toJson)).orElse(null))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                controllers.values().forEach(controller -> Try.to(() -> controller.destroy(EndCase.BOT_RESTART)));
                controllers.clear();
                backend.save(serialized);
                if (!backend.isClosed()) {
                    logger.info("Closing controller backend.");
                    Try.to(() -> backend.close()).onFailure(Throwable::printStackTrace);
                }
            }
        });
        if (backend.isAvailable()) {
            backend.load().forEach((guildId, controller) ->
                    Try.to(() -> controllers.put(guildId, fromJson(controller)))
                            .onFailure(Throwable::printStackTrace)
                            .onFailure(throwable -> logger.warning("Unable to initialize guild controller %s from backend.".formatted(guildId)))
                            .onSuccess(con -> logger.fine("Initialized guild controller %s from backend.".formatted(guildId))));
            backend.clear();
        }
    }

    public static boolean isCached(String guildId) {
        return controllers.containsKey(guildId);
    }

    public static Map<String, Controller> getControllers() {
        return controllers;
    }

    public static Controller get(String guildId, boolean cache) {
        boolean exists = controllers.containsKey(guildId);
        Controller controller = exists ? controllers.get(guildId) : new Controller(guildId);
        if (cache)
            controllers.put(guildId, controller);
        return controller.setLifetime(0, Status.CACHE);
    }

    public static void ifCached(String guildId, Consumer<Controller> controllerConsumer) {
        if (isCached(guildId))
            controllerConsumer.accept(get(guildId, false));
    }

    public static <T> Optional<T> ifCached(String guildId, Function<Controller, T> controllerFunction) {
        if (isCached(guildId))
            return Optional.of(controllerFunction.apply(get(guildId, false)));
        return Optional.empty();
    }

    public static String toJson(Controller controller) {
        JsonObject json = new JsonObject();
        json.addProperty("guildId", controller.getGuildId());
        Optional<AudioChannel> connectedChannel = controller.getConnectedChannel();
        json.addProperty("lastChannelId", controller.getLastChannelId() == null ? "" : controller.getLastChannelId());
        json.addProperty("lifetime", controller.lifetime);
        json.addProperty("status", controller.status.toString());
        boolean isConnected = controller.getAudioPlayer() != null;
        json.addProperty("isConnected", isConnected);
        if (isConnected) {
            json.addProperty("connectedChannel", connectedChannel.map(ISnowflake::getId).orElse(""));
            json.addProperty("previousQueue", PlaylistFactory.toJson(controller.getPreviousQueue()).toString());
            json.addProperty("queue", PlaylistFactory.toJson(controller.getQueue()).toString());
            json.addProperty("track", controller.getPlayingTrack() == null ? "" : TrackFactory.toJson(TrackFactory.from(null, controller.getPlayingTrack())).toString());
            json.addProperty("position", controller.getPlayingTrack() == null || !controller.getPlayingTrack().isSeekable() ? -1L : controller.getPlayingTrack().getPosition());
            json.addProperty("volume", controller.getVolume());
            json.addProperty("paused", controller.isPaused());
            json.addProperty("alone", controller.wasAlone());
            json.addProperty("repeating", controller.getRepeating().toString());
            json.addProperty("lastTrack", controller.getLastTrack());
        }
        return json.toString();
    }

    public static Controller fromJson(String json) {
        JsonObject controller = JsonParser.parseString(json).getAsJsonObject();
        String guildId = controller.get("guildId").getAsString();
        String lastChannelId = controller.get("lastChannelId").getAsString();
        lastChannelId = lastChannelId.isEmpty() ? null : lastChannelId;
        long lifetime = controller.get("lifetime").getAsLong();
        Status status = Status.valueOf(controller.get("status").getAsString());
        boolean isConnected = controller.get("isConnected").getAsBoolean();
        if (!isConnected) {
            return new Controller(guildId, lifetime, status, lastChannelId);
        } else {
            String connectedChannel = controller.get("connectedChannel").getAsString();
            connectedChannel = connectedChannel.isEmpty() ? null : connectedChannel;
            Playlist previousQueue = PlaylistFactory.fromJson(Pulse.audioPlayerManager, controller.get("previousQueue").getAsString());
            Playlist queue = PlaylistFactory.fromJson(Pulse.audioPlayerManager, controller.get("queue").getAsString());
            Track track = controller.get("track").getAsString().isEmpty() ? null : TrackFactory.fromJson(Pulse.audioPlayerManager, controller.get("track").getAsString());
            long position = controller.get("position").getAsLong();
            int volume = controller.get("volume").getAsInt();
            boolean paused = controller.get("paused").getAsBoolean();
            boolean alone = controller.get("alone").getAsBoolean();
            TriState repeating = TriState.valueOf(controller.get("repeating").getAsString());
            int lastTrack = controller.get("lastTrack").getAsInt();
            return new Controller(guildId, previousQueue, queue, lifetime, status, connectedChannel, lastChannelId, track, position, volume, paused, alone, repeating, lastTrack);
        }
    }

}
