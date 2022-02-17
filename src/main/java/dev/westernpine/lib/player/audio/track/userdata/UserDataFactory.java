package dev.westernpine.lib.player.audio.track.userdata;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.westernpine.lib.player.audio.track.userdata.platform.Platform;
import dev.westernpine.lib.player.audio.track.userdata.platform.PlatformManager;
import dev.westernpine.lib.player.audio.track.userdata.request.Request;
import dev.westernpine.lib.player.audio.track.userdata.request.RequestFactory;
import dev.westernpine.lib.player.audio.track.userdata.requester.Requester;
import dev.westernpine.lib.player.audio.track.userdata.requester.RequesterFactory;

public class UserDataFactory {

    public static UserData from(Request request, Requester requester, Platform preferredPlatform) {
        return new UserData(request, requester, preferredPlatform);
    }

    public static UserData from(Object userData) {
        return ((UserData) userData);
    }

    public static JsonObject toJson(UserData userData) {
        if (userData == null)
            return null;
        JsonObject json = new JsonObject();
        json.add("request", RequestFactory.toJson(userData.request()));
        json.add("requester", RequesterFactory.toJson(userData.requester()));
        json.add("platform", PlatformManager.toJson(userData.preferredPlatform()));
        return json;
    }

    public static UserData fromJson(JsonElement jsonElement) {
        if (jsonElement.isJsonNull())
            return null;
        JsonObject userData = jsonElement.getAsJsonObject();
        Request request = RequestFactory.fromJson(userData.get("request"));
        Requester requester = RequesterFactory.fromJson(userData.get("requester"));
        Platform platform = PlatformManager.fromJson(userData.get("platform"));
        return from(request, requester, platform);
    }


}
