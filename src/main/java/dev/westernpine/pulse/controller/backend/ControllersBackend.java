package dev.westernpine.pulse.controller.backend;

import java.io.Closeable;
import java.util.Map;

public interface ControllersBackend extends Closeable {

    public boolean isClosed();

    public Map<String, String> load();

    public void save(Map<String, String> controllerMap);

    public void clear();
}
