package com.releasingcode.goldenlobby.database.builders;

public class TableBuilder {
    private final String tableName;
    private boolean ifNotExists = false;
    private String columns;
    private String index = null;

    public TableBuilder(String tableName) {
        this.tableName = tableName;
    }

    public TableBuilder createIfNotExists() {
        this.ifNotExists = true;
        return this;
    }

    public TableBuilder columns(ColumnBuilder columns) {
        this.columns = columns.build();
        return this;
    }

    public TableBuilder index(String column) {
        this.index = column;
        return this;
    }

    public TableBuilder columns(String columns) {
        this.columns = columns;
        return this;
    }

    public String build() {
        if (this.columns == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder("CREATE TABLE ");
        if (this.ifNotExists) {
            builder.append("IF NOT EXISTS ");
        }
        builder.append(this.tableName);
        builder.append(" (").append(this.columns).append(index != null ? (", INDEX(" + index + ")") : (""));
        builder.append(" ) ENGINE=InnoDB");
        return builder.toString();
    }
}

