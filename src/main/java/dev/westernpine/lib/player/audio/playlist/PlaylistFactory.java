package dev.westernpine.lib.player.audio.playlist;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.westernpine.lib.player.audio.track.TrackFactory;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class PlaylistFactory {

    public static Playlist from(AudioPlaylist audioPlaylist) {
        if (audioPlaylist instanceof Playlist playlist)
            return playlist;
        else
            return new Playlist(audioPlaylist);
    }

    public static AudioPlaylist from(String name, Collection<AudioTrack> tracks, AudioTrack selectedTrack, boolean isSearchResult) {
        return new Playlist(name, tracks, selectedTrack, isSearchResult);
    }

    public static AudioPlaylist from(String name, Collection<AudioTrack> tracks, AudioTrack selectedTrack, boolean isSearchResult, String creator, String image, String uri, String type) {
        return new Playlist(name, tracks, selectedTrack, isSearchResult, creator, image, uri, type);
    }

    public static JsonObject toJson(AudioPlaylist audioPlaylist) {
        JsonObject json = new JsonObject();
        json.addProperty("name", audioPlaylist.getName());
        json.addProperty("isSearchResult", audioPlaylist.isSearchResult());
        JsonArray jsonTracks = new JsonArray(audioPlaylist.getTracks().size());
        if (audioPlaylist.getSelectedTrack() != null) {
            int index = -1;
            for (AudioTrack audioTrack : audioPlaylist.getTracks()) {
                index++;
                jsonTracks.add(TrackFactory.toJson(TrackFactory.from(null, audioTrack)));
                if (!json.has("selectedTrackIndex") && audioPlaylist.getSelectedTrack() == audioTrack)
                    json.addProperty("selectedTrackIndex", index);
            }
        } else {
            for (AudioTrack audioTrack : audioPlaylist.getTracks())
                jsonTracks.add(TrackFactory.toJson(TrackFactory.from(null, audioTrack)));
        }
        json.add("tracks", jsonTracks);
        return json;
    }

    public static Playlist fromJson(AudioPlayerManager audioPlayerManager, JsonElement jsonElement) {
        if(jsonElement.isJsonNull())
            return null;
        JsonObject audioPlaylist = jsonElement.getAsJsonObject();
        String name = audioPlaylist.get("name").getAsString();
        boolean isSearchResult = audioPlaylist.get("isSearchResult").getAsBoolean();
        AudioTrack selectedTrack = null;
        List<AudioTrack> audioTracks = new LinkedList<>();
        JsonArray jsonTracks = audioPlaylist.get("tracks").getAsJsonArray();
        if (audioPlaylist.has("selectedTrackIndex")) {
            int selectedTrackIndex = audioPlaylist.get("selectedTrackIndex").getAsInt();
            int index = -1;
            for (JsonElement jsonTrack : audioPlaylist.get("tracks").getAsJsonArray()) {
                index++;
                AudioTrack audioTrack = TrackFactory.fromJson(audioPlayerManager, jsonTrack);
                if (index == selectedTrackIndex)
                    selectedTrack = audioTrack;
                audioTracks.add(audioTrack);
            }
        } else {
            for (JsonElement jsonTrack : audioPlaylist.get("tracks").getAsJsonArray())
                audioTracks.add(TrackFactory.fromJson(audioPlayerManager, jsonTrack));
        }
        return new Playlist(name, audioTracks, selectedTrack, isSearchResult);
    }
}
