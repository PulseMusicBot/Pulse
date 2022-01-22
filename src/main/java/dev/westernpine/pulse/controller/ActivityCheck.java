package dev.westernpine.pulse.controller;

public enum ActivityCheck {

    CACHE(EndCase.NONE),
    INACTIVE(EndCase.INACTIVITY_TIMEOUT),
    ALONE(EndCase.ALONE_TIMEOUT),
    PAUSE(EndCase.PAUSE_TIMEOUT),
    ACTIVE(EndCase.NONE);

    private EndCase endCase;

    ActivityCheck(EndCase endCase) {
        this.endCase = endCase;
    }

    public EndCase getEndCase() {
        return endCase;
    }
}
