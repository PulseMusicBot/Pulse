package dev.westernpine.pulse.controller.settings.setting.value;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.westernpine.lib.object.Value;
import dev.westernpine.pulse.controller.settings.setting.Setting;
import dev.westernpine.pulse.controller.settings.setting.SettingManager;

import java.util.UUID;

public class SettingValueFactory {

    public static SettingValue from(Setting setting, Value value) {
        return new SettingValue(setting, value);
    }

    public static String toJson(SettingValue settingValue) {
        JsonObject json = new JsonObject();
        json.addProperty("uuid", settingValue.getSetting().getUuid().toString());
        json.addProperty("value", settingValue.getValue().toString());
        return json.toString();
    }

    public static SettingValue fromJson(String json) {
        JsonObject settingValue = JsonParser.parseString(json).getAsJsonObject();
        Setting setting = SettingManager.get(UUID.fromString(settingValue.get("uuid").getAsString()));
        Value value = Value.of(settingValue.get("value").getAsString());
        return from(setting, value);
    }

}
