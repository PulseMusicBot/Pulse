package dev.westernpine.lib.object;

import dev.westernpine.bettertry.Try;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public class SQL {

    private SQLBuilder connection;
    private boolean debugging = false;

    public static SQLBuilder getBuilder() {
        return new SQLBuilder();
    }

    SQL(SQLBuilder connection) {
        this.connection = connection;
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
        if(connection.isOpen())
            return true;
        try(Connection con = connection.open()) {
            return !con.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public Set<String> getTables() {
        Set<String> tables = new HashSet<>();
        boolean wasOpen = connection.isOpen();
        try(PreparedStatement sql = connection.open().prepareStatement("SHOW TABLES;")) {
            try(ResultSet rs = sql.executeQuery()) {
                while(rs.next())
                    tables.add(rs.getString("tables_in_" + getConnection().getDatabase()));
            }
        } catch(Exception e) {
            if(debugging)
                e.printStackTrace();
        }
        if(!wasOpen)
            connection.close();
        return tables;
    }

    public <T> T update(Function<Integer, T> affected, String statement, Object...values) {
        boolean wasOpen = connection.isOpen();
        T toReturn = null;
        try(PreparedStatement sql = connection.open().prepareStatement(statement)) {
            for (int i = 0; i < values.length;i++)
                sql.setObject(i + 1, values[i]);
            int a = sql.executeUpdate();
            if(affected != null)
                toReturn = affected.apply(a);
        } catch(Exception e) {
            if(debugging)
                e.printStackTrace();
        }
        if(!wasOpen)
            connection.close();
        return toReturn;
    }

    public void update(Consumer<Integer> affected, String statement, Object...values) {
        boolean wasOpen = connection.isOpen();
        try(PreparedStatement sql = connection.open().prepareStatement(statement)) {
            for (int i = 0; i < values.length;i++)
                sql.setObject(i + 1, values[i]);
            int a = sql.executeUpdate();
            if(affected != null)
                affected.accept(a);
        } catch(Exception e) {
            if(debugging)
                e.printStackTrace();
        }
        if(!wasOpen)
            connection.close();
    }

    public void update(String statement, Object...values) {
        boolean wasOpen = connection.isOpen();
        try(PreparedStatement sql = connection.open().prepareStatement(statement)) {
            for (int i = 0; i < values.length;i++)
                sql.setObject(i + 1, values[i]);
            sql.executeUpdate();
        } catch(Exception e) {
            if(debugging)
                e.printStackTrace();
        }
        if(!wasOpen)
            connection.close();
    }

    public <T> T query(Function<ResultSet, T> resultSet, String statement, Object...values) {
        boolean wasOpen = connection.isOpen();
        T toReturn = null;
        try(PreparedStatement sql = connection.open().prepareStatement(statement)) {
            for (int i = 0; i < values.length;i++)
                sql.setObject(i + 1, values[i]);
            try(ResultSet rs = sql.executeQuery()) {
                toReturn = resultSet.apply(rs);
            }
        } catch(Exception e) {
            if(debugging)
                e.printStackTrace();
        }
        if(!wasOpen)
            connection.close();
        return toReturn;
    }

    public void query(Consumer<ResultSet> resultSet, String statement, Object...values) {
        boolean wasOpen = connection.isOpen();
        try(PreparedStatement sql = connection.open().prepareStatement(statement)) {
            for (int i = 0; i < values.length;i++)
                sql.setObject(i + 1, values[i]);
            try(ResultSet rs = sql.executeQuery()) {
                resultSet.accept(rs);
            }
        } catch(Exception e) {
            if(debugging)
                e.printStackTrace();
        }
        if(!wasOpen)
            connection.close();
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
            } catch(Exception e) {}
            return false;
        }

        public Connection open() {

            try {
                if(isOpen())
                    return connection;
            } catch(Exception e) {}

            try {
                connection = DriverManager.getConnection(toString(), username, password);
            } catch(Exception e) {
                e.printStackTrace();
            }

            return connection;
        }

        public void close() {
            try {
                if(isOpen())
                    connection.close();
            } catch (Exception e) {} finally {
                try {
                    connection.close();
                } catch (Exception e) {}
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

        public String getPort() {
            return port;
        }

        public String getDatabase() {
            return database;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public boolean isUseSSLSuffix() {
            return useSSLSuffix;
        }

        public boolean isUseSSL() {
            return useSSL;
        }

        public Connection getConnection() {
            return connection;
        }

        public SQLBuilder setDatabaseType(DatabaseType type) {
            this.type = type;
            return this;
        }

        public SQLBuilder setIp(String ip) {
            this.ip = ip;
            return this;
        }

        public SQLBuilder setPort(String port) {
            this.port = port;
            return this;
        }

        public SQLBuilder setDatabase(String database) {
            this.database = database;
            return this;
        }

        public SQLBuilder setUsername(String username) {
            this.username = username;
            return this;
        }

        public SQLBuilder setPassword(String password) {
            this.password = password;
            return this;
        }

        public SQLBuilder setUseSSLSuffix(boolean useSSLSuffix) {
            this.useSSLSuffix = useSSLSuffix;
            return this;
        }

        public SQLBuilder setUseSSL(boolean useSSL) {
            this.useSSL = useSSL;
            return this;
        }

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

}