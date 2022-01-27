package dev.westernpine.pulse.listeners.system.jda.controller;

import dev.westernpine.pulse.controller.Controller;
import dev.westernpine.pulse.controller.ControllerFactory;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GuildVoiceListener extends ListenerAdapter {

    @Override
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
        ControllerFactory.ifCached(event.getGuild().getId(), controller -> {
            if (!controller.getSelfMember().getId().equals(event.getMember().getId()))
                controller.manageState();
        });
    }

    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        ControllerFactory.ifCached(event.getGuild().getId(), Controller::manageState);
    }

    @Override
    public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
        ControllerFactory.ifCached(event.getGuild().getId(), Controller::manageState);
    }

}
