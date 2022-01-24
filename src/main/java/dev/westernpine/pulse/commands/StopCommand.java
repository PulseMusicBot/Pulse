package dev.westernpine.pulse.commands;

import dev.westernpine.lib.interaction.ConsoleCommandHandler;
import dev.westernpine.pulse.Pulse;
import dev.westernpine.lib.object.State;

import static dev.westernpine.pulse.logging.Logger.logger;

public class StopCommand implements ConsoleCommandHandler {

	@Override
	public String[] usages() {
		return new String[] {"stop"};
	}

	@Override
	public String command() {
		return "stop";
	}

	@Override
	public String description() {
		return "Stops the bot if running, then the service.";
	}

	@Override
	public String category() {
		return "Management";
	}

	@Override
	public boolean handle(String command, String[] args) {
		logger.info("Shutting down...");
		Pulse.setState(State.SHUTDOWN);
		return true;
	}

}
