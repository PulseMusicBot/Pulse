package dev.westernpine.lib.audio.track;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.DelegatedAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.InternalAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor;
import dev.westernpine.bettertry.Try;
import dev.westernpine.lib.audio.AudioFactory;
import dev.westernpine.lib.audio.track.userdata.UserDataFactory;

public class Track extends DelegatedAudioTrack {

    /*
    TODO:
     - Add playerManager.
     - Add source manager name. (getSourceManager will get source manager from player manaager from name.)
     - Add search reference.
     */

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
        if (this.audioTrack == null)
            this.audioTrack = getAudioTrack();
        if (this.audioTrack == null)
            throw new RuntimeException("Unable to resolve track: " + AudioFactory.toJson(this));
        this.processDelegate(this.audioTrack, executor);
    }

    @Override
    public AudioTrackInfo getInfo() {
        return this.originalAudioTrackInfo;
    }

    @Override
    public String getIdentifier() {
        return this.getInfo().identifier != null ? this.getInfo().identifier : this.getInfo().uri;
    }

    @Override
    public boolean isSeekable() {
        return getAudioTrack() != null && this.audioTrack.isSeekable();
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
        if(this.audioTrack != null)
            return this.audioTrack;

        AudioTrack audioTrack = Try.to(() -> AudioFactory.query(this.getIdentifier()).get())
                .map(AudioFactory::toTrack)
                .orElse(null);
        if (audioTrack != null
                && (!this.equals(audioTrack) || !(audioTrack instanceof Track)) //Check to make sure we aren't resolving the same object.
                && audioTrack instanceof InternalAudioTrack internalAudioTrack) {
            this.audioTrack = internalAudioTrack;
        }

        if (this.audioTrack != null)
            return this.audioTrack;
        return this.audioTrack = UserDataFactory.from(this.getUserData()).preferredPlatform().getAudioTrackFactory().apply(this);
    }

    public InternalAudioTrack getAudioTrack(AudioPlayerManager audioPlayerManager) {
        if(this.audioTrack != null)
            return this.audioTrack;

        AudioTrack audioTrack = Try.to(() -> AudioFactory.query(this.getIdentifier()).get())
                .map(AudioFactory::toTrack)
                .orElse(null);
        if (audioTrack != null
                && (!this.equals(audioTrack) || !(audioTrack instanceof Track)) //Check to make sure we aren't resolving the same object.
                && audioTrack instanceof InternalAudioTrack internalAudioTrack) {
            this.audioTrack = internalAudioTrack;
        }

//        if (this.audioTrack != null)
            return this.audioTrack;
//        return this.audioTrack = UserDataFactory.from(this.getUserData()).preferredPlatform().getAudioTrackFactory().apply(this);
    }

    @Override
    public int hashCode() {
        return AudioFactory.hashAudioObject(originalAudioTrackInfo); // Not getInfo() because that can change when we initialize it with an audio track object.
    }

    @Override
    public boolean equals(Object object) {
        boolean isInt = object instanceof Integer;
        if (!isInt && !(object instanceof AudioTrack) && !(object instanceof AudioTrackInfo))
            return false;
        int hash = isInt ? (int) object : AudioFactory.hashAudioObject(object);
        boolean originalHashEquals = hash == AudioFactory.hashAudioObject(originalAudioTrackInfo);
        return this.audioTrack == null ? originalHashEquals : originalHashEquals || hash == AudioFactory.hashAudioObject(this.audioTrack.getInfo());
    }

}
