package dev.westernpine.lib.audio.playlist;

import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.BasicAudioPlaylist;
import dev.westernpine.lib.audio.AudioFactory;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class SortedPlaylist extends LinkedList<AudioTrack> implements AudioPlaylist {

    private final String name;

    private AudioTrack selectedTrack;

    private final boolean isSearchResult;

    public SortedPlaylist(String name) {
        super();
        this.name = name;
        this.isSearchResult = false;
    }

    public SortedPlaylist(String name, Collection<AudioTrack> tracks) {
        super(tracks);
        this.name = name;
        this.isSearchResult = false;
    }

    public SortedPlaylist(String name, Collection<AudioTrack> tracks, AudioTrack selectedTrack, boolean isSearchResult) {
        super(tracks);
        this.name = name;
        this.selectedTrack = selectedTrack;
        this.isSearchResult = isSearchResult;
    }

    public SortedPlaylist(AudioPlaylist audioPlaylist) {
        super(audioPlaylist.getTracks());
        this.name = audioPlaylist.getName();
        this.selectedTrack = audioPlaylist.getSelectedTrack();
        this.isSearchResult = audioPlaylist.isSearchResult();
    }

    /**
     * @return Name of the playlist
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * @return List of tracks in the playlist
     */
    @Override
    public LinkedList<AudioTrack> getTracks() {
        return this;
    }

    /**
     * @return Track that is explicitly selected, may be null. This same instance occurs in the track list.
     */
    @Override
    public AudioTrack getSelectedTrack() {
        return this.selectedTrack;
    }

    /**
     * @return True if the playlist was created from search results.
     */
    @Override
    public boolean isSearchResult() {
        return this.isSearchResult;
    }



    @Override
    public int hashCode() {
        return AudioFactory.hashAudioObject(this);
    }

    @Override
    public boolean equals(Object object) {
        boolean isInt = object instanceof Integer;
        if(!isInt && !(object instanceof AudioPlaylist))
            return false;
        return this.hashCode() == (isInt ? (int) object : AudioFactory.hashAudioObject(object));
    }
}
