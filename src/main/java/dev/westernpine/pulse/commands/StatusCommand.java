package dev.westernpine.pulse.commands;

import dev.westernpine.lib.interaction.ConsoleCommandHandler;
import dev.westernpine.pulse.Pulse;
import dev.westernpine.pulse.controller.ControllerFactory;

import static dev.westernpine.pulse.logging.Logger.logger;

public class StatusCommand implements ConsoleCommandHandler {

    @Override
    public String[] usages() {
        return new String[]{"status"};
    }

    @Override
    public String command() {
        return "status";
    }

    @Override
    public String description() {
        return "Get the status of the bot.";
    }

    @Override
    public String category() {
        return "Management";
    }

    @Override
    public boolean handle(String command, String[] args) {
        logger.info("Guilds: " + Pulse.shardManager.getGuilds().size());
        return true;
    }

}
