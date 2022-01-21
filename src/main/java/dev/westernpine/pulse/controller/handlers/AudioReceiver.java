package dev.westernpine.pulse.controller.handlers;

import dev.westernpine.pulse.controller.Controller;
import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.UserAudio;

import java.time.Instant;

public class AudioReceiver implements AudioReceiveHandler {

    private Instant lastAudioDetected = Instant.now();

    private final Controller controller;

    public AudioReceiver(Controller controller) {
        this.controller = controller;
    }

    @Override
    public boolean canReceiveUser() {
        return audioSession.getAgent().getGuildConfig().get(ConfigKey.AUDIO_SUPPRESSION).toBoolean();
        return false;
    }

    @Override
    public void handleUserAudio(UserAudio userAudio) {
        if(!userAudio.getUser().isBot()) //ignore all bot audio including self (So two music bot's can run at same time if users have other bot's muted, without audio being suppressed).
            this.lastVoiceDetected = Instant.now();
    }

    public Instant getLastAudioDetected() {
        return lastAudioDetected;
    }
}
