package com.releasingcode.goldenlobby.database.builders;

public class Value {
    public String columnName;
    public String value;

    public Value(String columnName, String value) {
        this.columnName = columnName;
        this.value = "'" + value + "'";
    }

    public Value(String columnName, Object i) {
        this.columnName = columnName;
        this.value = String.valueOf(i);
    }

    public String getColumn() {
        return this.columnName;
    }

    public String getValue() {
        return this.value;
    }
}

