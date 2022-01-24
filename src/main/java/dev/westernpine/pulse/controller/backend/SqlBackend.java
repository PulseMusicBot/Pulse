package dev.westernpine.pulse.controller.backend;

import dev.westernpine.lib.object.SQL;
import dev.westernpine.pulse.Pulse;
import dev.westernpine.lib.object.State;
import dev.westernpine.pulse.properties.SqlProperties;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static dev.westernpine.pulse.logging.Logger.logger;

public class SqlBackend implements ControllersBackend {

    private SQL sql;

    private String tableName;

    public SqlBackend(String sqlIdentity, String tableName) {
        try {
            this.sql = new SqlProperties(sqlIdentity).toSql();
            this.sql.setDebugging(true);
            this.tableName = tableName;
            this.sql.update("CREATE TABLE IF NOT EXISTS `%s` (`guildId` VARCHAR(255) NOT NULL, `controller` LONGTEXT NOT NULL, PRIMARY KEY(guildId));".formatted(this.tableName));
        } catch (Throwable e) {
            e.printStackTrace();
            logger.severe("Unable to initialize the controller backend.");
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
    public Map<String, String> load() {
        Map<String, String> controllerMap = new HashMap<>();
        sql.query(rs -> {
            try {
                while(rs.next()) {
                    controllerMap.put(rs.getString("guildId"), rs.getString("controller"));
                }
            } catch (Exception e) {
                if(sql.isDebugging())
                    e.printStackTrace();
            }
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
        if(!isClosed())
            this.sql.getConnection().close();
    }
}
