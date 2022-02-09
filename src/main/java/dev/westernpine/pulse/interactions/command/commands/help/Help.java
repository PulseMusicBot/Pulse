package dev.westernpine.pulse.interactions.command.commands.help;

import dev.westernpine.lib.interaction.component.command.SlashCommandComponentHandler;
import dev.westernpine.lib.util.jda.Embeds;
import dev.westernpine.pulse.Pulse;
import dev.westernpine.pulse.interactions.button.ButtonManager;
import dev.westernpine.pulse.interactions.command.CommandManager;
import dev.westernpine.pulse.properties.IdentityProperties;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.awt.*;
import java.util.LinkedList;
import java.util.Objects;

public class Help implements SlashCommandComponentHandler {

    static String[] usages = new String[]{"help [command]"};

    static OptionData OPTION_COMMAND = new OptionData(OptionType.STRING, "command", "Any command you would like to learn more about.");

    @Override
    public String[] usages() {
        return usages;
    }

    @Override
    public String command() {
        return "help";
    }

    @Override
    public String description() {
        return "Everything you need starts here!";
    }

    @Override
    public String category() {
        return "Help";
    }

    @Override
    public LinkedList<OptionData> options() {
        LinkedList<OptionData> options = new LinkedList<>();
        options.add(OPTION_COMMAND);
        return options;
    }

    @Override
    public boolean handle(SlashCommandEvent event) {
        if (event.getOptions().isEmpty()) {
            String host = Pulse.identityProperties.get(IdentityProperties.HOST);
            event.replyEmbeds(Embeds.info("Bot Help", """
                                    :question: Visit the **Docs** to learn more or get started.
                                                                        
                                    :point_right: Check out the **FAQ** for common questions.
                                                                        
                                    :gear: You can change how Pulse works by using `/settings`.
                                                                        
                                    :scroll: To view all commands, use `/commands`.
                                                                        
                                    :exclamation: You can also view more information of a command by issuing `/help [command]`.
                                    """, Pulse.color())
                            .setFooter("Have questions, or want to explore more projects? Click the support link!", event.getJDA().getSelfUser().getEffectiveAvatarUrl())
                            .build())
                    .setEphemeral(true)
                    .addActionRow(
                            ButtonManager.get("invite").get().toComponent(),
                            ButtonManager.get("support").get().toComponent(),
                            ButtonManager.get("website").get().toComponent()
                    )
                    .queue();
        } else {
            OptionMapping commandOption = event.getOption(OPTION_COMMAND.getName());
            String command = Objects.requireNonNull(commandOption).getAsString();
            CommandManager.get(command).ifPresentOrElse(
                    handler -> event.replyEmbeds(Objects.requireNonNull(new EmbedBuilder()
                                    .setColor(Pulse.color())
                                    .setTitle("Command: `" + handler.command() + "`")
                                    .addField("Description", handler.description(), true)
                                    .addBlankField(true)
                                    .addField("Usages (<> = Optional, [] = Required)", " - " + String.join("\n - ", handler.usages()), true)
                                    .build()))
                            .setEphemeral(true)
                            .queue(),
                    () -> event.replyEmbeds(Objects.requireNonNull(
                                    new EmbedBuilder()
                                            .setColor(Color.RED)
                                            .setTitle("Unknown Command: `" + command + "`")
                                            .addField("Available Commands", "`/commands`", true)
                                            .addBlankField(true)
                                            .addField("Help Usages (<> = Optional, [] = Required)", " - " + String.join("\n - ", usages), true)
                                            .build()))
                            .setEphemeral(true)
                            .queue());
        }
        return true;
    }

}
