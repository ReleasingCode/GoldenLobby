package com.releasingcode.goldenlobby.database.builders;

public class ColumnMeta {
    private Integer columnSize;
    private String defaultValue;
    private boolean autoIncrement;
    private boolean primaryKey;
    private boolean notNull;
    private boolean unique;

    public ColumnMeta() {
    }

    public ColumnMeta(int columnSize) {
        this.columnSize = columnSize;
    }

    public ColumnMeta defaultValue(String defaultValue) {
        this.defaultValue = "'" + defaultValue + "'";
        return this;
    }

    public ColumnMeta defaultValue(Object defaultValue) {
        this.defaultValue = String.valueOf(defaultValue);
        return this;
    }

    public ColumnMeta autoIncrement() {
        this.autoIncrement = true;
        return this;
    }

    public ColumnMeta primaryKey() {
        this.primaryKey = true;
        return this;
    }

    public ColumnMeta notNull() {
        this.notNull = true;
        return this;
    }

    public ColumnMeta unique() {
        this.unique = true;
        return this;
    }

    public String build() {
        StringBuilder builder = new StringBuilder();
        if (this.columnSize != null) {
            builder.append("(").append(this.columnSize).append(")");
        }
        if (this.defaultValue != null) {
            builder.append(" DEFAULT ").append(this.defaultValue);
        }
        if (this.primaryKey) {
            builder.append(" PRIMARY KEY");
        }
        if (this.autoIncrement) {
            builder.append(" AUTO_INCREMENT");
        }
        if (this.notNull) {
            builder.append(" NOT NULL");
        }
        if (this.unique) {
            builder.append(" UNIQUE");
        }
        return builder.toString();
    }
}

