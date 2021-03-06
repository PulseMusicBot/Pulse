package dev.westernpine.pulse.commands;

import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import dev.westernpine.bettertry.Try;
import dev.westernpine.lib.interaction.ConsoleCommandHandler;
import dev.westernpine.lib.object.TriState;
import dev.westernpine.lib.player.audio.AudioFactory;
import dev.westernpine.lib.player.audio.playlist.Playlist;
import dev.westernpine.lib.player.audio.playlist.PlaylistFactory;
import dev.westernpine.lib.player.audio.track.userdata.UserDataFactory;
import dev.westernpine.lib.player.audio.track.userdata.platform.Platform;
import dev.westernpine.lib.player.audio.track.userdata.platform.PlatformManager;
import dev.westernpine.lib.player.audio.track.userdata.request.Request;
import dev.westernpine.lib.player.audio.track.userdata.request.RequestFactory;
import dev.westernpine.lib.player.audio.track.userdata.requester.Requester;
import dev.westernpine.lib.player.audio.track.userdata.requester.RequesterFactory;
import dev.westernpine.lib.util.Strings;
import dev.westernpine.pulse.Pulse;
import dev.westernpine.pulse.controller.Controller;
import dev.westernpine.pulse.controller.ControllerFactory;
import dev.westernpine.pulse.properties.IdentityProperties;

import java.util.Arrays;

import static dev.westernpine.pulse.logging.Logger.logger;

public class ForceCommand implements ConsoleCommandHandler {

    @Override
    public String[] usages() {
        return new String[]{"force <guildid> <query>"};
    }

    @Override
    public String command() {
        return "force";
    }

    @Override
    public String description() {
        return "Force the bot to play something for a guild.";
    }

    @Override
    public String category() {
        return "Management";
    }

    @Override
    public boolean handle(String command, String[] args) {
        if (args.length < 2) {
            logger.info("Invalid usage:");
            for (String usage : usages())
                logger.info(" - " + usage);
            return false;
        }

        if (!Strings.isNumeric(args[0])) {
            logger.info("The first argument must be a guild id!");
            return false;
        }

        Controller controller = ControllerFactory.get(args[0], false);

        if (controller == null) {
            logger.info("Unable to find controller with guildid: " + args[0]);
            return false;
        }

        if (!controller.isConnected()) {
            logger.info("The controller is no longer connected for guildid: " + args[0]);
            return false;
        }

        String query = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        Platform platform = PlatformManager.defaultPlatform();

        Playlist playlist = Try.to(() -> AudioFactory.query(Pulse.audioPlayerManager, query).get())
                .flatMap(result -> result != null && result != AudioReference.NO_TRACK
                        ? Try.successful(result)
                        : Try.failure(null))
                .orElseTry(() -> AudioFactory.query(Pulse.audioPlayerManager, platform.getSearchPrefix() + query).get())
                .flatMap(result -> result != null && result != AudioReference.NO_TRACK
                        ? Try.successful(result)
                        : Try.failure(null))
                .map(AudioFactory::toPlaylist)
                .map(PlaylistFactory::from)
                .orElse(null);

        if (playlist == null || playlist.isEmpty()) {
            logger.info("Unable to find anything playable with: " + args[0]);
            return false;
        }

        if (playlist.isSearchResult())
            playlist.setTracks(playlist.getTracks().get(0));

        Requester requester = RequesterFactory.from(Pulse.shardManager.retrieveUserById(Pulse.identityProperties.get(IdentityProperties.ADMINS).split(", ")[0]).complete());
        Request request = RequestFactory.from(query);

        playlist.applyUserData(UserDataFactory.from(request, requester, platform));

        controller.enqueue(playlist, TriState.TRUE);

        return true;
    }

}
