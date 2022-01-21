package dev.westernpine.pulse.controller.settings;

import dev.westernpine.lib.object.Value;
import dev.westernpine.pulse.controller.Controller;
import dev.westernpine.pulse.controller.settings.backend.SettingsBackend;
import dev.westernpine.pulse.controller.settings.setting.Setting;
import dev.westernpine.pulse.controller.settings.setting.SettingManager;
import dev.westernpine.pulse.controller.settings.setting.value.SettingValue;
import dev.westernpine.pulse.controller.settings.setting.value.SettingValueFactory;

import java.util.*;

public class Settings {

    Controller controller;

    private SettingsBackend backend;

    private Map<UUID, Value> settings = new HashMap<>();

    public Settings(Controller controller, SettingsBackend backend) {
        this.controller = controller;
        this.backend = backend;
    }

    public Settings(Controller controller, SettingsBackend backend, Map<UUID, Value> settings) {
        this.controller = controller;
        this.backend = backend;
        this.settings = settings;
    }

    public Map<UUID, Value> getSettings() {
        return settings;
    }

    public List<SettingValue> getSettingValues() {
        return settings.entrySet().stream().map(entry -> SettingValueFactory.from(SettingManager.get(entry.getKey()), entry.getValue())).toList();
    }

    public boolean exists(Setting setting) {
        return this.settings.get(setting.getUuid()) != null;
    }

    public Settings load(Setting setting, Value value) {
        this.settings.put(setting.getUuid(), value);
        return this;
    }

    public Settings loadDefaults(Collection<Setting> settings) {
        settings.forEach(setting -> this.settings.put(setting.getUuid(), Value.of(setting.getDefaultValue())));
        return this;
    }

    public Value get(Setting setting) {
        boolean exists = exists(setting);
        Value value = exists ? this.settings.get(setting.getUuid()) : Value.of(setting.getDefaultValue());
        if(!exists)
            this.settings.put(setting.getUuid(), value);
        return value;
    }

    public Settings update() {
        String json = SettingsFactory.toJson(this);
        if(this.backend.exists(controller.getGuildId()))
            this.backend.update(controller.getGuildId(), json);
        else
            this.backend.save(controller.getGuildId(), json);
        return this;
    }

    public Settings set(Setting setting, Value value) {
        this.settings.put(setting.getUuid(), value);
        return update();
    };



}
