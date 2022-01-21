package dev.westernpine.pulse.controller.settings.setting.value;

import dev.westernpine.lib.object.Value;
import dev.westernpine.pulse.controller.settings.setting.Setting;

public class SettingValue {

    private Setting setting;

    private Value value;

    SettingValue(Setting setting, Value value) {
        this.setting = setting;
        this.value = value;
    }

    public Setting getSetting() {
        return setting;
    }

    public Value getValue() {
        return value;
    }

}
