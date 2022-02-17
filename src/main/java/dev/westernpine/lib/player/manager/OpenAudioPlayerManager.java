package dev.westernpine.lib.player.manager;

import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import org.apache.commons.collections4.list.UnmodifiableList;

import java.util.ArrayList;
import java.util.List;

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
