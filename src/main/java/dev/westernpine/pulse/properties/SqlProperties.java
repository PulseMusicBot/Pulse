package dev.westernpine.pulse.properties;

import dev.westernpine.lib.object.SQL;
import dev.westernpine.lib.properties.PropertiesFile;
import dev.westernpine.lib.properties.Property;
import dev.westernpine.lib.properties.PropertyField;
import dev.westernpine.lib.properties.PropertyFile;

public class SqlProperties extends PropertiesFile {

    @PropertyField
    public static final Property HOST = new Property("host", "localhost");

    @PropertyField
    public static final Property PORT = new Property("port", "3306");

    @PropertyField
    public static final Property DATABASE = new Property("database", "database");

    @PropertyField
    public static final Property USERNAME = new Property("username", "username");

    @PropertyField
    public static final Property PASSWORD = new Property("password", "password");

    public SqlProperties(String identity) throws Throwable {
        super(identity + ".properties", PropertyFile.getDeclaredProperties(SqlProperties.class));
    }

    @Override
    public PropertyFile reload() throws Throwable {
        super.reload();
        return this;
    }

    @Override
    public SqlProperties print() {
        super.print();
        return this;
    }

    public SQL toSql() {
        return SQL.getBuilder()
                .setIp(this.get(HOST))
                .setPort(this.get(PORT))
                .setDatabase(this.get(DATABASE))
                .setUsername(this.get(USERNAME))
                .setPassword(this.get(PASSWORD))
                .setUseSSLSuffix(true)
                .setUseSSL(false)
                .build();
    }
}
