package dev.westernpine.pulse.commands;

import dev.westernpine.lib.interaction.ConsoleCommandHandler;
import dev.westernpine.pulse.Pulse;

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
		System.out.println("Shutting down...");
		Pulse.shutdownTask.run();
		System.exit(0);
		return true;
	}

}
