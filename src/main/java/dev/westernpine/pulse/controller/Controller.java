package dev.westernpine.pulse.controller;

import com.sedmelluq.discord.lavaplayer.filter.PcmFilterFactory;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.westernpine.lib.audio.playlist.SortedPlaylist;
import dev.westernpine.lib.audio.track.Track;
import dev.westernpine.lib.object.TriState;
import dev.westernpine.pulse.Pulse;
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

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Controller {

    private final String guildId;
    protected long lifetime = 0;
    protected Status status = Status.CACHE;
    private String lastChannelId;
    private boolean premium = false;
    private Settings settings;

    private SortedPlaylist previousQueue;

    private SortedPlaylist queue;

    private AudioPlayer audioPlayer;

    private AudioReceiver audioReceiver;

    private AudioSender audioSender;

    private int volume = 7;

    private boolean alone = false;

    /*
    Other controller properties.
     */

    //False = Track, None = Off, True = Queue.
    private TriState repeating = TriState.NONE;

    private int lastTrack = 0;

    //TODO:
    // Initialize settings on initialization. (we need it for the activity checks anyways...) - Done!
    // update guild premium status on initialization. (requires management)

    Controller(String guildId) {
        this.guildId = guildId;
        this.settings = SettingsFactory.from(this);
        this.previousQueue = new SortedPlaylist(getGuild().getName() + "'s Previous Queue");
        this.queue = new SortedPlaylist(getGuild().getName() + "'s Queue");
    }

    public Controller(String guildId, long lifetime, Status status, String lastChannelId) {
        this.guildId = guildId;
        this.settings = SettingsFactory.from(this);
        this.previousQueue = new SortedPlaylist(getGuild().getName() + "'s Previous Queue");
        this.queue = new SortedPlaylist(getGuild().getName() + "'s Queue");
        this.lifetime = lifetime;
        this.status = status;
        this.lastChannelId = lastChannelId;
    }

    public Controller(String guildId, SortedPlaylist previousQueue, SortedPlaylist queue, long lifetime, Status status, String connectedChannel, String lastChannelId, Track track, long position, int volume, boolean paused, boolean alone, TriState repeating, int lastTrack) {
        this.guildId = guildId;
        this.settings = SettingsFactory.from(this);
        this.previousQueue = previousQueue;
        this.queue = queue;
        this.lifetime = lifetime;
        this.status = status;
        this.lastChannelId = lastChannelId;
        //todo: initialize
        if (this.getGuild().getGuildChannelById(connectedChannel) instanceof AudioChannel audioChannel) {
            this.connect(audioChannel);
            this.setVolume(volume);
            this.alone = alone;
            this.repeating = repeating;
            this.lastTrack = lastTrack;
            this.startTrack(track, false);
            this.setPaused(paused);
            this.getPlayingTrack().setPosition(position);
        }
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

    public String getGuildId() {
        return guildId;
    }

    public Guild getGuild() {
        return Pulse.shardManager.getGuildById(guildId);
    }

    public String getLastChannelId() {
        return this.lastChannelId;
    }

    public Controller setLastChannelId(String lastChannelId) {
        this.lastChannelId = lastChannelId;
        return this;
    }

    public Controller updateLastChannelId(String lastChannelId, Supplier<Boolean> conditionSupplier) {
        if (conditionSupplier.get())
            return setLastChannelId(lastChannelId);
        return this;
    }

    public boolean isPremium() {
        return premium;
    }

    public void setPremium(boolean premium) {
        this.premium = premium;
    }

    public Controller resetStatus(Status status) {
        this.lifetime = 0;
        this.status = status;
        return this;
    }

    public Controller setLifetime(long lifetime, Status status) {
        if (this.status == null)
            this.status = status;
        if (this.status.equals(status))
            this.lifetime = lifetime;
        return this;
    }

    public Settings getSettings() {
        return settings;
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

    public List<Member> getConnectedMembers() {
        AudioManager audioManager = getAudioManager();
        return audioManager.isConnected() ? audioManager.getConnectedChannel().getMembers() : List.of();
    }

    /**
     * @throws: IllegalArgumentException –
     * If the provided channel was null.
     * If the provided channel is not part of the Guild that the current audio connection is connected to.
     * UnsupportedOperationException – If audio is disabled due to an internal JDA error
     * net.dv8tion.jda.api.exceptions.InsufficientPermissionException –
     * If the currently logged in account does not have the Permission VOICE_CONNECT
     * If the currently logged in account does not have the Permission VOICE_MOVE_OTHERS and the user limit has been exceeded!
     */
    public Controller connect(Member member) throws IllegalArgumentException, InsufficientPermissionException {
        return connect(getAudioChannel(member));
    }

    /**
     * @throws: IllegalArgumentException –
     * If the provided channel was null.
     * If the provided channel is not part of the Guild that the current audio connection is connected to.
     * UnsupportedOperationException – If audio is disabled due to an internal JDA error
     * net.dv8tion.jda.api.exceptions.InsufficientPermissionException –
     * If the currently logged in account does not have the Permission VOICE_CONNECT
     * If the currently logged in account does not have the Permission VOICE_MOVE_OTHERS and the user limit has been exceeded!
     */
    public Controller connect(AudioChannel audioChannel) throws IllegalArgumentException, InsufficientPermissionException {
        AudioManager audioManager = getAudioManager();
        audioManager.setSelfDeafened(true);
        audioManager.setSpeakingMode(SpeakingMode.SOUNDSHARE);
        if (audioPlayer == null) {
            audioPlayer = Pulse.audioPlayerManager.createPlayer();
            audioPlayer.setVolume(getSettings().get(Setting.DEFAULT_VOLUME).toInteger());
            audioPlayer.addListener(new PlayerListener(this));
        }
        if (audioManager.getReceivingHandler() == null)
            audioManager.setReceivingHandler(this.audioReceiver = new AudioReceiver(this));
        if (audioManager.getSendingHandler() == null)
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

    public AudioTrack getPlayingTrack() {
        return audioPlayer.getPlayingTrack();
    }

    public void playTrack(AudioTrack track) {
        audioPlayer.playTrack(track);
    }

    public boolean startTrack(AudioTrack track, boolean noInterrupt) {
        return audioPlayer.startTrack(track, noInterrupt);
    }

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

    public boolean isPaused() {
        return audioPlayer.isPaused();
    }

    public void setPaused(boolean paused) {
        boolean changeState = audioPlayer.isPaused() != paused;
        if (!changeState)
            return;
        AudioTrack track = getPlayingTrack();
        if (track != null) {
            if (!track.isSeekable()) {
                changeState = false;
            }
        } else {
            paused = false;
        }
        if (changeState)
            audioPlayer.setPaused(paused);
    }

    public void clearQueues() {
        this.previousQueue.clear();
        this.queue.clear();
    }

    public SortedPlaylist getPreviousQueue() {
        return previousQueue;
    }

    public SortedPlaylist getQueue() {
        return queue;
    }

    public TriState getRepeating() {
        return repeating;
    }

    public void setRepeating(TriState repeating) {
        this.repeating = repeating;
    }

    public int getLastTrack() {
        return lastTrack;
    }

    public void setLastTrack(int lastTrack) {
        this.lastTrack = lastTrack;
    }

    public void destroy(EndCase endCase) {
        AudioManager audioManager = getAudioManager();
        audioManager.closeAudioConnection();
        audioManager.setSendingHandler(this.audioSender = null);
        audioManager.setReceivingHandler(this.audioReceiver = null);
        if (this.audioPlayer != null) {
            this.audioPlayer.destroy();
            this.audioPlayer = null;
        }
        this.clearQueues();
        this.setRepeating(TriState.NONE);
        this.lastTrack = 0;
    }
}
