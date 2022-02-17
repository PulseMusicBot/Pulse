package dev.westernpine.lib.player.audio.track;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.InternalAudioTrack;
import dev.westernpine.lib.player.audio.AudioTrackInfoFactory;
import dev.westernpine.lib.player.audio.track.userdata.UserData;
import dev.westernpine.lib.player.audio.track.userdata.UserDataFactory;

public class TrackFactory {

    public static Track from(AudioPlayerManager audioPlayerManager, AudioTrack audioTrack) {
        Track trackResult = audioTrack instanceof Track track
                ? track
                : audioTrack instanceof InternalAudioTrack internalAudioTrack
                ? new Track(audioPlayerManager, internalAudioTrack)
                : new Track(audioPlayerManager, audioTrack.getSourceManager().getSourceName(), audioTrack.getInfo(), audioTrack.getInfo().uri);
        trackResult.setUserData(audioTrack.getUserData());
        return trackResult;
    }

    public static Track from(AudioPlayerManager audioPlayerManager, String sourceName, AudioTrackInfo originalAudioTrackInfo, String reference) {
        return new Track(audioPlayerManager, sourceName, originalAudioTrackInfo, reference);
    }

    public static Track from(AudioPlayerManager audioPlayerManager, String sourceName, AudioTrackInfo originalAudioTrackInfo, String reference, UserData userData) {
        Track track = from(audioPlayerManager, sourceName, originalAudioTrackInfo, reference);
        track.setUserData(userData);
        return track;
    }

    public static JsonObject toJson(Track track) {
        JsonObject json = new JsonObject();
        json.addProperty("sourceName", track.getSourceName());
        json.add("audioTrackInfo", AudioTrackInfoFactory.toJson(track.getInfo()));
        json.addProperty("reference", track.getReference());
        json.add("userData", UserDataFactory.toJson(UserDataFactory.from(track.getUserData())));
        return json;
    }

    public static Track fromJson(AudioPlayerManager audioPlayerManager, JsonElement jsonElement) {
        if(jsonElement.isJsonNull())
            return null;
        JsonObject audioTrack = jsonElement.getAsJsonObject();
        String sourceName = audioTrack.get("sourceName").getAsString();
        AudioTrackInfo audioTrackInfo = AudioTrackInfoFactory.fromJson(audioTrack.get("audioTrackInfo"));
        String reference = audioTrack.get("reference").getAsString();
        UserData userData = UserDataFactory.fromJson(audioTrack.get("userData"));
        return from(audioPlayerManager, sourceName, audioTrackInfo, reference, userData);
    }

}
