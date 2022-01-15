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

    public IdentityProperties(String identity) {
        super(identity + ".properties", PropertyFile.getDeclaredProperties(IdentityProperties.class));
    }
    @Override
    public PropertyFile reload() {
        super.reload();
        return this;
    }

    @Override
    public IdentityProperties print() {
        super.print();
        return this;
    }
}
