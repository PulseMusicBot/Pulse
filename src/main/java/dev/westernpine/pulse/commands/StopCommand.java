package dev.westernpine.pulse.commands;

import dev.westernpine.lib.command.ConsoleCommandHandler;

public class StopCommand implements ConsoleCommandHandler {

	@Override
	public String command() {
		return "stop";
	}

	@Override
	public String description() {
		return "Stops the bot if running, then the service.";
	}

	@Override
	public boolean handle(String command, String[] args) {
		System.out.println("Shutting down...");
		System.exit(0);
		return true;
	}

}
