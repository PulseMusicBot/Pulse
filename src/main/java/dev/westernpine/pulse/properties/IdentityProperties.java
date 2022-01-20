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
    public static final Property HOST = new Property("host", "pulsebot.gg");

    @PropertyField
    public static final Property COLOR = new Property("color", "#000000");

    @PropertyField
    public static final Property IPBLOCKS = new Property("ipblocks", "");

    public IdentityProperties(String identity) throws Throwable {
        super(identity + ".properties", PropertyFile.getDeclaredProperties(IdentityProperties.class));
    }
    @Override
    public PropertyFile reload() throws Throwable {
        super.reload();
        return this;
    }

    @Override
    public IdentityProperties print() {
        super.print();
        return this;
    }
}
