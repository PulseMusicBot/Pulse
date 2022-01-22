package dev.westernpine.pulse.commands;

import dev.westernpine.lib.interaction.ConsoleCommandHandler;
import dev.westernpine.pulse.controller.ControllerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class ControllersCommand implements ConsoleCommandHandler {

    @Override
    public String[] usages() {
        return new String[] {"controllers"};
    }

    @Override
    public String command() {
        return "controllers";
    }

    @Override
    public String description() {
        return "Lists all controllers in json format.";
    }

    @Override
    public String category() {
        return "Management";
    }

    @Override
    public boolean handle(String command, String[] args) {
        System.out.println("Cached Controllers: ");
        ControllerFactory.getControllers().forEach((guildId, controller) -> System.out.println(" - " + ControllerFactory.toJson(controller)));
        return true;
    }

}
