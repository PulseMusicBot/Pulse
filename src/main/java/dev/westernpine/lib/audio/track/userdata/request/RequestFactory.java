package dev.westernpine.lib.audio.track.userdata.request;

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

    public static String toJson(Request request) {
        if(request == null)
            return "";
        JsonObject json = new JsonObject();
        json.addProperty("request", request.getRequest());
        return json.toString();
    }

    public static Request fromJson(String json) {
        if(json.isEmpty())
            return null;
        JsonObject request = JsonParser.parseString(json).getAsJsonObject();
        return from(request.get("request").getAsString());
    }

}
