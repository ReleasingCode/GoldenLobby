package com.releasingcode.goldenlobby.modulos.cooldown.db;

import com.releasingcode.goldenlobby.Utils;
import com.releasingcode.goldenlobby.call.CallBack;
import com.releasingcode.goldenlobby.database.Database;
import com.releasingcode.goldenlobby.database.IDatabase;
import com.releasingcode.goldenlobby.database.builders.ColumnBuilder;
import com.releasingcode.goldenlobby.database.builders.ColumnMeta;
import com.releasingcode.goldenlobby.database.builders.TableBuilder;
import com.releasingcode.goldenlobby.modulos.cooldown.CooldownPlugin;
import com.releasingcode.goldenlobby.modulos.cooldown.object.CooldownSystem;

import java.sql.*;
import java.util.ArrayList;

public class CooldownDB implements IDatabase {

    public static String nombre = "nombre";
    public static String cooldownString = "cooldownString";
    public static String finishAt = "finishAt";
    public static String startedAt = "startedAt";
    public static String status = "status";

    public Database database;
    public CooldownPlugin plugin;

    public CooldownDB(CooldownPlugin plugin) {
        Database.registerCall(this);
        this.plugin = plugin;
    }

    @Override
    public void onLoaded(Database database) {
        this.database = database;
        plugin.loadCooldown();
    }

    @Override
    public void onCreateTable(Database database) {
        try (Connection con = database.getConnection();
             Statement statement = con.createStatement()) {
            TableBuilder tableBuilder = new TableBuilder(
                    database.getDbConfig().getTable("Cooldown"));
            ColumnBuilder columnBuilder = new ColumnBuilder("id", ColumnBuilder.ColumnType.INT,
                    new ColumnMeta().primaryKey().autoIncrement());
            columnBuilder.appendColumn(nombre,
                    ColumnBuilder.ColumnType.VARCHAR, new ColumnMeta(255).unique());
            columnBuilder.appendColumn(cooldownString, ColumnBuilder.ColumnType.VARCHAR, new ColumnMeta(255));
            columnBuilder.appendColumn(finishAt, ColumnBuilder.ColumnType.VARCHAR, new ColumnMeta(255));
            columnBuilder.appendColumn(startedAt, ColumnBuilder.ColumnType.VARCHAR, new ColumnMeta(255));
            columnBuilder.appendColumn(status, ColumnBuilder.ColumnType.VARCHAR, new ColumnMeta(255));
            tableBuilder.columns(columnBuilder);
            tableBuilder.createIfNotExists();
            int i = statement.executeUpdate(tableBuilder.build());
            if (i > 0) {
                Utils.log("[DB] The cooldown module table has been created");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Utils.log("Could not connect to the [CooldownPlugin] database");
        }
    }

    public void createOrUpdate(CooldownSystem cooldownSystem, CallBack.SingleCallBack call) {
        if (database == null) {
            return;
        }
        String SQL = "INSERT INTO " + this.database.getDbConfig().getTable("Cooldown") + "("
                + nombre + ","
                + cooldownString + ","
                + finishAt + ","
                + startedAt + ","
                + status
                + ") VALUES(?,?,?,?,?) ON DUPLICATE KEY UPDATE "
                + cooldownString + "=?, "
                + finishAt + "=?, "
                + startedAt + "=?,"
                + status + "=?";
        try (Connection con = this.database.getConnection();
             PreparedStatement prestat = con.prepareStatement(SQL)) {
            prestat.setString(1, cooldownSystem.getName().toLowerCase());
            prestat.setString(2, cooldownSystem.getCooldownString());
            prestat.setString(3, cooldownSystem.getFinishAt() + "");
            prestat.setString(4, cooldownSystem.getStartedAt() + "");
            prestat.setString(5, cooldownSystem.getStatus().name() + "");
            prestat.setString(6, cooldownSystem.getCooldownString());
            prestat.setString(7, cooldownSystem.getFinishAt() + "");
            prestat.setString(8, cooldownSystem.getStartedAt() + "");
            prestat.setString(9, cooldownSystem.getStatus().name() + "");
            prestat.executeUpdate();
            call.onSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            call.onError();
        }
    }


    public void deleteCooldown(String name, CallBack.ReturnCallBack<Integer> callback) {
        String SQL = "DELETE FROM " + this.database.getDbConfig().getTable("Cooldown") + " WHERE " + nombre + "=?";
        try (Connection con = this.database.getConnection(); PreparedStatement preparedStatement = con.prepareStatement(SQL)) {
            preparedStatement.setString(1, name);
            int rowsAffected = preparedStatement.executeUpdate();
            callback.onSuccess(rowsAffected);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            callback.onError(0);
        }
    }

    public void fetchCooldown(CallBack.ReturnCallBack<ArrayList<CooldownSystem>> callBack) {
        if (database == null) {
            Utils.log("CooldownPlugin", "Database is null");
            return;
        }
        String SQL = "SELECT * FROM " + database.getDbConfig().getTable("Cooldown");
        try (Connection con = this.database.getConnection();
             PreparedStatement prestat = con.prepareStatement(SQL)) {
            try (ResultSet set = prestat.executeQuery()) {
                ArrayList<CooldownSystem> cooldowns = new ArrayList<>();
                while (set.next()) {
                    String nombreSystem = set.getString(nombre);
                    String cooldownStringSystem = set.getString(cooldownString);
                    long finishAtSystem = set.getLong(finishAt);
                    long startedAtSystem = set.getLong(startedAt);
                    String statusSystem = set.getString(status);
                    try {
                        CooldownSystem system = new CooldownSystem(nombreSystem);
                        system.setCooldownString(cooldownStringSystem);
                        system.setFinishAt(finishAtSystem);
                        system.setStartedAt(startedAtSystem);
                        system.setStatus(CooldownSystem.CooldownStatus.valueOf(statusSystem));
                        cooldowns.add(system);
                        Utils.log("Adding new CooldownSystem: " + system.getName());
                    } catch (Exception e) {
                        Utils.log("&cCouldn't load the cooldown: " + nombreSystem + " Error: " + e.getMessage());
                    }
                }
                callBack.onSuccess(cooldowns);
            }
        } catch (Exception e) {
            e.printStackTrace();
            callBack.onError(null);
        }
    }
}
