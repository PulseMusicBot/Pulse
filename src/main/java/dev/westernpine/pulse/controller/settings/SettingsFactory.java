package dev.westernpine.pulse.controller.settings;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.westernpine.bettertry.Try;
import dev.westernpine.pulse.Pulse;
import dev.westernpine.pulse.controller.Controller;
import dev.westernpine.pulse.controller.settings.backend.SettingsBackend;
import dev.westernpine.pulse.controller.settings.backend.SqlBackend;
import dev.westernpine.pulse.controller.settings.setting.SettingManager;
import dev.westernpine.pulse.controller.settings.setting.value.SettingValue;
import dev.westernpine.pulse.controller.settings.setting.value.SettingValueFactory;
import dev.westernpine.pulse.properties.IdentityProperties;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static dev.westernpine.pulse.logging.Logger.logger;

public class SettingsFactory {

    private static SettingsBackend backend;

    static {
        backend = new SqlBackend(Pulse.identityProperties.get(IdentityProperties.SETTINGS_SQL_BACKEND), "settings");
        Pulse.shutdownHooks.addLast(() -> {
            if (!backend.isClosed()) {
                logger.info("Closing settings backend.");
                Try.of(() -> backend.close()).onFailure(Throwable::printStackTrace);
            }
        });
    }

    public static Settings from(Controller controller) {
        if (backend.exists(controller.getGuildId()))
            return fromJson(controller, backend.load(controller.getGuildId()));
        else {
            Settings settings = new Settings(controller, backend).loadDefaults(SettingManager.getSettings());
            backend.save(controller.getGuildId(), toJson(settings));
            return settings;
        }
    }

    public static String toJson(Settings settings) {
        JsonObject json = new JsonObject();
        json.addProperty("guildId", settings.controller.getGuildId());
        JsonArray settingsJson = new JsonArray();
        for (SettingValue settingValue : settings.getSettingValues())
            settingsJson.add(SettingValueFactory.toJson(settingValue));
        json.add("settings", settingsJson);
        return json.toString();
    }

    public static Settings fromJson(Controller controller, String json) {
        JsonObject settings = JsonParser.parseString(json).getAsJsonObject();
        return new Settings(controller,
                backend,
                StreamSupport
                        .stream(settings.get("settings").getAsJsonArray().spliterator(), true)
                        .map(jsonElement -> SettingValueFactory.fromJson(jsonElement.getAsString()))
                        .collect(Collectors.toMap(settingValue -> settingValue.getSetting().getUuid(), SettingValue::getValue)));
    }

}
