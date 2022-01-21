package dev.westernpine.pulse.audio.track;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.InternalAudioTrack;
import dev.westernpine.pulse.audio.track.userdata.UserData;
import dev.westernpine.pulse.audio.track.userdata.UserDataFactory;
import dev.westernpine.pulse.audio.track.userdata.platform.Platform;
import dev.westernpine.pulse.audio.track.userdata.request.Request;
import dev.westernpine.pulse.audio.track.userdata.requester.Requester;

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
        return new Track(internalAudioTrack);
    }

    //No json functions needed! This can be done with AudioFactory functions.

}
