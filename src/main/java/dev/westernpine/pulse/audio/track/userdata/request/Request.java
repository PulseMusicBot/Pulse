package dev.westernpine.pulse.audio.track.userdata.request;

import dev.westernpine.bettertry.Try;
import dev.westernpine.pulse.audio.track.userdata.platform.Platform;

import java.net.URI;
import java.net.URL;

public class Request {

    String request;

    Request(String request) {
        this.request = request;
    }

    public String getRequest() {
        return request;
    }

    public URL toUrl() {
        return Try.of(() -> new URL(request)).getUnchecked();
    }

    public URI toUri() {
        return Try.of(() -> new URI(request)).getUnchecked();
    }

    public boolean isUrl() {
        return Try.of(this::toUrl).isSuccessful();
    }

    public boolean isUri() {
        return !request.contains(" ") && request.contains(":"); //Only requirements for spotify uri
    }

    @Override
    public String toString() {
        return this.request;
    }

}
