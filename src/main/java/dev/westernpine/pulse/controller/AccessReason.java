package dev.westernpine.pulse.controller;

public enum AccessReason {

    UNKNOWN(false, false, false),

    //Should Save

    //Should Reset Lifetime
    INITIALIZATION(false, false, true),

    //Should Save and Reset Lifetime

    ;

    //Used in case we just need to get a property of the controller, but we don't want to override the last valid access reason.
    private final boolean shouldOverride;

    //Used if we want to save the controller.
    private final boolean shouldSave;

    //Used if we want to reset the lifetime of the controller.
    private final boolean shouldResetLifetime;

    AccessReason(boolean shouldOverride, boolean shouldSave, boolean shouldResetLifetime) {
        this.shouldOverride = shouldOverride;
        this.shouldSave = shouldSave;
        this.shouldResetLifetime = shouldResetLifetime;
    }

    public boolean shouldOverride() {
        return shouldOverride;
    }

    public boolean shouldSave() {
        return shouldSave;
    }

    public boolean shouldResetLifetime() {
        return shouldResetLifetime;
    }
}
