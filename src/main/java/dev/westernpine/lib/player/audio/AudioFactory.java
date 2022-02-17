package dev.westernpine.lib.player.audio;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.*;
import dev.westernpine.bettertry.Try;
import dev.westernpine.lib.player.audio.playlist.Playlist;
import dev.westernpine.lib.player.audio.playlist.PlaylistFactory;
import dev.westernpine.lib.player.audio.track.Track;
import dev.westernpine.lib.player.audio.track.TrackFactory;
import dev.westernpine.lib.player.audio.track.userdata.UserData;
import dev.westernpine.lib.player.audio.track.userdata.UserDataFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class AudioFactory {

    public static CompletableFuture<AudioItem> query(AudioPlayerManager audioPlayerManager, String query) {
        return CompletableFuture.supplyAsync(() -> {
            CompletableFuture<AudioItem> audioItemCompletableFuture = new CompletableFuture<>();
            audioPlayerManager.loadItemOrdered(audioPlayerManager, query, new AudioLoadResultHandler() {
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
            return Try.to(() -> audioItemCompletableFuture.get()).getUnchecked(); //GetUnchecked will pass the throwable to the completableFutureWrapper.
        });
    }

    public static AudioPlaylist toPlaylist(AudioItem audioItem) {
        if(audioItem instanceof AudioPlaylist audioPlaylist)
            return audioPlaylist;
        else if (audioItem instanceof AudioTrack audioTrack)
            return PlaylistFactory.from(audioTrack.getInfo().title, List.of(audioTrack), audioTrack, false);
        else
            return PlaylistFactory.from("Empty Playlist", List.of(), null, false);
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

    private static int hashAudioTrackInfo(AudioTrackInfo info) {
        return Objects.hash(info.uri, info.author, info.identifier, info.title);
    }

    private static int hashAudioPlaylist(AudioPlaylist audioPlaylist) {
        return Objects.hash(audioPlaylist.getName(), audioPlaylist.getTracks().stream().map(AudioFactory::hashAudioObject).toList());
    }

    public static int hashAudioObject(Object object) {
        if (object instanceof AudioPlaylist audioPlaylist)
            return hashAudioPlaylist(audioPlaylist);
        if (object instanceof AudioTrackInfo audioTrackInfo)
            return hashAudioTrackInfo(audioTrackInfo);
        if (object instanceof AudioTrack audioTrack)
            return hashAudioTrackInfo(audioTrack.getInfo());
        return object.hashCode();
    }

    public <T extends AudioItem> T applyUserData(T audioItem, UserData userData) {
        if (audioItem instanceof AudioTrack audioTrack)
            audioTrack.setUserData(userData);
        else if (audioItem instanceof AudioPlaylist audioPlaylist)
            audioPlaylist.getTracks().forEach(track -> track.setUserData(userData));
        return audioItem;
    }

}
