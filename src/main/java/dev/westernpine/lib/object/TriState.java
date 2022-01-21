package dev.westernpine.lib.object;

import java.util.Arrays;

public enum TriState {

    TRUE("True", true),
    NONE("None", false),
    FALSE("False", false);

    private String friendlyName;
    private boolean booleanValue;

    TriState(String friendlyName, boolean booleanValue) {
        this.friendlyName = friendlyName;
        this.booleanValue = booleanValue;
    }

    public static TriState from(String value) {
        return Arrays.asList(TriState.values()).stream().filter(state -> state.getFriendlyName().equalsIgnoreCase(value)).findAny().orElse(NONE);
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public boolean isBooleanValue() {
        return booleanValue;
    }

    public TriState previous() {
        return this.equals(TRUE) ? FALSE : this.equals(FALSE) ? NONE : TRUE;
    }

    public TriState next() {
        return this.equals(TRUE) ? NONE : this.equals(NONE) ? FALSE : TRUE;
    }

    public boolean isTrue() {
        return this.equals(TRUE);
    }

    public boolean isNone() {
        return this.equals(NONE);
    }

    public boolean isFalse() {
        return this.equals(FALSE);
    }

}
