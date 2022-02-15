package dev.westernpine.pulse.player.sources.wrapper;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.*;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class Wrapper implements AudioSourceManager {

    private AudioSourceManager sourceManager;

    public Wrapper(AudioSourceManager sourceManager) {
        this.sourceManager = sourceManager;
    }

    @Override
    public String getSourceName() {
        return sourceManager.getSourceName();
    }

    @Override
    public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference) {
        System.out.println(getSourceName() + " Reference: %s".formatted(reference.getIdentifier()));
        AudioItem item = sourceManager.loadItem(manager, reference);
        if(item == null) {
            System.out.println(getSourceName() + " Nothing found!");
        } else {
            System.out.println(getSourceName() + " Result found!");
            System.out.println(getSourceName() + " Result source: " + (item instanceof AudioTrack track ? track.getSourceManager().getSourceName() : ((AudioPlaylist)item).getTracks().get(0).getSourceManager().getSourceName()));
        }
        return item;
    }

    @Override
    public boolean isTrackEncodable(AudioTrack track) {
        return sourceManager.isTrackEncodable(track);
    }

    @Override
    public void encodeTrack(AudioTrack track, DataOutput output) throws IOException {
        sourceManager.encodeTrack(track, output);
    }

    @Override
    public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) throws IOException {
        return sourceManager.decodeTrack(trackInfo, input);
    }

    @Override
    public void shutdown() {
        sourceManager.shutdown();
    }
}
