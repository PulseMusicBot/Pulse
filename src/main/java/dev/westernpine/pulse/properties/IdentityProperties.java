package dev.westernpine.pulse.properties;

import dev.westernpine.lib.properties.PropertiesFile;
import dev.westernpine.lib.properties.Property;
import dev.westernpine.lib.properties.PropertyField;
import dev.westernpine.lib.properties.PropertyFile;

public class IdentityProperties extends PropertiesFile {

    @PropertyField
    public static final Property TOKEN = new Property("token", "TokenHere");

    @PropertyField
    public static final Property PREMIUM = new Property("premium", "false");

    @PropertyField
    public static final Property COLOR = new Property("color", "#000000");

    @PropertyField
    public static final Property IPBLOCKS = new Property("ipblocks", "");

    @PropertyField
    public static final Property LOGGING_SQL_BACKEND = new Property("loggingbackend", "loggingbackend");

    @PropertyField
    public static final Property SETTINGS_SQL_BACKEND = new Property("settingsbackend", "settingsbackend");

    @PropertyField
    public static final Property CONTROLLERS_SQL_BACKEND = new Property("controllersbackend", "controllersbackend");

    @PropertyField
    public static final Property ADMINS = new Property("admins", "559027677017669661");

    @PropertyField
    public static final Property HOST = new Property("host", "pulsebot.gg");

    @PropertyField
    public static final Property WEBSITE = new Property("website", "https://pulsebot.gg");

    @PropertyField
    public static final Property INVITE = new Property("invite", "https://pulsebot.gg/invite");

    @PropertyField
    public static final Property SUPPORT = new Property("support", "https://pulsebot.gg/support");

    public IdentityProperties(String identity) throws Throwable {
        super(identity + ".properties", PropertyFile.getDeclaredProperties(IdentityProperties.class));
    }

    @Override
    public PropertyFile reload() throws Throwable {
        super.reload();
        return this;
    }
}
