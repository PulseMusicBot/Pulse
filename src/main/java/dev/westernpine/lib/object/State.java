package dev.westernpine.lib.object;

import java.util.Arrays;

public enum State {

    OFFLINE("Offline", false, false),
    INITIALIZATION("Initialization", true, true),
    STARTUP("System Startup", true, true),
    RUNNING("System Running", true, false),
    SHUTDOWN("System Shutdown", false, true);

    private String name;
    private boolean active;
    private boolean loggable;

    State(String name, boolean active, boolean loggable) {
        this.name = name;
        this.active = active;
        this.loggable = loggable;
    }

    public static State fromName(String string) {
        return Arrays.stream(State.values()).filter(state -> state.name.equals(string)).findAny().orElse(null);
    }

    public String getName() {
        return this.name;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isLoggable() {
        return loggable;
    }

    public boolean is(State state) {
        return this == state;
    }
}
