package dev.westernpine.pulse.listeners.system.jda;

import dev.westernpine.lib.interaction.component.command.SlashCommandComponentHandler;
import dev.westernpine.lib.util.jda.Messenger;
import dev.westernpine.pulse.Pulse;
import dev.westernpine.pulse.interactions.command.CommandManager;
import dev.westernpine.pulse.properties.IdentityProperties;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.stream.Collectors;

import static dev.westernpine.pulse.logging.Logger.logger;

public class GuildInitializer extends ListenerAdapter {

    public void initialize(Guild guild) {
        try {
            guild
                    .updateCommands()
                    .addCommands(CommandManager.getComponentHandlers().stream().map(SlashCommandComponentHandler::commandData).collect(Collectors.toList()))
                    .queue(success -> {}, initializeFailure -> {
                        guild.retrieveOwner().queue(owner -> {
                            owner.getUser().openPrivateChannel().queue(privateChannel -> {
                                privateChannel.sendMessage(Messenger.messageBuilder("I don't have permissions to update commands! Please re-invite me so I can initialize your guild properly. %s".formatted(Pulse.identityProperties.get(IdentityProperties.INVITE))).build()).queue(message -> {
                                    guild.leave().queue(leftGuild -> {
                                        logger.info("Left guild because of improper initialization! (%s) ".formatted(guild.getId()));
                                    }, leaveFailure -> {});
                                }, messageFailure -> {});
                            }, channelFailure -> {});
                        }, ownerFailure -> {});
                    });
        } catch (Exception e) {
            e.printStackTrace();
            logger.fine("Something went wrong when initializing a guild! (%s)".formatted(guild.getId()));
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
