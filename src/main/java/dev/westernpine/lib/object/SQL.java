package dev.westernpine.lib.object;

import dev.westernpine.bettertry.Try;
import dev.westernpine.bettertry.functions.TryConsumer;
import dev.westernpine.bettertry.functions.TryFunction;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;

public class SQL {

    private SQLBuilder connection;
    private boolean debugging = false;

    SQL(SQLBuilder connection) {
        this.connection = connection;
    }

    public static SQLBuilder getBuilder() {
        return new SQLBuilder();
    }

    public SQLBuilder getConnection() {
        return connection;
    }

    public boolean isDebugging() {
        return debugging;
    }

    public void setDebugging(boolean debugging) {
        this.debugging = debugging;
    }

    public boolean canConnect() {
        if (connection.isOpen())
            return true;
        return Try.to(() -> connection.open()).map(con -> !con.isClosed()).orElse(false);
    }

    public Set<String> getTables() {
        Set<String> tables = new HashSet<>();
        boolean wasOpen = connection.isOpen();
        try (PreparedStatement sql = connection.open().prepareStatement("SHOW TABLES;")) {
            try (ResultSet rs = sql.executeQuery()) {
                while (rs.next())
                    tables.add(rs.getString("tables_in_" + getConnection().getDatabase()));
            } catch (Exception e) {
                if (debugging)
                    e.printStackTrace();
            }
        } catch (Exception e) {
            if (debugging)
                e.printStackTrace();
        }
        if (!wasOpen)
            connection.close();
        return tables;
    }

    private void closeConnection(boolean wasOpen) {
        if (!wasOpen)
            Try.to(connection::close);
    }

    public Try<Integer> update(String statement, Object... values) {
        boolean wasOpen = connection.isOpen();
        return Try.to(() -> connection.open())
                .onFailure(throwable -> closeConnection(wasOpen))
                .flatMap(con -> Try.to(() -> con.prepareStatement(statement))
                        .onFailure(throwable -> closeConnection(wasOpen))
                        .flatMap(sql -> Try.to(() -> {
                                    for (int i = 0; i < values.length; i++)
                                        sql.setObject(i + 1, values[i]);
                                    return sql.executeUpdate();
                                })
                                .onFailure(throwable -> {
                                    Try.to(sql::close);
                                    closeConnection(wasOpen);
                                })
                                .onSuccess(affected -> {
                                    Try.to(sql::close);
                                    closeConnection(wasOpen);
                                })));
    }

    public void query(TryConsumer<ResultSet> resultHandler, String statement, Object... values) {
        query(rs -> {
            resultHandler.accept(rs);
            return null;
        }, statement, values);
    }

    public <T> Try<T> query(TryFunction<ResultSet, T> resultHandler, String statement, Object... values) {
        boolean wasOpen = connection.isOpen();
        return Try.to(() -> connection.open())
                .onFailure(throwable -> closeConnection(wasOpen))
                .flatMap(con -> Try.to(() -> con.prepareStatement(statement))
                        .onFailure(throwable -> closeConnection(wasOpen))
                        .flatMap(sql -> Try.to(() -> {
                                    for (int i = 0; i < values.length; i++)
                                        sql.setObject(i + 1, values[i]);
                                    return sql.executeQuery();
                                })
                                .onFailure(throwable -> {
                                    Try.to(sql::close);
                                    closeConnection(wasOpen);
                                })
                                .flatMap(rs -> Try.to(() -> resultHandler.apply(rs))
                                        .onFailure(throwable -> {
                                            Try.to(rs::close);
                                            Try.to(sql::close);
                                            closeConnection(wasOpen);
                                        })
                                        .onSuccess(t -> {
                                            Try.to(rs::close);
                                            Try.to(sql::close);
                                            closeConnection(wasOpen);
                                        }))));
    }


    public enum DatabaseType {

        MYSQL("jdbc:mysql"),
        REDIS("jdbc:redis"),
        POSTGRES("jdbc:postgresql"),
        SQL("jdbc:postgresql"),
        MARIADB("jdbc:mariadb"),
        DB2EXPRESSC("jdbc:db2"),
        SAPHANA("jdbc:sap"),
        INFORMIX("jdbc:informix-sqli"),
        ;

        private String connectionPrefix;

        DatabaseType(String connectionPrefix) {
            this.connectionPrefix = connectionPrefix;
        }

        @Override
        public String toString() {
            return this.connectionPrefix;
        }

    }

    public static class SQLBuilder {

        private DatabaseType type;
        private String ip;
        private String port;
        private String database;
        private String username;
        private String password;
        private boolean useSSLSuffix = true;
        private boolean useSSL = false;
        private Connection connection;

        private SQLBuilder() {
            this.type = DatabaseType.MYSQL;
            this.ip = "localhost";
            this.port = "3306";
            this.database = "database";
            this.username = "root";
            this.password = "admin";
        }

        public SQL build() {
            return new SQL(this);
        }

        public boolean isOpen() {
            try {
                return (connection != null && !connection.isClosed());
            } catch (Exception e) {
            }
            return false;
        }

        public Connection open() {

            try {
                if (isOpen())
                    return connection;
            } catch (Exception e) {
            }

            try {
                connection = DriverManager.getConnection(toString(), username, password);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return connection;
        }

        public void close() {
            try {
                if (isOpen())
                    connection.close();
            } catch (Exception e) {
            } finally {
                try {
                    connection.close();
                } catch (Exception e) {
                }
            }
            connection = null;
        }

        @Override
        public String toString() {
            return type.toString() + "://" + ip + ":" + port + "/" + database + (useSSLSuffix ? (useSSL ? "?useSSL=true" : "?useSSL=false") : "");
        }

        public String toUnprotectedString() {
            return type.toString() + ":" + username + "/" + password + "@//" + ip + ":" + port + "/" + database + (useSSLSuffix ? (useSSL ? "?useSSL=true" : "?useSSL=false") : "");
        }

        public DatabaseType getType() {
            return type;
        }

        public String getIp() {
            return ip;
        }

        public SQLBuilder setIp(String ip) {
            this.ip = ip;
            return this;
        }

        public String getPort() {
            return port;
        }

        public SQLBuilder setPort(String port) {
            this.port = port;
            return this;
        }

        public String getDatabase() {
            return database;
        }

        public SQLBuilder setDatabase(String database) {
            this.database = database;
            return this;
        }

        public String getUsername() {
            return username;
        }

        public SQLBuilder setUsername(String username) {
            this.username = username;
            return this;
        }

        public String getPassword() {
            return password;
        }

        public SQLBuilder setPassword(String password) {
            this.password = password;
            return this;
        }

        public boolean isUseSSLSuffix() {
            return useSSLSuffix;
        }

        public SQLBuilder setUseSSLSuffix(boolean useSSLSuffix) {
            this.useSSLSuffix = useSSLSuffix;
            return this;
        }

        public boolean isUseSSL() {
            return useSSL;
        }

        public SQLBuilder setUseSSL(boolean useSSL) {
            this.useSSL = useSSL;
            return this;
        }

        public Connection getConnection() {
            return connection;
        }

        public SQLBuilder setDatabaseType(DatabaseType type) {
            this.type = type;
            return this;
        }

    }

}