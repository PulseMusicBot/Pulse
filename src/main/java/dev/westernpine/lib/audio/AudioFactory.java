package dev.westernpine.lib.audio;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.*;
import dev.westernpine.bettertry.Try;
import dev.westernpine.lib.audio.playlist.SortedPlaylist;
import dev.westernpine.lib.audio.track.Track;
import dev.westernpine.lib.audio.track.userdata.UserDataFactory;
import dev.westernpine.pulse.Pulse;
import dev.westernpine.lib.audio.track.TrackFactory;
import dev.westernpine.lib.audio.track.userdata.UserData;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class AudioFactory {

    public <T extends AudioItem> T applyUserData(T audioItem, UserData userData) {
        if(audioItem instanceof AudioTrack audioTrack)
            audioTrack.setUserData(userData);
        else if (audioItem instanceof AudioPlaylist audioPlaylist)
            audioPlaylist.getTracks().forEach(track -> track.setUserData(userData));
        return audioItem;
    }

    public static CompletableFuture<AudioItem> query(String query) {
        return CompletableFuture.supplyAsync(() -> {
           CompletableFuture<AudioItem> audioItemCompletableFuture = new CompletableFuture<>();
           Pulse.audioPlayerManager.loadItemOrdered(Pulse.audioPlayerManager, query, new AudioLoadResultHandler() {
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
           return Try.of(() -> audioItemCompletableFuture.get()).getUnchecked(); //GetUnchecked will pass the throwable to the completableFutureWrapper.
        });
    }

    public static SortedPlaylist toPlaylist(AudioItem audioItem) {
        if (audioItem instanceof AudioPlaylist playlist)
            return new SortedPlaylist(playlist);
        else {
            AudioTrack audioTrack = (AudioTrack) audioItem;
            return new SortedPlaylist(audioTrack.getInfo().title, List.of(audioTrack), audioTrack, false);
        }
    }

    public static SortedPlaylist toPlaylist(String title, AudioItem... audioItems) {
        SortedPlaylist playlist = new SortedPlaylist(title);
        for (AudioItem audioItem : audioItems) {
            if (audioItem instanceof AudioPlaylist audioPlaylist)
                playlist.addAll(audioPlaylist.getTracks());
            else
                playlist.add((AudioTrack) audioItem);
        }
        return playlist;
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

    //We use the track object here, as it is an extension of AudioTrack, and it's already an implementation.

    public static String toJson(AudioTrack audioTrack) {
        JsonObject json = new JsonObject();
        json.addProperty("audioTrackInfo", AudioTrackInfoFactory.toJson(audioTrack.getInfo()));
        json.addProperty("userData", UserDataFactory.toJson(UserDataFactory.from(audioTrack.getUserData())));
        return json.toString();
    }

    public static String toJson(AudioPlaylist audioPlaylist) {
        JsonObject json = new JsonObject();
        json.addProperty("name", audioPlaylist.getName());
        json.addProperty("isSearchResult", audioPlaylist.isSearchResult());
        JsonArray jsonTracks = new JsonArray(audioPlaylist.getTracks().size());
        if(audioPlaylist.getSelectedTrack() != null) {
            int index = -1;
            for(AudioTrack audioTrack : audioPlaylist.getTracks()) {
                index++;
                jsonTracks.add(toJson(audioTrack));
                if(!json.has("selectedTrackIndex") && audioPlaylist.getSelectedTrack() == audioTrack)
                    json.addProperty("selectedTrackIndex", index);
            }
        } else {
            for(AudioTrack audioTrack : audioPlaylist.getTracks())
                jsonTracks.add(toJson(audioTrack));
        }
        json.add("tracks", jsonTracks);
        return json.toString();
    }

    public static Track fromTrackJson(String json) {
        JsonObject audioTrack = JsonParser.parseString(json).getAsJsonObject();
        AudioTrackInfo audioTrackInfo = AudioTrackInfoFactory.fromJson(audioTrack.get("audioTrackInfo").getAsString());
        UserData userData = UserDataFactory.fromJson(audioTrack.get("userData").getAsString());
        return TrackFactory.from(audioTrackInfo, userData);
    }

    public static SortedPlaylist fromPlaylistJson(String json) {
        JsonObject audioPlaylist = JsonParser.parseString(json).getAsJsonObject();
        String name = audioPlaylist.get("name").getAsString();
        boolean isSearchResult = audioPlaylist.get("isSearchResult").getAsBoolean();
        AudioTrack selectedTrack = null;
        List<AudioTrack> audioTracks = new LinkedList<>();
        JsonArray jsonTracks = audioPlaylist.get("tracks").getAsJsonArray();
        if(audioPlaylist.has("selectedTrackIndex")) {
            int selectedTrackIndex = audioPlaylist.get("selectedTrackIndex").getAsInt();
            int index = -1;
            for(JsonElement jsonTrack : audioPlaylist.get("tracks").getAsJsonArray()) {
                index++;
                AudioTrack audioTrack = fromTrackJson(jsonTrack.getAsString());
                if(index == selectedTrackIndex)
                    selectedTrack = audioTrack;
                audioTracks.add(audioTrack);
            }
        } else {
            for(JsonElement jsonTrack : audioPlaylist.get("tracks").getAsJsonArray())
                audioTracks.add(fromTrackJson(jsonTrack.getAsString()));
        }
        return new SortedPlaylist(name, audioTracks, selectedTrack, isSearchResult);
    }

    private static int hashAudioTrackInfo(AudioTrackInfo info) {
        return Objects.hash(info.uri, info.author, info.identifier, info.title);
    }

    private static int hashAudioPlaylist(AudioPlaylist audioPlaylist) {
        return Objects.hash(audioPlaylist.getName(), audioPlaylist.getTracks().stream().map(AudioFactory::hashAudioObject).toList());
    }

    public static int hashAudioObject(Object object) {
        if(object instanceof AudioPlaylist audioPlaylist)
            return hashAudioPlaylist(audioPlaylist);
        if(object instanceof AudioTrackInfo audioTrackInfo)
            return hashAudioTrackInfo(audioTrackInfo);
        if(object instanceof AudioTrack audioTrack)
            return hashAudioTrackInfo(audioTrack.getInfo());
        return object.hashCode();
    }

}
