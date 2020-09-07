package com.releasingcode.goldenlobby.database;

import com.releasingcode.goldenlobby.GoldenLobby;
import com.releasingcode.goldenlobby.Utils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

public class Database {
    private static final ArrayList<IDatabase> onCreateTable = new ArrayList<>();
    private final DatabaseConfig dbConfig;
    private HikariDataSource hikariDc;

    public Database(GoldenLobby mc) {
        dbConfig = mc.getDbConfig();
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(dbConfig.getJDBCUrl());
        hikariConfig.setAutoCommit(true);
        if (dbConfig.poolSize > 0) {
            hikariConfig.setMaximumPoolSize(dbConfig.poolSize);
        }
        if (dbConfig.timeout > 0) {
            hikariConfig.setIdleTimeout(dbConfig.timeout);
        }
        hikariConfig.setLeakDetectionThreshold(60 * 1000);
        hikariConfig.setDriverClassName("com.mysql.jdbc.Driver");
        hikariConfig.setDataSourceProperties(dbConfig.getPropierties());
        // para rendimiento
        try {
            this.hikariDc = new HikariDataSource(hikariConfig);
            for (IDatabase call :
                    onCreateTable) {
                call.onCreateTable(this);
                call.onLoaded(this);
            }
            Utils.log("The database has been started");
        } catch (Exception e) {
            Utils.log("&4An error has occurred while connecting to the database [Database Disabled].: " + e.getMessage());
        }

    }

    public static void registerCall(IDatabase callback) {
        onCreateTable.add(callback);
    }

    public DatabaseConfig getDbConfig() {
        return dbConfig;
    }


    public HikariDataSource getHikariDc() {
        return hikariDc;
    }

    public Connection getConnection() throws SQLException {
        return hikariDc.getConnection();
    }


}
