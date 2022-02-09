package dev.westernpine.pulse.interactions.command.commands.informative;

import dev.westernpine.lib.interaction.component.command.SlashCommandComponentHandler;
import dev.westernpine.lib.util.jda.Embeds;
import dev.westernpine.lib.util.jda.Messenger;
import dev.westernpine.pulse.Pulse;
import dev.westernpine.pulse.controller.Controller;
import dev.westernpine.pulse.controller.ControllerFactory;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.LinkedList;
import java.util.Optional;

public class Status implements SlashCommandComponentHandler {

    /**
     * @return How the command should be used.
     */
    @Override
    public String[] usages() {
        return new String[]{"status"};
    }

    /**
     * @return The command signifier string.
     */
    @Override
    public String command() {
        return "status";
    }

    /**
     * @return The command description.
     */
    @Override
    public String description() {
        return "Returns the current status of Pulse.";
    }

    /**
     * @return The category of the command.
     */
    @Override
    public String category() {
        return "Informative";
    }

    @Override
    public LinkedList<OptionData> options() {
        LinkedList<OptionData> options = new LinkedList<>();
        return options;
    }

    @Override
    public boolean handle(SlashCommandEvent event) {
        Controller controller = ControllerFactory.get(event.getGuild().getId(), true);
        Optional<AudioChannel> connectedChannel = controller.getConnectedChannel();

        long gatewayPing = event.getGuild().getJDA().getGatewayPing();
        long restPing = event.getGuild().getJDA().getRestPing().complete();

        controller.setLastChannelId(event.getChannel().getId());

        //The actual playing message.
        EmbedBuilder embedBuilder = Embeds.info(":satellite: Discord Status", "", Pulse.color(event.getGuild()))
                .addField("Voice & Events", "`" + gatewayPing + " ms`", true)
                .addField("Messaging & Properties", "`" + restPing + " ms`", true)
                .addField("Experiencing Problems?", "[Changing the discord server or voice channel region](https://support.discord.com/hc/en-us/articles/1500007645701-Voice-Regions-on-Discord-FAQ) typically fixes any glitchy audio, as some regions can be overloaded. "
                        + "You can also check the [Discord Status](https://status.discord.com/) page, "
                        + "but keep in mind, sometimes discord experiences lag that isn't listed.", false);

        Messenger.replyTo(event, embedBuilder, 15);
        return true;
    }
}
