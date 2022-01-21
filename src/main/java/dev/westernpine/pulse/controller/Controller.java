package dev.westernpine.pulse.controller;

import com.sedmelluq.discord.lavaplayer.filter.PcmFilterFactory;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;
import dev.westernpine.pulse.Pulse;
import dev.westernpine.pulse.audio.playlist.SortedPlaylist;
import dev.westernpine.pulse.controller.handlers.AudioReceiver;
import dev.westernpine.pulse.controller.handlers.AudioSender;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.audio.SpeakingMode;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Controller {

    private final String guildId;

    AccessReason lastAccessReason = AccessReason.INITIALIZATION;

    long lifetime = 0L;

    private SortedPlaylist previousQueue;

    private SortedPlaylist queue;

    private AudioPlayer audioPlayer;

    private AudioReceiver audioReceiver;

    private AudioSender audioSender;

    private int volume = 7;

    Controller(String guildId) {
        this.guildId = guildId;
        this.previousQueue = new SortedPlaylist(getGuild().getName() + "'s Previous Queue");
        this.queue = new SortedPlaylist(getGuild().getName() + "'s Queue");
    }

    public String getGuildId() {
        return guildId;
    }

    public Guild getGuild() {
        return Pulse.shardManager.getGuildById(guildId);
    }

    public AccessReason getLastAccessReason() {
        return lastAccessReason;
    }

    public long getLifetime() {
        return lifetime;
    }

    public AudioManager getAudioManager() {
        return getGuild().getAudioManager();
    }

    public User getSelfUser() {
        return getGuild().getJDA().getSelfUser();
    }

    public Member getSelfMember() {
        return getGuild().getSelfMember();
    }

    public GuildVoiceState getVoiceState(Member member) {
        return member.getVoiceState();
    }

    public AudioChannel getAudioChannel(Member member) {
        GuildVoiceState voiceState = getVoiceState(member);
        if (voiceState == null || !voiceState.inAudioChannel())
            return null;
        return voiceState.getChannel();
    }

    public Controller connect(Member member, SpeakingMode... speakingModes) throws InsufficientPermissionException {
        return connect(getAudioChannel(member), speakingModes);
    }

    public Controller connect(AudioChannel audioChannel, SpeakingMode... speakingModes) throws InsufficientPermissionException {
        AudioManager audioManager = getAudioManager();
        audioManager.setSelfDeafened(true);
        audioManager.setSpeakingMode(speakingModes.length == 0 ? List.of(SpeakingMode.SOUNDSHARE) : Arrays.asList(speakingModes));
        if(audioPlayer == null) {
            audioPlayer = Pulse.audioPlayerManager.createPlayer();
            audioPlayer.setVolume(7);
        }
        if(audioManager.getReceivingHandler() == null)
            audioManager.setReceivingHandler(this.audioReceiver = new AudioReceiver(this));
        if(audioManager.getSendingHandler() == null)
            audioManager.setSendingHandler(this.audioSender = new AudioSender(this));
        audioManager.openAudioConnection(audioChannel);
        return this;
    }

    public AudioPlayer getAudioPlayer() {
        return audioPlayer;
    }

    public AudioReceiver getAudioReceiver() {
        return audioReceiver;
    }

    public AudioSender getAudioSender() {
        return audioSender;
    }

    /**
     * @return Currently playing track
     */
    public AudioTrack getPlayingTrack() {
        return audioPlayer.getPlayingTrack();
    }

    /**
     * @param track The track to start playing
     */
    public void playTrack(AudioTrack track) {
        audioPlayer.playTrack(track);
    }

    /**
     * @param track       The track to start playing, passing null will stop the current track and return false
     * @param noInterrupt Whether to only start if nothing else is playing
     * @return True if the track was started
     */
    public boolean startTrack(AudioTrack track, boolean noInterrupt) {
        return audioPlayer.startTrack(track, noInterrupt);
    }

    /**
     * Stop currently playing track.
     */
    public void stopTrack() {
        audioPlayer.stopTrack();
    }

    public int getVolume() {
        return this.volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public void setFilterFactory(PcmFilterFactory factory) {
        audioPlayer.setFilterFactory(factory);
    }

    /**
     * @return Whether the player is paused
     */
    public boolean isPaused() {
        return audioPlayer.isPaused();
    }

    /**
     * @param value True to pause, false to resume
     */
    public void setPaused(boolean value) {
        audioPlayer.setPaused(value);
    }

    public void clearQueues() {
        this.previousQueue.clear();
        this.queue.clear();
    }

    /**
     * Destroy the player and stop playing track.
     */
    public void destroy() {
        AudioManager audioManager = getAudioManager();
        audioManager.closeAudioConnection();
        audioManager.setSendingHandler(this.audioSender = null);
        audioManager.setReceivingHandler(this.audioReceiver = null);
        this.audioPlayer.destroy();
        this.audioPlayer = null;
        this.clearQueues();
    }

    public SortedPlaylist getPreviousQueue() {
        return previousQueue;
    }

    public SortedPlaylist getQueue() {
        return queue;
    }
}
