package dev.westernpine.pulse.component.command.commands;

import dev.westernpine.bettertry.Try;
import dev.westernpine.lib.interaction.component.command.SlashCommandComponentHandler;
import dev.westernpine.pulse.audio.AudioFactory;
import dev.westernpine.pulse.controller.AccessReason;
import dev.westernpine.pulse.controller.Controller;
import net.dv8tion.jda.api.audio.SpeakingMode;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.LinkedList;

public class Test implements SlashCommandComponentHandler {
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
        return "test";
    }

    /**
     * @return The command description.
     */
    @Override
    public String description() {
        return "a test command";
    }

    /**
     * @return The category of the command.
     */
    @Override
    public String category() {
        return "test";
    }

    @Override
    public LinkedList<OptionData> options() {
        LinkedList<OptionData> options = new LinkedList<>();
        options.add(new OptionData(OptionType.STRING, "query", "Anything you want to query."));
        return options;
    }

    @Override
    public boolean handle(SlashCommandEvent event) {
        if(!event.getName().equals(command()))
            return false;
        Controller controller = Controller.get(event.getGuild().getId(), AccessReason.UNKNOWN);
        controller.connect(controller.getAudioChannel(event.getMember()), SpeakingMode.SOUNDSHARE);
        Try.of(() -> AudioFactory.toTrack(AudioFactory.query(event.getOption("query").getAsString()).get()))
                .onSuccess(track -> controller.startTrack(track, true))
                .onFailure(Throwable::printStackTrace);
        return true;
    }
}
