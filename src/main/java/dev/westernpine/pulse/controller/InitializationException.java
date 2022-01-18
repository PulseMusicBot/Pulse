package dev.westernpine.pulse.controller;

public class InitializationException extends RuntimeException {

    public InitializationException() {
        super("The controller has not been initialized yet!");
    }

}
