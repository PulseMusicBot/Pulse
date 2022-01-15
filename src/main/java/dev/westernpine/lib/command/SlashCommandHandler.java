package dev.westernpine.lib.command;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public interface SlashCommandHandler extends CommandHandler {
	
	/**
	 * @return The command signifier string.
	 */
    public String command();
	
    /**
     * @return The command description.
     */
	public String description();
	
	/**
	 * @return Default auto-generated {@link CommandData} object based on command and description values, unless otherwise specified.
	 */
	public default CommandData commandData() {
		return new CommandData(command(), description());
	}
	
	/**
	 * The handler for the slash command event.
	 * @param event The slash command event to handle.
	 * @return Whether the command was handled or not. (Whether to continue to find an executioner.)
	 */
	public boolean handle(SlashCommandEvent event);
	
	//Other handle methods!

}
