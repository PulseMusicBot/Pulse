package dev.westernpine.lib.player.audio.track.userdata;

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

    public static String toJson(UserData userData) {
        if (userData == null)
            return "";
        JsonObject json = new JsonObject();
        json.addProperty("request", RequestFactory.toJson(userData.request()));
        json.addProperty("requester", RequesterFactory.toJson(userData.requester()));
        json.addProperty("platform", PlatformManager.toJson(userData.preferredPlatform()));
        return json.toString();
    }

    public static UserData fromJson(String json) {
        if (json.isEmpty())
            return null;
        JsonObject userData = JsonParser.parseString(json).getAsJsonObject();
        Request request = RequestFactory.fromJson(userData.get("request").getAsString());
        Requester requester = RequesterFactory.fromJson(userData.get("requester").getAsString());
        Platform platform = PlatformManager.fromJson(userData.get("platform").getAsString());
        return from(request, requester, platform);
    }


}
