package dev.westernpine.pulse.audio.requester;

import dev.westernpine.pulse.Pulse;
import net.dv8tion.jda.api.entities.User;

public class RequesterFactory {

    public static Requester from(User user) {
        return new Requester(user.getId(), user.getName(), user.getDiscriminator(), user.getEffectiveAvatarUrl());
    }

    public static Requester fromJson(String json) {
        return Pulse.gson.fromJson(json, Requester.class);
    }

    public static String toJson(Requester requester) {
        return Pulse.gson.toJson(requester);
    }

}
