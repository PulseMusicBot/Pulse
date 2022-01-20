package dev.westernpine.pulse.audio.request;

import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import dev.westernpine.pulse.Pulse;

import java.util.function.Supplier;

public class Platform {
//    YOUTUBE("ytsearch:"),
//    SOUNDCLOUD("scsearch:"),
//    ;

    public static final Platform YOUTUBE = new Platform("ytsearch:", () -> Pulse.youtubeAudioSourceManager);
    public static final Platform SOUNDCLOUD = new Platform("scsearch:", () -> Pulse.soundCloudAudioSourceManager);

    public static Platform defaultPlatform() {
        return YOUTUBE;
    }

    private String prefix;

    private Supplier<AudioSourceManager> audioSourceManagerSupplier;

    Platform(String prefix, Supplier<AudioSourceManager> audioSourceManagerSupplier) {
        this.prefix = prefix;
        this.audioSourceManagerSupplier = audioSourceManagerSupplier;
    }

    public String getPrefix() {
        return prefix;
    }

    public AudioSourceManager getAudioSourceManager() {
        return audioSourceManagerSupplier.get();
    }
}
