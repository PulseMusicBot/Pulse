package dev.westernpine.pulse.commands;

import dev.westernpine.lib.interaction.ConsoleCommandHandler;
import dev.westernpine.pulse.controller.ControllerFactory;

import static dev.westernpine.pulse.logging.Logger.logger;

public class ControllersCommand implements ConsoleCommandHandler {

    @Override
    public String[] usages() {
        return new String[]{"controllers"};
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
        logger.info("Cached Controllers: ");
        ControllerFactory.getControllers().forEach((guildId, controller) -> logger.info(" - " + ControllerFactory.toJson(controller)));
        return true;
    }

}
