package dev.westernpine.pulse.component.command.commands;

import com.vdurmont.emoji.EmojiParser;
import dev.westernpine.lib.interaction.component.command.SlashCommandComponentHandler;
import dev.westernpine.pulse.Pulse;
import dev.westernpine.pulse.component.button.ButtonManager;
import dev.westernpine.pulse.component.command.CommandManager;
import dev.westernpine.pulse.properties.IdentityProperties;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;

import java.awt.*;
import java.util.LinkedList;
import java.util.Objects;

public class Help implements SlashCommandComponentHandler {

    static String[] usages = new String[] {"help", "help [command]"};

    static OptionData OPTION_COMMAND = new OptionData(OptionType.STRING, "command", "Any command you would like to learn more about.");

    static MessageEmbed standardEmbed(JDA jda) {
        User self = jda.getSelfUser();
        return new EmbedBuilder()
                .setColor(Pulse.color())
                .setTitle("Bot Help")
                .setDescription("""
                        %s Visit the **Docs** to learn more or get started.
                                                            
                        %s Check out the **FAQ** for common questions.
                                                            
                        %s You can change how Pulse works by using `/settings`.
                                                            
                        %s To view all commands, use `/commands`.
                                                            
                        %s You can also view more information of a command by issuing `/help [command]`.
                        """.formatted(
                        EmojiParser.parseToUnicode(":question:"),
                        EmojiParser.parseToUnicode(":point_right:"),
                        EmojiParser.parseToUnicode(":gear:"),
                        EmojiParser.parseToUnicode(":scroll:"),
                        EmojiParser.parseToUnicode(":exclamation:"),
                        EmojiParser.parseToUnicode(":link:")
                ))
                .addField("Version:", "`%s`".formatted(Pulse.version), true)
                .setFooter("Have questions, or want to explore more projects? Click the support link!", self.getEffectiveAvatarUrl())
                .build();
    }

    static MessageEmbed commandEmbed(SlashCommandComponentHandler handler) {
        return new EmbedBuilder()
                .setColor(Pulse.color())
                .setTitle("Command: `" + handler.command() + "`")
                .addField("Description", handler.description(), true)
                .addBlankField(true)
                .addField("Usages (<> = Optional, [] = Required)", " - " + String.join("\n - ", handler.usages()), true)
                .build();
    }

    static MessageEmbed commandUnknownEmbed(String command) {
        return new EmbedBuilder()
                .setColor(Color.RED)
                .setTitle("Unknown Command: `" + command + "`")
                .addField("Available Commands", "`/commands`", true)
                .addBlankField(true)
                .addField("Help Usages (<> = Optional, [] = Required)", " - " + String.join("\n - ", usages), true)
                .build();
    }

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
    public String category() { return "Utility"; }

    @Override
    public LinkedList<OptionData> options() {
        LinkedList<OptionData> options = new LinkedList<>();
        options.add(OPTION_COMMAND);
        return options;
    }

    @Override
    public boolean handle(SlashCommandEvent event) {
        if (!event.getName().equals(command()))
            return false;
        if (event.getOptions().isEmpty()) {
            String host = Pulse.identityProperties.get(IdentityProperties.HOST);
            event.replyEmbeds(standardEmbed(event.getJDA())).setEphemeral(true)
                    .addActionRow(
                            ButtonManager.get("invite").get().toComponent(),
                            ButtonManager.get("support").get().toComponent(),
                            ButtonManager.get("website").get().toComponent(),
                            ButtonManager.get("docs").get().toComponent(),
                            ButtonManager.get("faq").get().toComponent()
                    )
                    .queue();
        } else {
            OptionMapping commandOption = event.getOption(OPTION_COMMAND.getName());
            String command = Objects.requireNonNull(commandOption).getAsString();
            CommandManager.get(command).ifPresentOrElse(
                    handler -> event.replyEmbeds(Objects.requireNonNull(commandEmbed(handler))).setEphemeral(true).queue(),
                    () -> event.replyEmbeds(Objects.requireNonNull(commandUnknownEmbed(command))).setEphemeral(true).queue());
        }
        return true;
    }

}
