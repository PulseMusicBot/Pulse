package dev.westernpine.pulse.interactions.command.commands.management;

import dev.westernpine.lib.interaction.component.command.SlashCommandComponentHandler;
import dev.westernpine.lib.object.Value;
import dev.westernpine.lib.util.jda.Embeds;
import dev.westernpine.lib.util.jda.Messenger;
import dev.westernpine.pulse.Pulse;
import dev.westernpine.pulse.authentication.Authenticator;
import dev.westernpine.pulse.controller.Controller;
import dev.westernpine.pulse.controller.ControllerFactory;
import dev.westernpine.pulse.controller.settings.Settings;
import dev.westernpine.pulse.controller.settings.setting.Setting;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.LinkedList;

public class DJRole implements SlashCommandComponentHandler {

    private static final Setting setting = Setting.DJ_ROLE;

    private static final OptionData data = new OptionData(OptionType.ROLE, "role", "The role to set as the DJ.");

    /**
     * @return How the command should be used.
     */
    @Override
    public String[] usages() {
        return new String[]{"%s [%s]".formatted(setting.getIds()[0]), data.getName()};
    }

    /**
     * @return The command signifier string.
     */
    @Override
    public String command() {
        return "%s".formatted(setting.getIds()[0]);
    }

    /**
     * @return The command description.
     */
    @Override
    public String description() {
        return setting.getDescription();
    }

    /**
     * @return The category of the command.
     */
    @Override
    public String category() {
        return "Management";
    }

    @Override
    public LinkedList<OptionData> options() {
        LinkedList<OptionData> options = new LinkedList<>();
        options.add(data);
        return options;
    }

    @Override
    public boolean handle(SlashCommandEvent event) {
        Controller controller = ControllerFactory.get(event.getGuild().getId(), true);
        Settings settings = controller.getSettings();
        OptionMapping option = event.getOption(data.getName());

        if (!Authenticator.isManager(event.getMember())) {
            Messenger.replyTo(event, Embeds.error("Authentication failed.", "You must be a server manager use this command."), 15);
            return false;
        }

        if (option == null) {
            Messenger.replyTo(event, Embeds.info("%s %s".formatted(setting.getEmoji(), setting.getLabel()), "%s".formatted(getRoleMention(event.getGuild(), settings.get(setting).toString())), Pulse.color(event.getGuild())), 15);
        } else {
            settings.set(setting, Value.of(option.getAsRole().getId()));
            Messenger.replyTo(event, Embeds.success("%s Updated".formatted(setting.getLabel()), "%s".formatted(getRoleMention(event.getGuild(), settings.get(setting).toString()))), 15);
        }
        return true;
    }

    public String getRoleMention(Guild guild, String roleId) {
        String defaultReturn = "`none`";
        if (roleId.isEmpty())
            return defaultReturn;
        Role role = guild.getRoleById(roleId);
        return role == null ? defaultReturn : role.getAsMention();
    }
}
