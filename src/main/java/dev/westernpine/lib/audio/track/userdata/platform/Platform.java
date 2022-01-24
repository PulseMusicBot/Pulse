package dev.westernpine.lib.audio.track.userdata.platform;

import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.*;
import dev.westernpine.lib.audio.track.Track;
import dev.westernpine.pulse.Pulse;

import java.util.function.Function;
import java.util.function.Supplier;

public class Platform {

    public static final Platform YOUTUBE = new Platform("YouTube", "ytsearch:", () -> Pulse.youtubeAudioSourceManager, track -> {
        AudioTrackInfo audioTrackInfo = track.getInfo();
        String query = audioTrackInfo.title + " - " + audioTrackInfo.author;
        AudioItem audioItem = Pulse.youtubeSearchProvider.loadSearchResult(query, info -> new YoutubeAudioTrack(info, Pulse.youtubeAudioSourceManager));
        if (audioItem == AudioReference.NO_TRACK)
            return null;
        if (audioItem instanceof AudioPlaylist playlist) {
            return playlist.getTracks().isEmpty() ? null : (InternalAudioTrack) playlist.getTracks().get(0);
        } else if (audioItem instanceof InternalAudioTrack audioTrack) {
            return audioTrack;
        }
        return null;
    });

    public static final Platform SOUNDCLOUD = new Platform("SoundCloud", "scsearch:", () -> Pulse.soundCloudAudioSourceManager, track -> {
        AudioTrackInfo audioTrackInfo = track.getInfo();
        String query = "scsearch:" + audioTrackInfo.title + " - " + audioTrackInfo.author;
        AudioItem audioItem = Pulse.soundCloudAudioSourceManager.loadItem(Pulse.audioPlayerManager, new AudioReference(query, null));
        if (audioItem == AudioReference.NO_TRACK)
            return null;
        if (audioItem instanceof AudioPlaylist playlist) {
            return playlist.getTracks().isEmpty() ? null : (InternalAudioTrack) playlist.getTracks().get(0);
        } else if (audioItem instanceof InternalAudioTrack audioTrack) {
            return audioTrack;
        }
        return null;
    });

    private final String name;

    private final String prefix;

    private final Supplier<AudioSourceManager> audioSourceManagerSupplier;

    private final Function<Track, InternalAudioTrack> audioTrackFactory;

    Platform(String name, String prefix, Supplier<AudioSourceManager> audioSourceManagerSupplier, Function<Track, InternalAudioTrack> audioTrackFactory) {
        this.name = name;
        this.prefix = prefix;
        this.audioSourceManagerSupplier = audioSourceManagerSupplier;
        this.audioTrackFactory = audioTrackFactory;
    }

    public String getName() {
        return name;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public AudioSourceManager getAudioSourceManager() {
        return this.audioSourceManagerSupplier.get();
    }

    public Function<Track, InternalAudioTrack> getAudioTrackFactory() {
        return this.audioTrackFactory;
    }

}
