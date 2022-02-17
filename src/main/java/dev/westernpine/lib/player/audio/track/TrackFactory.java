package dev.westernpine.lib.player.audio.track;

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

    public static String toJson(Track track) {
        JsonObject json = new JsonObject();
        json.addProperty("sourceName", track.getSourceName());
        json.addProperty("audioTrackInfo", AudioTrackInfoFactory.toJson(track.getInfo()));
        json.addProperty("reference", track.getReference());
        json.addProperty("userData", UserDataFactory.toJson(UserDataFactory.from(track.getUserData())));
        return json.toString();
    }

    public static Track fromJson(AudioPlayerManager audioPlayerManager, String json) {
        JsonObject audioTrack = JsonParser.parseString(json).getAsJsonObject();
        String sourceName = audioTrack.get("sourceName").getAsString();
        AudioTrackInfo audioTrackInfo = AudioTrackInfoFactory.fromJson(audioTrack.get("audioTrackInfo").getAsString());
        String reference = audioTrack.get("reference").getAsString();
        UserData userData = UserDataFactory.fromJson(audioTrack.get("userData").getAsString());
        return from(audioPlayerManager, sourceName, audioTrackInfo, reference, userData);
    }

}
