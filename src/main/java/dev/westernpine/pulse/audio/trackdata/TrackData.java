package dev.westernpine.pulse.audio.trackdata;

import java.util.List;

public class TrackData {

    private final String title;
    private final String url;
    private final List<String> artists;
    private final long duration;

    public TrackData(String title, String url, List<String> artists, long duration) {
        this.title = title;
        this.url = url;
        this.artists = artists;
        this.duration = duration;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public List<String> getArtists() {
        return artists;
    }

    public String getArtistsString() {
        return String.join(", ", artists);
    }

    public long getDuration() {
        return duration;
    }

}
