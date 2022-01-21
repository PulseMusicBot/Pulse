package dev.westernpine.pulse.controller;

import com.sedmelluq.discord.lavaplayer.filter.PcmFilterFactory;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;
import dev.westernpine.pulse.Pulse;
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

    //TODO: Make this class into a manager as well as a singleton object.
    // Let each controller store a "lastValidAccess" value that a timer can determine if it is viable to disconnect or not, then to delete or not (determined by a bots voice state).
    // Dont forget to include settings...
    // Audio Interface as well.
    // serializeable controller to save the current state on shutdown.
    // maximum allowed enques.

    //TODO: Store tracks as plain wrappers... for EVERY track.

    public static final long MAX_LIFETIME = 15 * 60;

    private static Map<String, Controller> controllers = new HashMap<>();

    static {
        Pulse.scheduler.runLaterRepeatingAsync(() -> {
            //TODO: Work on audio capabilities so we can track if controller needs to be deleted or cached.
        }, 0L, 1000L);
    }

    public static Controller get(String guildId, AccessReason reason) {
        Controller controller = null;
        boolean exists = controllers.containsKey(guildId);
        if (exists) {
            controller = controllers.get(guildId);
        } else {
            controller = new Controller();
            controller.guildId = guildId;
        }
        if (reason.shouldOverride())
            controller.lastAccessReason = reason;
        if (!exists && reason.shouldSave())
            controllers.put(guildId, controller);
        if (reason.shouldResetLifetime())
            controller.lifetime = 0L;
        return controller;
    }

    public String toJson(Controller controller) {
        return null;
    }

    //TODO: JsonInitialization
    public Controller fromJson(String json, AccessReason reason) {
        return null;
    }

    public boolean isSaved(String guildId) {
        return controllers.containsKey(guildId);
    }

    /*
    Instance Starts Here.
     */

    //TODO: constructor.

    private String guildId;

    private AccessReason lastAccessReason = AccessReason.INITIALIZATION;

    private long lifetime = 0L;

    private AudioPlayer audioPlayer;

    private Controller() {}

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

    public Controller connect(AudioChannel audioChannel, SpeakingMode... speakingModes) throws InsufficientPermissionException {
        AudioManager audioManager = getAudioManager();
        audioManager.setSelfDeafened(true);
        audioManager.setSpeakingMode(speakingModes.length == 0 ? List.of(SpeakingMode.SOUNDSHARE) : Arrays.asList(speakingModes));
        if(audioPlayer == null) {
            audioPlayer = Pulse.audioPlayerManager.createPlayer();
            audioPlayer.setVolume(7);
        }
        if(audioManager.getSendingHandler() == null)
            audioManager.setSendingHandler(new AudioSendHandler() {
                private AudioFrame lastFrame;
                @Override
                public boolean canProvide() {
                    return (this.lastFrame = audioPlayer.provide()) != null;
                }
                @Override
                public ByteBuffer provide20MsAudio() {
                    return ByteBuffer.wrap(lastFrame.getData());
                }
                @Override
                public boolean isOpus() {
                    return true;
                }
            });
        audioManager.openAudioConnection(audioChannel);
        return this;
    }

    public AudioPlayer getAudioPlayer() {
        return audioPlayer;
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
        return audioPlayer.getVolume();
    }

    public void setVolume(int volume) {
        audioPlayer.setVolume(volume);
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

    /**
     * Destroy the player and stop playing track.
     */
    public void destroy() {
        AudioManager audioManager = getAudioManager();
        audioManager.closeAudioConnection();
        audioManager.setSendingHandler(null);
        audioPlayer.destroy();
        audioPlayer = null;
    }
}
