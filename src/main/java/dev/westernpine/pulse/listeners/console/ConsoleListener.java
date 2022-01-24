package dev.westernpine.pulse.listeners.console;

import dev.westernpine.eventapi.objects.EventHandler;
import dev.westernpine.eventapi.objects.Listener;
import dev.westernpine.lib.interaction.ConsoleCommandHandler;
import dev.westernpine.pulse.commands.ControllersCommand;
import dev.westernpine.pulse.commands.StopCommand;
import dev.westernpine.pulse.events.console.ConsoleEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static dev.westernpine.pulse.logging.Logger.logger;

public class ConsoleListener implements Listener {
	
	private List<ConsoleCommandHandler> commands = new ArrayList<>();
	
	public ConsoleListener() {
		commands.add(new StopCommand());
		commands.add(new ControllersCommand());
	}
	
	@EventHandler
	public void onConsoleEvent(ConsoleEvent event) {
		String message = event.getMessage();

		logger.info("Console: " + message);
		
		if(message == null || message.isBlank())
			return;

		String[] split = message.split(" ");
		String command = split[0];
		String[] args = split.length > 1 ? Arrays.copyOfRange(split, 1, split.length) : new String[] {};
		
		for(ConsoleCommandHandler handler : commands) {
			if(!handler.command().equalsIgnoreCase(command))
				continue;
			
			if(handler.handle(command, args))
				break;
		}
	}

}
