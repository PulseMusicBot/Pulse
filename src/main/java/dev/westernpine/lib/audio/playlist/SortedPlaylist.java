package dev.westernpine.lib.audio.playlist;

import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.westernpine.lib.audio.AudioFactory;
import dev.westernpine.lib.audio.track.userdata.UserData;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

public class SortedPlaylist extends LinkedList<AudioTrack> implements AudioPlaylist {

    private final String name;
    private final boolean isSearchResult;
    private AudioTrack selectedTrack;

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

    public SortedPlaylist applyUserData(UserData userData) {
        forEach(audioTrack -> audioTrack.setUserData(userData));
        return this;
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

    public SortedPlaylist setTracks(AudioTrack... tracks) {
        this.clear();
        this.addAll(Arrays.asList(tracks));
        if (!this.contains(selectedTrack))
            selectedTrack = null;
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

    public int getSelectedTrackIndex() {
        int index = -1;
        if (selectedTrack != null) {
            int i = 0;
            for (AudioTrack track : getTracks()) {
                if (track == selectedTrack) {
                    index = i;
                    break;
                }
            }
        }
        return index;
    }

    public void move(int index, int to) {
        add(to > index ? to - 1 : to, remove(index));
    }

    @Override
    public int hashCode() {
        return AudioFactory.hashAudioObject(this);
    }

    @Override
    public boolean equals(Object object) {
        boolean isInt = object instanceof Integer;
        if (!isInt && !(object instanceof AudioPlaylist))
            return false;
        return this.hashCode() == (isInt ? (int) object : AudioFactory.hashAudioObject(object));
    }
}
