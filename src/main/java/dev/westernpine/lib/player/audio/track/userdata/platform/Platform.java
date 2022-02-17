package dev.westernpine.lib.player.audio.track.userdata.platform;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.*;

import java.util.function.Function;
import java.util.function.Supplier;

public interface Platform {

    public String getOfficialName();

    public String getSourceName();

    public String getSearchPrefix();

    public String getSimilarSearchPrefix();

    public Supplier<? extends AudioPlayerManager> getAudioPlayerManager();

    public Supplier<? extends AudioSourceManager> getAudioSourceManager();

    public default boolean canSearch() {
        return getSearchPrefix() != null && !getSearchPrefix().isBlank();
    }

    public default boolean canSearchSimilar() {
        return getSearchPrefix() != null && !getSearchPrefix().isBlank();
    }

    public default AudioItem search(AudioTrack audioTrack) {
        AudioTrackInfo audioTrackInfo = audioTrack.getInfo();
        return search(audioTrackInfo.author, audioTrackInfo.title);
    }

    public default AudioItem search(String author, String title) {
        return search("%s - %s".formatted(author, title));
    }

    public default AudioItem search(String query) {
        String search = getSearchPrefix();
        if(!query.startsWith(search))
            query = search + query;
        AudioSourceManager sourceManager = getAudioSourceManager().get();
        return sourceManager.loadItem(getAudioPlayerManager().get(), new AudioReference(query, null));
    }

    public default AudioItem searchSimilar(AudioTrack audioTrack) {
        AudioTrackInfo audioTrackInfo = audioTrack.getInfo();
        return searchSimilar(audioTrackInfo.author, audioTrackInfo.title);
    }

    public default AudioItem searchSimilar(String author, String title) {
        return searchSimilar("%s - %s".formatted(author, title));
    }

    public default AudioItem searchSimilar(String query) {
        String similar = getSimilarSearchPrefix();
        if(!query.startsWith(similar))
            query = similar + query;
        AudioSourceManager sourceManager = getAudioSourceManager().get();
        return sourceManager.loadItem(getAudioPlayerManager().get(), new AudioReference(query, null));
    }

}
