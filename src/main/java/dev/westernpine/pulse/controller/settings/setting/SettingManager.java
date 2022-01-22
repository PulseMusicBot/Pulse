package dev.westernpine.pulse.controller.settings.setting;

import java.util.*;

public class SettingManager {

    private static List<Setting> settings = new ArrayList<>();

    static {
        settings.add(Setting.DEFAULT_VOLUME);
        settings.add(Setting.IMAGE_SIZE);
        settings.add(Setting.SHUFFLE_PLAYLISTS);
        settings.add(Setting.DJ_MODE);
        settings.add(Setting.DJ_ROLE);
        settings.add(Setting.VOICE_DETECTION);
        settings.add(Setting.TRACK_UPDATES);
        settings.add(Setting.DISCONNECT_CLEANUP);
        settings.add(Setting.TWENTRY_FOUR_SEVEN);
        settings.add(Setting.JOIN_PLAYLIST);
    }

    public static List<Setting> getSettings() {
        return settings;
    }

    public static Setting get(UUID uuid) {
        return settings.stream().filter(setting -> setting.getUuid().equals(uuid)).findAny().orElse(null);
    }
}
