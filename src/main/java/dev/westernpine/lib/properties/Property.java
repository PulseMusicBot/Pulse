package dev.westernpine.lib.properties;

public class Property {

    private final String identifier;

    private final String defaultValue;

    public Property(String identifier, String defaultValue) {
        this.identifier = identifier;
        this.defaultValue = defaultValue;
    }

    public String get(PropertyFile propertyFile) {
        return propertyFile.get(this);
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public String getDefaultValue() {
        return this.defaultValue;
    }

}
