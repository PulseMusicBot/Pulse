package dev.westernpine.pulse.controller.handlers.audio;

import dev.westernpine.pulse.controller.Controller;
import dev.westernpine.pulse.controller.settings.setting.Setting;
import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.UserAudio;

import java.time.Instant;

public class AudioReceiver implements AudioReceiveHandler {

    private final Controller controller;
    private Instant lastAudioDetected = null;

    public AudioReceiver(Controller controller) {
        this.controller = controller;
    }

    @Override
    public boolean canReceiveUser() {
        return controller.getSettings().get(Setting.VOICE_DETECTION).toBoolean();
    }

    @Override
    public void handleUserAudio(UserAudio userAudio) {
        if (!userAudio.getUser().isBot()) //ignore all bot audio including self (So two music bot's can run at same time if users have other bot's muted, without audio being suppressed).
            this.lastAudioDetected = Instant.now();
    }

    public Instant getLastAudioDetected() {
        return lastAudioDetected;
    }
}
