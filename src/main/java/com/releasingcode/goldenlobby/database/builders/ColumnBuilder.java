package com.releasingcode.goldenlobby.database.builders;

public class ColumnBuilder {

    private final StringBuilder builder;

    public ColumnBuilder(String column) {
        builder = new StringBuilder(column);
    }

    public ColumnBuilder(String columnName, ColumnType type) {
        this("`" + columnName + "` " + type.toString());
    }

    public ColumnBuilder(String columnName, ColumnType type, ColumnMeta meta) {
        this("`" + columnName + "` " + type.toString() + meta.build());
    }

    public ColumnBuilder(String columnName, ColumnType type, String meta) {
        this("`" + columnName + "` " + type.toString() + meta);
    }

    public ColumnBuilder appendColumn(String str) {
        builder.append(", ").append(str);
        return this;
    }

    public ColumnBuilder appendColumn(String columnName, ColumnType type) {
        appendColumn("`" + columnName + "` " + type.toString());
        return this;
    }

    public ColumnBuilder appendColumn(String columnName, ColumnType type, ColumnMeta meta) {
        appendColumn("`" + columnName + "` " + type.toString() + meta.build());
        return this;
    }

    public ColumnBuilder appendColumn(String columnName, ColumnType type, String meta) {
        appendColumn("`" + columnName + "` " + type.toString() + meta);
        return this;
    }

    public String build() {
        return builder.toString();
    }

    public enum ColumnType {
        CHAR, VARCHAR, TINYTEXT, TEXT, BLOB, MEDIUMTEXT, MEDIUMBLOB, LONGTEXT, LONGBLOB, ENUM, TINYINT, SMALLINT, MEDIUMINT, INT, BIGINT, FLOAT, DOUBLE, DECIMAL, DATE, DATETIME, TIMESTAMP, TIME, YEAR, VARBINARY, BIT
    }

}

