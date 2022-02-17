package dev.westernpine.lib.player.audio.track.userdata.request;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

public class RequestFactory {

    public static Request from(String request) {
        return new Request(request);
    }

    public static Request from(AudioTrackInfo audioTrackInfo) {
        return new Request(audioTrackInfo.title + " - " + audioTrackInfo.author);
    }

    public static JsonObject toJson(Request request) {
        if (request == null)
            return null;
        JsonObject json = new JsonObject();
        json.addProperty("request", request.getRequest());
        return json;
    }

    public static Request fromJson(JsonElement jsonElement) {
        if (jsonElement.isJsonNull())
            return null;
        JsonObject request = jsonElement.getAsJsonObject();
        return from(request.get("request").getAsString());
    }

}
