package dev.westernpine.pulse.controller.settings.backend;

import java.io.Closeable;

public interface SettingsBackend extends Closeable {

    public boolean isAvailable();

    public boolean isClosed();

    public boolean exists(String guildId);

    public String load(String guildId);

    public void update(String guildId, String json);

    public void save(String guildId, String json);
}
