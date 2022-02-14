package dev.westernpine.pulse.controller.backend;

import dev.westernpine.pulse.properties.SqlProperties;
import dev.westernpine.sql.Sql;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static dev.westernpine.pulse.logging.Logger.logger;

public class SqlBackend implements ControllersBackend {

    private Sql sql;

    private String tableName;

    public SqlBackend(String sqlIdentity, String tableName) {
        try {
            this.sql = new SqlProperties(sqlIdentity).toSql();
            this.tableName = tableName;
            this.sql.update("CREATE TABLE IF NOT EXISTS `%s` (`guildId` VARCHAR(255) NOT NULL, `controller` LONGTEXT NOT NULL, PRIMARY KEY(guildId));".formatted(this.tableName));
        } catch (Throwable e) {
            logger.warning("Unable to initialize the controller backend.");
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
    public Map<String, String> load() {
        Map<String, String> controllerMap = new HashMap<>();
        sql.query(rs -> {
            while (rs.next()) controllerMap.put(rs.getString("guildId"), rs.getString("controller"));
        }, "SELECT * FROM `%s`;".formatted(this.tableName));
        return controllerMap;
    }

    @Override
    public void save(Map<String, String> controllerMap) {
        controllerMap.forEach((guildId, controller) -> sql.update("INSERT INTO `%s` VALUES(?,?);".formatted(this.tableName), guildId, controller));
    }

    @Override
    public void clear() {
        sql.update("TRUNCATE TABLE `%s`;".formatted(this.tableName));
    }

    @Override
    public void close() throws IOException {
        if (!isClosed())
            this.sql.close();
    }
}
