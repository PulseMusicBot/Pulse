package dev.westernpine.lib.player.source;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class WrappedSource implements AudioSourceManager {

    private AudioSourceManager audioSourceManager;

    public WrappedSource(AudioSourceManager audioSourceManager) {
        this.audioSourceManager = audioSourceManager;
    }

    @Override
    public String getSourceName() {
        return audioSourceManager.getSourceName();
    }

    @Override
    public AudioItem loadItem(AudioPlayerManager audioPlayerManager, AudioReference audioReference) {
        System.out.println(getSourceName() + " Identifier: " + (audioReference.identifier));
        AudioItem item = audioSourceManager.loadItem(audioPlayerManager, audioReference);
        System.out.println(getSourceName() + " Is Item Null: " + (item == null));
        System.out.println(getSourceName() + " Is Item AudioReference.NO_TRACK: " + (item == AudioReference.NO_TRACK));
        System.out.println();
        return item;
    }

    @Override
    public boolean isTrackEncodable(AudioTrack audioTrack) {
        return audioSourceManager.isTrackEncodable(audioTrack);
    }

    @Override
    public void encodeTrack(AudioTrack audioTrack, DataOutput dataOutput) throws IOException {
        audioSourceManager.encodeTrack(audioTrack, dataOutput);
    }

    @Override
    public AudioTrack decodeTrack(AudioTrackInfo audioTrackInfo, DataInput dataInput) throws IOException {
        return audioSourceManager.decodeTrack(audioTrackInfo, dataInput);
    }

    @Override
    public void shutdown() {
        audioSourceManager.shutdown();
    }
}
