package dev.westernpine.pulse.audio.track.userdata.platform;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.*;
import dev.westernpine.pulse.Pulse;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PlatformFactory {

    private static final Map<String, Platform> platforms = new HashMap<>();

    static {
        platforms.put(Platform.YOUTUBE.getName(), Platform.YOUTUBE);
        platforms.put(Platform.SOUNDCLOUD.getName(), Platform.SOUNDCLOUD);
    }

    public static Map<String, Platform> getPlatforms() {
        return platforms;
    }

    public static Platform get(String name) {
        return Optional.ofNullable(platforms.get(name)).orElse(defaultPlatform());
    }

    public static Platform defaultPlatform() {
        return get("YouTube");
    }

    public static String toJson(Platform platform) {
        if(platform == null)
            return "";
        JsonObject json = new JsonObject();
        json.addProperty("platform", platform.getName());
        return json.toString();
    }

    public static Platform fromJson(String json) {
        if(json.isEmpty())
            return null;
        JsonObject platform = JsonParser.parseString(json).getAsJsonObject();
        return get(platform.get("platform").getAsString());
    }


}
