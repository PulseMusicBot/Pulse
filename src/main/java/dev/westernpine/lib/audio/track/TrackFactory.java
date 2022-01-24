package dev.westernpine.lib.audio.track;

import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.InternalAudioTrack;
import dev.westernpine.lib.audio.track.userdata.UserData;

public class TrackFactory {

    public static Track from(AudioTrackInfo audioTrackInfo) {
        return new Track(audioTrackInfo);
    }

    public static Track from(AudioTrackInfo audioTrackInfo, UserData userData) {
        Track track = new Track(audioTrackInfo);
        track.setUserData(userData);
        return track;
    }

    public static Track from(InternalAudioTrack internalAudioTrack) {
        if(internalAudioTrack instanceof Track track)
            return track;
        return new Track(internalAudioTrack);
    }

    //No json functions needed! This can be done with AudioFactory functions.

}
