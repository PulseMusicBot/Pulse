package dev.westernpine.lib.audio.track;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.westernpine.bettertry.Try;
import dev.westernpine.lib.audio.AudioFactory;
import dev.westernpine.pulse.Pulse;
import dev.westernpine.pulse.player.sources.iHeart.iHeartAudioSourceManager;

import static org.junit.jupiter.api.Assertions.*;

class TrackTest {

    public static void main(String[] args) {
        Pulse.main(args);
        AudioPlayerManager manager = Pulse.audioPlayerManager;

//        AudioPlayerManager manager = new DefaultAudioPlayerManager();
////        manager.registerSourceManager(new HttpAudioSourceManager());
//        manager.registerSourceManager(new YoutubeAudioSourceManager());
//        manager.registerSourceManager(new VimeoAudioSourceManager());
//        manager.registerSourceManager(new TwitchStreamAudioSourceManager());
//        manager.registerSourceManager(new iHeartAudioSourceManager(manager));


        AudioItem item = Try.to(() -> AudioFactory.query(manager, "https://www.iheart.com/live/b985-5887/").get()).orElse(null);

        if(item == null)
            System.out.println("Item null!");
        else {
            AudioTrack audioTrack = AudioFactory.toTrack(item);
            String json = AudioFactory.toJson(audioTrack);
            System.out.println(json);

            Track track = AudioFactory.fromTrackJson(json);
            System.out.println(AudioFactory.toJson(track));
            AudioTrack internal = track.getAudioTrack(manager);
            System.out.println(internal == null ? "NULL INTERNAL" : AudioFactory.toJson(internal));
            System.out.println(AudioFactory.toJson(track));
        }

        System.exit(0);

    }


}