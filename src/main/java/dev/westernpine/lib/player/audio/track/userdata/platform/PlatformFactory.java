package dev.westernpine.lib.player.audio.track.userdata.platform;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;

import java.util.function.Function;
import java.util.function.Supplier;

public class PlatformFactory {

    public static Platform from(String officialName, String sourceName, String searchPrefix, String similarSearchPrefix, Supplier<? extends AudioPlayerManager> audioPlayerManagerSupplier, Function<String, AudioSourceManager> audioSourceManagerFunction) {
        return new Platform() {
            @Override
            public String getOfficialName() {
                return officialName;
            }

            @Override
            public String getSourceName() {
                return sourceName;
            }

            @Override
            public String getSearchPrefix() {
                return searchPrefix;
            }

            @Override
            public String getSimilarSearchPrefix() {
                return similarSearchPrefix;
            }

            @Override
            public Supplier<? extends AudioPlayerManager> getAudioPlayerManager() {
                return audioPlayerManagerSupplier;
            }

            @Override
            public Supplier<AudioSourceManager> getAudioSourceManager() {
                return () -> audioSourceManagerFunction.apply(getSourceName());
            }
        };
    }

    public static Platform from(String officialName, String sourceName, String searchPrefix, String similarSearchPrefix, Supplier<? extends AudioPlayerManager> audioPlayerManagerSupplier, Supplier<AudioSourceManager> audioSourceManagerSupplier) {
        return new Platform() {
            @Override
            public String getOfficialName() {
                return officialName;
            }

            @Override
            public String getSourceName() {
                return sourceName;
            }

            @Override
            public String getSearchPrefix() {
                return searchPrefix;
            }

            @Override
            public String getSimilarSearchPrefix() {
                return similarSearchPrefix;
            }

            @Override
            public Supplier<? extends AudioPlayerManager> getAudioPlayerManager() {
                return audioPlayerManagerSupplier;
            }

            @Override
            public Supplier<AudioSourceManager> getAudioSourceManager() {
                return audioSourceManagerSupplier;
            }
        };
    }

}
