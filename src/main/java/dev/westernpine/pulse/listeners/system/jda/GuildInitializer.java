package dev.westernpine.pulse.listeners.system.jda;

import dev.westernpine.lib.interaction.component.command.SlashCommandComponentHandler;
import dev.westernpine.pulse.component.command.CommandManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.stream.Collectors;

public class GuildInitializer extends ListenerAdapter {

    public void initialize(Guild guild) {
        try {
            guild
                    .updateCommands()
                    .addCommands(CommandManager.getComponentHandlers().stream().map(SlashCommandComponentHandler::commandData).collect(Collectors.toList()))
                    .queue();
            System.out.println("GuildInitializer >> Guild initialized! (%s)".formatted(guild.getId()));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("GuildInitializer >> Something went wrong when initializing a guild! (%s)".formatted(guild.getId()));
        }
    }

    @Override
    public void onGuildReady(GuildReadyEvent event) {
        initialize(event.getGuild());
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        initialize(event.getGuild());
    }

}
