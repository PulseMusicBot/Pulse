package dev.westernpine.pulse.controller.settings.backend;

import dev.westernpine.lib.object.SQL;
import dev.westernpine.pulse.Pulse;
import dev.westernpine.lib.object.State;
import dev.westernpine.pulse.properties.SqlProperties;

import java.io.IOException;

import static dev.westernpine.pulse.logging.Logger.logger;

public class SqlBackend implements SettingsBackend {

    private SQL sql;

    private String tableName;

    public SqlBackend(String sqlIdentity, String tableName) {
        try {
            this.sql = new SqlProperties(sqlIdentity).toSql();
            this.sql.setDebugging(true);
            this.tableName = tableName;
            this.sql.update("CREATE TABLE IF NOT EXISTS `%s` (`guildId` VARCHAR(255) NOT NULL, `settings` LONGTEXT NOT NULL, PRIMARY KEY(guildId));".formatted(this.tableName));
        } catch (Throwable e) {
            e.printStackTrace();
            logger.severe("Unable to initialize the settings backend.");
            Pulse.setState(State.SHUTDOWN);
        }
        this.tableName = tableName;
    }

    public SQL getSql() {
        return this.sql;
    }

    public String getTableName() {
        return this.tableName;
    }

    @Override
    public boolean isClosed() {
        return !this.sql.getConnection().isOpen();
    }

    @Override
    public boolean exists(String guildId) {
        return sql.query(rs -> {
            try {
                return rs.next();
            } catch (Exception e) {
                if(sql.isDebugging())
                    e.printStackTrace();
            }
            return false;
        }, "SELECT * FROM `%s` WHERE guildId=?;".formatted(this.tableName), guildId);
    }

    @Override
    public String load(String guildId) {
        return sql.query(rs -> {
            try {
                if(rs.next()) {
                    return rs.getString("settings");
                }
            } catch (Exception e) {
                if(sql.isDebugging())
                    e.printStackTrace();
            }
            return null;
        }, "SELECT * FROM `%s` WHERE guildId=?;".formatted(this.tableName), guildId);
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
        if(!isClosed())
            this.sql.getConnection().close();
    }
}
