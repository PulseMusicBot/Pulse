package dev.westernpine.pulse.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.westernpine.bettertry.Try;
import dev.westernpine.pulse.Pulse;
import dev.westernpine.pulse.audio.request.Platform;
import dev.westernpine.pulse.audio.request.Request;
import dev.westernpine.pulse.audio.request.RequestFactory;
import dev.westernpine.pulse.audio.trackdata.TrackData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class AudioFactory {

    public static List<CompletableFuture<AudioTrack>> getAudioTracks(Collection<TrackData> trackDataList, Platform platform) {
        return trackDataList.stream().map(trackData -> getAudioTrack(trackData, platform)).toList();
    }

    /*
    Lavaplayer REQUIRES tracks to be resolved in-sync as it handles everything async already.
    Trying to resolve tracks async will cause memory leaks and numerous errors.
    This is why we wrap the resolution in so many try's, suppliers, and completable futures.

    First we create the async completable future with a supplier that will be called when #get is called.
    Then because #call throws a checked exception, we need to handle it.
    Then we bring the execution back in-sync with the call method.
    Then we go ahead and try and resolve all tracks at the same time (since this is being done in a completable future, we will need all tracks when #get is called).
     */
    public static CompletableFuture<List<AudioTrack>> getAudioTracksCollectively(Collection<TrackData> trackDataList, Platform platform, long timeout, TimeUnit timeUnit) {
        return CompletableFuture.supplyAsync(() -> Try.of(() -> Pulse.scheduler.call(() -> trackDataList.stream()
                                .map(trackData -> getAudioTrack(trackData, platform))
                                .map(futureTrack -> Try.of(() -> futureTrack.get(timeout, timeUnit)).orElse(null))
                                .toList())
                        .get())
                .orElse(new ArrayList<>()));
    }

    public static CompletableFuture<AudioTrack> getAudioTrack(TrackData trackData, Platform platform) {
        new YoutubeAudioTrack(null, null);
        CompletableFuture<AudioTrack> audioTrackCompletableFuture = new CompletableFuture<AudioTrack>();
        Pulse.audioPlayerManager.loadItem(RequestFactory.from(trackData, platform).toString(), new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                audioTrackCompletableFuture.complete(track);

            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                if (playlist.getTracks().isEmpty()) {
                    audioTrackCompletableFuture.complete(null);
                    return;
                }
                audioTrackCompletableFuture.complete(playlist.getTracks().get(0));
            }

            @Override
            public void noMatches() {
                audioTrackCompletableFuture.complete(null);
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                audioTrackCompletableFuture.completeExceptionally(exception);
            }
        });
        return audioTrackCompletableFuture;
    }

    public static CompletableFuture<AudioItem> getAudioItem(Request request) {
        CompletableFuture<AudioItem> audioItemCompletableFuture = new CompletableFuture<AudioItem>();
        Pulse.audioPlayerManager.loadItem(request.toString(), new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                audioItemCompletableFuture.complete(track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                audioItemCompletableFuture.complete(playlist);
            }

            @Override
            public void noMatches() {
                audioItemCompletableFuture.complete(null);
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                audioItemCompletableFuture.completeExceptionally(exception);
            }
        });
        return audioItemCompletableFuture;
    }

    public static AudioPlaylist toPlaylist(AudioItem audioItem) {
        if (audioItem instanceof AudioPlaylist playlist)
            return playlist;
        else {
            AudioTrack audioTrack = (AudioTrack) audioItem;
            return new AudioPlaylist() {
                @Override
                public String getName() {
                    return audioTrack.getInfo().title;
                }

                @Override
                public List<AudioTrack> getTracks() {
                    return List.of(audioTrack);
                }

                @Override
                public AudioTrack getSelectedTrack() {
                    return audioTrack;
                }

                @Override
                public boolean isSearchResult() {
                    return false;
                }
            };
        }
    }

    public static AudioPlaylist toPlaylist(String title, AudioItem... audioItems) {
        List<AudioTrack> audioTracks = new LinkedList<>();
        for (AudioItem audioItem : audioItems) {
            if (audioItem instanceof AudioPlaylist audioPlaylist)
                audioTracks.addAll(audioPlaylist.getTracks());
            else
                audioTracks.add((AudioTrack) audioItem);
        }
        return new AudioPlaylist() {
            @Override
            public String getName() {
                return title;
            }

            @Override
            public List<AudioTrack> getTracks() {
                return audioTracks;
            }

            @Override
            public AudioTrack getSelectedTrack() {
                return null;
            }

            @Override
            public boolean isSearchResult() {
                return false;
            }
        };
    }

    public static AudioTrack toTrack(AudioItem audioItem) {
        if (audioItem instanceof AudioTrack audioTrack)
            return audioTrack;
        else {
            AudioPlaylist audioPlaylist = ((AudioPlaylist) audioItem);
            if (audioPlaylist.getTracks().isEmpty())
                return null;
            return audioPlaylist.getTracks().get(0);
        }
    }

}
