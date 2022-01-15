package dev.westernpine.pulse.events.console;

import dev.westernpine.eventapi.objects.Cancellable;
import dev.westernpine.eventapi.objects.Event;

public class ConsoleEvent extends Event implements Cancellable {

	private String message;

	private boolean cancelled;
	
	public ConsoleEvent(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
}