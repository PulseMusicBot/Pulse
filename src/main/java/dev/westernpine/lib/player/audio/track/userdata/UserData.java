package dev.westernpine.lib.player.audio.track.userdata;

import dev.westernpine.lib.player.audio.track.userdata.platform.Platform;
import dev.westernpine.lib.player.audio.track.userdata.request.Request;
import dev.westernpine.lib.player.audio.track.userdata.requester.Requester;

public record UserData(Request request, Requester requester, Platform preferredPlatform) {

}
