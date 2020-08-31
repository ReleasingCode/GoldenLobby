package com.releasingcode.goldenlobby.database;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.Properties;

public class DatabaseConfig {
    public final String host;
    public final String databaseName;
    public final int poolSize;
    public final long timeout;
    public Properties properties;
    public FileConfiguration fileConfiguration;

    public DatabaseConfig(FileConfiguration config) {
        this.fileConfiguration = config;
        properties = new Properties();
        this.host = config.getString("MySQL.Host");
        this.databaseName = config.getString("MySQL.DatabaseName");
        this.poolSize = config.getInt("MySQL.PoolSize", -1);
        this.timeout = config.getInt("MySQL.TimeOut", 30000);
        String userName = config.getString("MySQL.UserName", "root");
        String password = config.getString("MySQL.PassWord", "");
        properties.put("user", userName);
        properties.put("password", password);
        properties.put("useServerPrepStmts", true);
        properties.put("cacheServerConfiguration", true);
        properties.put("alwaysSendSetIsolation", false);
        properties.put("useLocalSessionState", true);
        properties.put("elideSetAutoCommits", true);
        properties.put("maintainTimeStats", false);
        properties.put("cachePrepStmts", true);
        properties.put("prepStmtCacheSize", 250);
        properties.put("prepStmtCacheSqlLimit", 2048);
        properties.put("cacheResultSetMetadata", true);
        properties.put("tcpKeepAlive", true);
    }

    public String getJDBCUrl() {
        //jdbc:mysql://localhost:3306/dabasename
        return "jdbc:mysql://" + host + "/" + this.databaseName + "?allowMultiQueries=true";
    }


    public String getTable(String key) {
        return fileConfiguration.getString("MySQL.Modules." + key + ".TableName", null);
    }


    public Properties getPropierties() {
        return properties;
    }
}
