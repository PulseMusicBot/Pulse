package dev.westernpine.pulse.controller;

public enum EndCase {

    COULDNT_START("Unable to start the session."),
    BY_COMMAND("By request."),
    DISCONNECTED("Connection lost."),
    INACTIVITY_TIMEOUT("Inactive for too long."),
    PAUSE_TIMEOUT("Paused for too long."),
    ALONE_TIMEOUT("Alone for too long."),
    BOT_RESTART("Bot restarting."),
    FATAL_ERROR("A fatal error has occured. Please contact the bot developer."),
    CACHE_CLEANUP("Cache clean-up (Error)."),
    ADMIN_DISCONNECT("Disconnected by an Official Bot Administrator."),
    PREMIUM_EXPIRED("This server's premium status has expired!"),
    NONE(""),
    ;

    private String reason;

    EndCase(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
}
