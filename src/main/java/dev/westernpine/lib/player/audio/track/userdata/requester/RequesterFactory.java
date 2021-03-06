package dev.westernpine.lib.player.audio.track.userdata.requester;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dv8tion.jda.api.entities.User;

public class RequesterFactory {

    public static Requester from(String id, String name, String discriminator, String avatarUrl) {
        return new Requester(id, name, discriminator, avatarUrl);
    }

    public static Requester from(User user) {
        return new Requester(user.getId(), user.getName(), user.getDiscriminator(), user.getEffectiveAvatarUrl());
    }

    public static JsonObject toJson(Requester requester) {
        if (requester == null)
            return null;
        JsonObject json = new JsonObject();
        json.addProperty("id", requester.id);
        json.addProperty("name", requester.name);
        json.addProperty("discriminator", requester.discriminator);
        json.addProperty("avatarUrl", requester.avatarUrl);
        return json;
    }

    public static Requester fromJson(JsonElement jsonElement) {
        if (jsonElement.isJsonNull())
            return null;
        JsonObject requester = jsonElement.getAsJsonObject();
        String id = requester.get("id").getAsString();
        String name = requester.get("name").getAsString();
        String discriminator = requester.get("discriminator").getAsString();
        String avatarUrl = requester.get("avatarUrl").getAsString();
        return new Requester(id, name, discriminator, avatarUrl);
    }

}
