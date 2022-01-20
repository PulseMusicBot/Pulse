package dev.westernpine.pulse.audio.trackdata;

import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import dev.westernpine.pulse.Pulse;

import java.util.List;

public class TrackDataFactory {

    public static TrackData from(String title, String url, List<String> artists, long duration) {
        return new TrackData(title, url, artists, duration);
    }

    public static TrackData from(AudioTrackInfo audioTrackInfo) {
        return new TrackData(audioTrackInfo.title, audioTrackInfo.uri, List.of(audioTrackInfo.author), audioTrackInfo.length);
    }

    public static TrackData fromJson(String json) {
        return Pulse.gson.fromJson(json, TrackData.class);
    }

    public static String toJson(TrackData trackData) {
        return Pulse.gson.toJson(trackData);
    }
}
