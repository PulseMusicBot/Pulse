package dev.westernpine.pulse.audio.track.userdata;

import dev.westernpine.pulse.audio.track.userdata.request.Request;
import dev.westernpine.pulse.audio.track.userdata.platform.Platform;
import dev.westernpine.pulse.audio.track.userdata.requester.Requester;

public record UserData(Request request, Requester requester, Platform preferredPlatform) {

}
