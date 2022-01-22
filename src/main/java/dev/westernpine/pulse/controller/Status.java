package dev.westernpine.pulse.controller;

public enum Status {

    CACHE(EndCase.NONE),
    INACTIVE(EndCase.INACTIVITY_TIMEOUT),
    ALONE(EndCase.ALONE_TIMEOUT),
    PAUSED(EndCase.PAUSE_TIMEOUT),
    ACTIVE(EndCase.NONE);

    private EndCase endCase;

    Status(EndCase endCase) {
        this.endCase = endCase;
    }

    public EndCase getEndCase() {
        return endCase;
    }

    public boolean is(Status status) {
        return this == status;
    }

    public boolean isnt(Status status) {
        return !is(status);
    }
}
