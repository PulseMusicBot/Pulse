package dev.westernpine.lib.interaction.component.command;

import dev.westernpine.lib.interaction.component.IdentifiableComponent;
import dev.westernpine.lib.interaction.CommandHandler;
import net.dv8tion.jda.api.events.interaction.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.LinkedList;

public interface SlashCommandComponentHandler extends CommandHandler, IdentifiableComponent {

	public default String id() {
		return this.command();
	}

	public default CommandData commandData() {
		return new CommandData(command(), description()).addOptions(options());
	}

    public LinkedList<OptionData> options();

	public boolean handle(SlashCommandEvent event);

}
