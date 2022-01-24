package dev.westernpine.pulse.controller.settings.setting;

import dev.westernpine.pulse.Pulse;

import java.util.UUID;

public class SettingFactory {

    public static Setting from(UUID uuid, String emoji, String label, String description, String defaultValue, String... ids) {
        return new Setting(uuid, emoji, label, description, defaultValue, ids);
    }

    public static String toJson(Setting setting) {
        return Pulse.gson.toJson(setting);
    }

    public Setting fromJson(String json) {
        return Pulse.gson.fromJson(json, Setting.class);
    }
}
