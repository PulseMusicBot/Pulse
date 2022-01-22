package dev.westernpine.pulse.controller.handlers.audio;

import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import dev.westernpine.pulse.controller.Controller;
import dev.westernpine.pulse.controller.settings.setting.Setting;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.time.Instant;

public class AudioSender implements AudioSendHandler {

    private Controller controller;

    private AudioFrame lastFrame;

    public AudioSender(Controller controller) {
        this.controller = controller;
    }

    @Override
    public boolean canProvide() {
        int volume = controller.getVolume();
        Number adjusted = volume * 0.25D;
        if(controller.getSettings().get(Setting.VOICE_DETECTION).toBoolean())
            if(Instant.now().minusSeconds(2).isBefore(controller.getAudioReceiver().getLastAudioDetected()))
                volume = adjusted.intValue() > 0 ? adjusted.intValue() : (volume != 1 ? 1 : 0);
        if(volume != controller.getAudioPlayer().getVolume())
            controller.getAudioPlayer().setVolume(volume);
        return (lastFrame = controller.getAudioPlayer().provide()) != null;
    }

    @Nullable
    @Override
    public ByteBuffer provide20MsAudio() {
        return ByteBuffer.wrap(lastFrame.getData());
    }

    @Override
    public boolean isOpus() {
        return true;
    }
}
