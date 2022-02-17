package dev.westernpine.lib.player.manager;

import com.sedmelluq.discord.lavaplayer.player.*;
import com.sedmelluq.discord.lavaplayer.remote.RemoteAudioTrackExecutor;
import com.sedmelluq.discord.lavaplayer.remote.RemoteNodeManager;
import com.sedmelluq.discord.lavaplayer.remote.RemoteNodeRegistry;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.ProbingAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.*;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpConfigurable;
import com.sedmelluq.discord.lavaplayer.tools.io.MessageInput;
import com.sedmelluq.discord.lavaplayer.tools.io.MessageOutput;
import com.sedmelluq.discord.lavaplayer.track.*;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioTrackExecutor;
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor;
import com.sedmelluq.lava.common.tools.DaemonThreadFactory;
import com.sedmelluq.lava.common.tools.ExecutorTools;
import org.apache.commons.collections4.list.UnmodifiableList;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity.FAULT;
import static com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity.SUSPICIOUS;

public class OpenAudioPlayerManager extends DefaultAudioPlayerManager {

    private final List<AudioSourceManager> sourceManagers;


    /**
     * Create a new instance
     */
    public OpenAudioPlayerManager() {
        super();
        sourceManagers = new ArrayList<>();
    }

    @Override
    public void registerSourceManager(AudioSourceManager sourceManager) {
        super.registerSourceManager(sourceManager);
        this.sourceManagers.add(sourceManager);
    }

    public List<AudioSourceManager> getSourceManagers() {
        return UnmodifiableList.unmodifiableList(sourceManagers);
    }

    public AudioSourceManager source(String sourceName) {
        return sourceManagers.stream()
                .filter(source -> source.getSourceName() != null)
                .filter(source -> source.getSourceName().equals(sourceName))
                .findFirst()
                .orElse(null);
    }
}
