package dev.westernpine.pulse.audio.request;

import dev.westernpine.bettertry.Try;
import dev.westernpine.pulse.Pulse;
import dev.westernpine.pulse.audio.trackdata.TrackData;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Objects;

public class RequestFactory {
    public static Request from(String request) {
        return new Request(request, Arrays.stream(Platform.class.getDeclaredFields())
                .filter(field -> Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers()))
                .map(field -> Try.of(() -> field.get(null)).orElse(null))
                .filter(Objects::nonNull)
                .filter(obj ->  obj instanceof Platform)
                .map(obj -> (Platform)obj)
                .filter(platform -> request.startsWith(platform.getPrefix()))
                .findAny()
                .orElse(Platform.defaultPlatform()));
    }

    public static Request from(String request, Platform platform) {
        return new Request(request, platform);
    }

    public static Request from(TrackData trackData, Platform platform) {
        return new Request(trackData.getTitle() + " - " + trackData.getArtistsString(), platform);
    }

    public static Request fromJson(String json) {
        return Pulse.gson.fromJson(json, Request.class);
    }

    public static String toJson(Request request) {
        return Pulse.gson.toJson(request);
    }

}
