package dev.westernpine.pulse.controller.settings.backend;

import dev.westernpine.pulse.properties.SqlProperties;
import dev.westernpine.sql.Sql;

import java.io.IOException;
import java.sql.ResultSet;

import static dev.westernpine.pulse.logging.Logger.logger;

public class SqlBackend implements SettingsBackend {

    private Sql sql;

    private String tableName;

    public SqlBackend(String sqlIdentity, String tableName) {
        try {
            this.sql = new SqlProperties(sqlIdentity).toSql();
            this.tableName = tableName;
            this.sql.update("CREATE TABLE IF NOT EXISTS `%s` (`guildId` VARCHAR(255) NOT NULL, `settings` LONGTEXT NOT NULL, PRIMARY KEY(guildId));".formatted(this.tableName));
        } catch (Throwable e) {
            logger.warning("Unable to initialize the settings backend.");
        }
    }

    public Sql getSql() {
        return this.sql;
    }

    public String getTableName() {
        return this.tableName;
    }

    @Override
    public boolean isAvailable() {
        return sql.canConnect();
    }

    @Override
    public boolean isClosed() {
        return !this.sql.isConnected();
    }

    @Override
    public boolean exists(String guildId) {
        return sql.query(ResultSet::next, "SELECT * FROM `%s` WHERE guildId=?;".formatted(this.tableName), guildId).orElse(false);
    }

    @Override
    public String load(String guildId) {
        return sql.query(rs -> rs.next() ? rs.getString("settings") : null, "SELECT * FROM `%s` WHERE guildId=?;".formatted(this.tableName), guildId).orElse(null);
    }

    @Override
    public void update(String guildId, String json) {
        sql.update("UPDATE `%s` SET settings=? WHERE guildId=?;".formatted(this.tableName), json, guildId);
    }

    @Override
    public void save(String guildId, String json) {
        sql.update("INSERT INTO `%s` VALUES(?,?);".formatted(this.tableName), guildId, json);
    }

    @Override
    public void close() throws IOException {
        if (!isClosed())
            this.sql.close();
    }
}
