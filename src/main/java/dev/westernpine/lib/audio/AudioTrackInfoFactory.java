package dev.westernpine.lib.audio;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

public class AudioTrackInfoFactory {

    public static String toJson(AudioTrackInfo audioTrackInfo) {
        JsonObject json = new JsonObject();
        json.addProperty("title", audioTrackInfo.title);
        json.addProperty("author", audioTrackInfo.author);
        json.addProperty("length", audioTrackInfo.length);
        json.addProperty("identifier", audioTrackInfo.identifier);
        json.addProperty("isStream", audioTrackInfo.isStream);
        json.addProperty("uri", audioTrackInfo.uri);
        return json.toString();
    }

    public static AudioTrackInfo fromJson(String json) {
        JsonObject audioTrackInfo = JsonParser.parseString(json).getAsJsonObject();
        String title = audioTrackInfo.get("title").getAsString();
        String author = audioTrackInfo.get("author").getAsString();
        long length = audioTrackInfo.get("length").getAsLong();
        String identifier = audioTrackInfo.get("identifier").getAsString();
        boolean isStream = audioTrackInfo.get("isStream").getAsBoolean();
        String uri = audioTrackInfo.get("uri").getAsString();
        return new AudioTrackInfo(title, author, length, identifier, isStream, uri);
    }

}
