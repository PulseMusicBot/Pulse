package dev.westernpine.lib.player.audio.track;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.*;
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor;
import dev.westernpine.bettertry.Try;
import dev.westernpine.lib.player.audio.AudioFactory;
import dev.westernpine.lib.player.audio.track.userdata.UserDataFactory;
import dev.westernpine.lib.player.audio.track.userdata.platform.PlatformManager;

public class Track extends DelegatedAudioTrack {

    private final AudioPlayerManager audioPlayerManager;
    private final String sourceName;
    private final AudioTrackInfo originalAudioTrackInfo;
    private final String reference;
    private InternalAudioTrack internalAudioTrack;

    public Track(AudioPlayerManager audioPlayerManager, InternalAudioTrack internalAudioTrack) {
        super(internalAudioTrack.getInfo());
        this.audioPlayerManager = audioPlayerManager;
        this.sourceName = internalAudioTrack.getSourceManager().getSourceName();
        this.originalAudioTrackInfo = internalAudioTrack.getInfo();
        this.reference = internalAudioTrack.getInfo().uri;
        this.internalAudioTrack = internalAudioTrack;
    }

    public Track(AudioPlayerManager audioPlayerManager, String sourceName, AudioTrackInfo originalAudioTrackInfo, String reference) {
        super(originalAudioTrackInfo);
        this.audioPlayerManager = audioPlayerManager;
        this.sourceName = sourceName;
        this.originalAudioTrackInfo = originalAudioTrackInfo;
        this.reference = reference;
    }

    public AudioPlayerManager getAudioPlayerManager() {
        return audioPlayerManager;
    }

    public String getSourceName() {
        return sourceName;
    }

    public AudioTrackInfo getOriginalAudioTrackInfo() {
        return originalAudioTrackInfo;
    }

    public String getReference() {
        return reference;
    }

    public InternalAudioTrack getInternalAudioTrack() {
        return internalAudioTrack;
    }

    @Override
    public void process(LocalAudioTrackExecutor executor) throws Exception {
        if (this.internalAudioTrack == null)
            if ((this.internalAudioTrack = getAudioTrack()) == null)
                throw new RuntimeException("Unable to resolve track: " + TrackFactory.toJson(this));
        this.processDelegate(this.internalAudioTrack, executor);
    }

    @Override
    public AudioTrackInfo getInfo() {
        return this.originalAudioTrackInfo;
    }

    @Override
    public String getIdentifier() {
        return this.originalAudioTrackInfo.identifier;
    }

    @Override
    public boolean isSeekable() {
        return getAudioTrack() != null && this.internalAudioTrack.isSeekable();
    }

    @Override
    public long getDuration() {
        return this.internalAudioTrack != null ? this.internalAudioTrack.getDuration() : this.originalAudioTrackInfo.length;
    }

    @Override
    public AudioSourceManager getSourceManager() {
        return PlatformManager.getFromSource(this.sourceName).getAudioSourceManager().get();
    }

    @Override
    public Track makeClone() {
        Track track = new Track(audioPlayerManager, sourceName, originalAudioTrackInfo, reference);
        if (this.internalAudioTrack != null)
            track.internalAudioTrack = (InternalAudioTrack) this.internalAudioTrack.makeClone();
        track.setUserData(this.getUserData());
        return track;
    }

    public InternalAudioTrack getAudioTrack() {
        if(this.internalAudioTrack != null)
            return this.internalAudioTrack;

        AudioTrack audioTrack = Try.to(() -> AudioFactory.query(audioPlayerManager, reference).get())
                .map(AudioFactory::toTrack)
                .orElse(null);
        if (audioTrack != null
                && (!this.equals(audioTrack) || !(audioTrack instanceof Track)) //Check to make sure we aren't resolving the same object.
                && audioTrack instanceof InternalAudioTrack internalAudioTrack) {
            this.internalAudioTrack = internalAudioTrack;
        }

        if (this.internalAudioTrack != null)
            return this.internalAudioTrack;
        audioTrack = AudioFactory.toTrack(UserDataFactory.from(this.getUserData()).preferredPlatform().search(this));
        return this.internalAudioTrack = audioTrack instanceof InternalAudioTrack internalAudioTrack ? internalAudioTrack : null;
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
        return this.internalAudioTrack == null ? originalHashEquals : originalHashEquals || hash == AudioFactory.hashAudioObject(this.internalAudioTrack.getInfo());
    }

}
