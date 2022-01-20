package dev.westernpine.pulse.audio.request;

import dev.westernpine.bettertry.Try;

import java.net.URI;
import java.net.URL;

public class Request {

    String request;
    Platform platform;

    Request(String request, Platform platform) {
        this.request = request;
        this.platform = platform;
    }

    public String getRequest() {
        return request;
    }

    public Platform getPlatform() {
        return platform;
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
        return this.isUrl() ? this.request : platform.getPrefix() + this.request;
    }

}
