package dev.westernpine.lib.interaction;

public interface ConsoleCommandHandler extends CommandHandler {
	
	/**
	 * The handler for the console command.
	 * @param command The command that was issued.
	 * @param args The attached arguments to the command if any.
	 * @return Whether the command was handled or not. (Whether to continue to find an executioner.)
	 */
	public boolean handle(String command, String[] args);
	
	//Other handle methods!

}
