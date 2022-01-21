package dev.westernpine.lib.audio.track;

import com.sedmelluq.discord.lavaplayer.track.*;
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor;
import dev.westernpine.bettertry.Try;
import dev.westernpine.lib.audio.track.userdata.UserDataFactory;
import dev.westernpine.lib.audio.AudioFactory;

public class Track extends DelegatedAudioTrack {

    private final AudioTrackInfo originalAudioTrackInfo;
    private InternalAudioTrack audioTrack;

    public Track(AudioTrackInfo originalAudioTrackInfo) {
        super(originalAudioTrackInfo);
        this.originalAudioTrackInfo = originalAudioTrackInfo;
    }

    public Track(InternalAudioTrack audioTrack) {
        super(audioTrack.getInfo());
        this.originalAudioTrackInfo = audioTrack.getInfo();
        this.audioTrack = audioTrack;
    }

    @Override
    public void process(LocalAudioTrackExecutor executor) throws Exception {
        if(this.audioTrack == null)
            this.audioTrack = getAudioTrack();
        if(this.audioTrack == null)
            throw new RuntimeException("Unable to resolve track: " + AudioFactory.toJson(this));
        this.processDelegate(this.audioTrack, executor);
    }

    @Override
    public AudioTrackInfo getInfo() {
        return this.audioTrack != null ? this.audioTrack.getInfo() : this.originalAudioTrackInfo;
    }

    @Override
    public String getIdentifier() {
        return this.getInfo().identifier;
    }

    @Override
    public boolean isSeekable() {
        return this.audioTrack != null ? this.audioTrack.isSeekable() : !this.getInfo().isStream;
    }

    @Override
    public Track makeClone() {
        Track track = new Track(originalAudioTrackInfo);
        if (this.audioTrack != null)
            track.audioTrack = (InternalAudioTrack) this.audioTrack.makeClone();
        track.setUserData(this.getUserData());
        return track;
    }

    public InternalAudioTrack getAudioTrack() {
        if(this.getInfo().uri != null) {
            AudioItem audioItem = Try.of(() -> AudioFactory.query(this.getInfo().uri).get()).orElse(null);
            if(audioItem != null && AudioFactory.toTrack(audioItem) instanceof InternalAudioTrack internalAudioTrack) {
                this.audioTrack = internalAudioTrack;
            }
        }
        if (this.audioTrack != null)
            return this.audioTrack;
        return UserDataFactory.from(this.getUserData()).preferredPlatform().getAudioTrackFactory().apply(this);
    }
}
