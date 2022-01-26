package dev.westernpine.pulse.component.command.commands;

import dev.westernpine.bettertry.Try;
import dev.westernpine.lib.audio.AudioFactory;
import dev.westernpine.lib.interaction.component.command.SlashCommandComponentHandler;
import dev.westernpine.lib.object.TriState;
import dev.westernpine.lib.util.Formatter;
import dev.westernpine.lib.util.jda.Embeds;
import dev.westernpine.lib.util.jda.Messenger;
import dev.westernpine.pulse.Pulse;
import dev.westernpine.pulse.controller.Controller;
import dev.westernpine.pulse.controller.ControllerFactory;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.LinkedList;

public class Play implements SlashCommandComponentHandler {
    /**
     * @return How the command should be used.
     */
    @Override
    public String[] usages() {
        return new String[0];
    }

    /**
     * @return The command signifier string.
     */
    @Override
    public String command() {
        return "play";
    }

    /**
     * @return The command description.
     */
    @Override
    public String description() {
        return "A play command";
    }

    /**
     * @return The category of the command.
     */
    @Override
    public String category() {
        return "play";
    }

    @Override
    public LinkedList<OptionData> options() {
        LinkedList<OptionData> options = new LinkedList<>();
        options.add(new OptionData(OptionType.STRING, "query", "Anything you want to query."));
        return options;
    }

    @Override
    public boolean handle(SlashCommandEvent event) {
        if (!event.getName().equals(command()))
            return false;
        Controller controller = ControllerFactory.get(event.getGuild().getId(), true).connect(event.getMember());
        Try.to(() -> AudioFactory.toTrack(AudioFactory.query(event.getOption("query").getAsString()).get()))
                .onSuccess(track -> controller.enque(track, TriState.FALSE))
                .onSuccess(track -> event.reply(Messenger.buildMessage(Embeds.play("Now Playing", Formatter.formatInfo(track.getInfo()), track.getInfo().isStream ? -1 : track.getDuration(), Pulse.color(event.getGuild())))).queue())
                .onFailure(Throwable::printStackTrace);
        return true;
    }
}
