package dev.westernpine.pulse.events.system;

import dev.westernpine.eventapi.objects.Event;
import dev.westernpine.lib.object.State;

public class StateChangeEvent extends Event {

    private State oldState;
    private State newState;

    public StateChangeEvent(State oldState, State newState) {
        this.oldState = oldState;
        this.newState = newState;
    }

    public State getOldState() {
        return oldState;
    }

    public State getNewState() {
        return newState;
    }
}
