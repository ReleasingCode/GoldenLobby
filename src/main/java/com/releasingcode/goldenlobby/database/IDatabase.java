package com.releasingcode.goldenlobby.database;

public interface IDatabase {
    void onLoaded(Database database);

    default void onCreateTable(Database database) {

    }
}
