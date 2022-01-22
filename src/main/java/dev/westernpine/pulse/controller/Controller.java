package dev.westernpine.pulse.controller;

import com.sedmelluq.discord.lavaplayer.filter.PcmFilterFactory;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.westernpine.lib.audio.track.Track;
import dev.westernpine.pulse.Pulse;
import dev.westernpine.lib.audio.playlist.SortedPlaylist;
import dev.westernpine.pulse.controller.handlers.audio.AudioReceiver;
import dev.westernpine.pulse.controller.handlers.audio.AudioSender;
import dev.westernpine.pulse.controller.handlers.player.PlayerListener;
import dev.westernpine.pulse.controller.settings.Settings;
import dev.westernpine.pulse.controller.settings.SettingsFactory;
import dev.westernpine.pulse.controller.settings.setting.Setting;
import net.dv8tion.jda.api.audio.SpeakingMode;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.managers.AudioManager;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Controller {

    private final String guildId;

    private String lastChannelId;

    AccessReason lastAccessReason = AccessReason.INITIALIZATION;

    long lifetime = 0L;

    private Settings settings;

    private SortedPlaylist previousQueue;

    private SortedPlaylist queue;

    private AudioPlayer audioPlayer;

    private AudioReceiver audioReceiver;

    private AudioSender audioSender;

    private int volume = 7;

    private boolean alone = false;

    Controller(String guildId) {
        this.guildId = guildId;
        this.previousQueue = new SortedPlaylist(getGuild().getName() + "'s Previous Queue");
        this.queue = new SortedPlaylist(getGuild().getName() + "'s Queue");
    }

    public Controller(String guildId, SortedPlaylist previousQueue, SortedPlaylist queue, AccessReason accessReason, long lifetime) {
        this.guildId = guildId;
        this.previousQueue = previousQueue;
        this.queue = queue;
        this.lastAccessReason = accessReason;
        this.lifetime = lifetime;
    }

    public Controller initialize(@Nullable String connectedChannel,
                                 @Nullable String lastChannelId,
                                 @Nullable Track track,
                                 long position,
                                 int volume,
                                 boolean paused,
                                 boolean alone) {
        //todo
        return this;
    }

    public Controller perform(Consumer<Controller> controllerConsumer) {
        controllerConsumer.accept(this);
        return this;
    }

    public Controller perform(Runnable task) {
        task.run();
        return this;
    }

    public <R> R map(Function<Controller, R> controllerMapper) {
        return controllerMapper.apply(this);
    }

    public String getLastChannelId() {
        return this.lastChannelId;
    }

    public Controller setLastChannelId(String lastChannelId) {
        this.lastChannelId = lastChannelId;
        return this;
    }

    public Controller updateLastChannelId(String lastChannelId, Supplier<Boolean> conditionSupplier) {
        if(conditionSupplier.get())
            return setLastChannelId(lastChannelId);
        return this;
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

    public Settings getSettings() {
        return settings == null ? settings = SettingsFactory.from(this) : settings;
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
            audioPlayer.setVolume(getSettings().get(Setting.DEFAULT_VOLUME).toInteger());
            audioPlayer.addListener(new PlayerListener(this));
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

    public boolean wasAlone() {
        return this.alone;
    }

    public void setAlone(boolean alone) {
        this.alone = alone;
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
