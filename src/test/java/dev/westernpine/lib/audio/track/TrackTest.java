package dev.westernpine.lib.audio.track;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.iheart.iHeartAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.westernpine.bettertry.Try;
import dev.westernpine.lib.player.audio.AudioFactory;
import dev.westernpine.lib.player.audio.track.Track;
import dev.westernpine.lib.player.audio.track.TrackFactory;
import dev.westernpine.lib.player.audio.track.userdata.platform.PlatformManager;
import dev.westernpine.lib.player.manager.OpenAudioPlayerManager;
import dev.westernpine.lib.player.source.WrappedSource;
import dev.westernpine.pulse.Pulse;

class TrackTest {

    public static void main(String[] args) {
//        Pulse.main(args);
//        OpenAudioPlayerManager manager = Pulse.audioPlayerManager;

        OpenAudioPlayerManager manager = new OpenAudioPlayerManager();
//        manager.registerSourceManager(new HttpAudioSourceManager());
        manager.registerSourceManager(new WrappedSource(new YoutubeAudioSourceManager()));
        manager.registerSourceManager(new WrappedSource(new iHeartAudioSourceManager()));

        PlatformManager.registerKnown(() -> manager);


        AudioItem item = Try.to(() -> AudioFactory.query(manager,
                "https://www.iheart.com/live/b985-5887/"
//                "https://www.youtube.com/playlist?list=PLZQCnHsR-SG4MVOk6WKS2rS6__H3VCivx"
        ).get()).orElse(null);

        if(item == null)
            System.out.println("Item null!");
        else {
            Track audioTrack = TrackFactory.from(manager, AudioFactory.toTrack(item));
            String json = TrackFactory.toJson(audioTrack);
            System.out.println(json);

            Track track = TrackFactory.fromJson(manager, json);
            System.out.println(TrackFactory.toJson(track));
            AudioTrack internal = track.getAudioTrack();
            System.out.println(internal == null ? "NULL INTERNAL" : TrackFactory.toJson(TrackFactory.from(manager, internal)));
            System.out.println(TrackFactory.toJson(track));
        }

        System.exit(0);

    }


}