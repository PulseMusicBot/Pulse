package dev.westernpine.lib.command;

public interface ConsoleCommandHandler extends CommandHandler {
	
	/**
	 * @return The command signifier string.
	 */
    public String command();
	
    /**
     * @return The command description.
     */
	public String description();
	
	/**
	 * The handler for the console command.
	 * @param command The command that was issued.
	 * @param args The attached arguments to the command if any.
	 * @return Whether the command was handled or not. (Whether to continue to find an executioner.)
	 */
	public boolean handle(String command, String[] args);
	
	//Other handle methods!

}
