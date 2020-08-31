package com.releasingcode.goldenlobby.modulos.inventarios.db;

import com.releasingcode.goldenlobby.Utils;
import com.releasingcode.goldenlobby.call.CallBack;
import com.releasingcode.goldenlobby.configuracion.CustomConfiguration;
import com.releasingcode.goldenlobby.database.Database;
import com.releasingcode.goldenlobby.database.IDatabase;
import com.releasingcode.goldenlobby.database.builders.ColumnBuilder;
import com.releasingcode.goldenlobby.database.builders.ColumnMeta;
import com.releasingcode.goldenlobby.database.builders.TableBuilder;
import com.releasingcode.goldenlobby.modulos.inventarios.InventarioPlugin;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;

public class InventoriesDB implements IDatabase {

    public Database database;
    public InventarioPlugin plugin;

    public InventoriesDB(InventarioPlugin plugin) {
        Database.registerCall(this);
        this.plugin = plugin;
    }

    @Override
    public void onLoaded(Database database) {
        this.database = database;
    }

    @Override
    public void onCreateTable(Database database) {
        try (Connection con = database.getConnection();
             Statement statement = con.createStatement()) {
            TableBuilder tableBuilder = new TableBuilder(
                    database.getDbConfig().getTable("Inventory"));
            ColumnBuilder columnBuilder = new ColumnBuilder("id", ColumnBuilder.ColumnType.INT,
                                                            new ColumnMeta().primaryKey().autoIncrement());
            columnBuilder.appendColumn(Inventory_Columns.fileConfiguration,
                                       ColumnBuilder.ColumnType.VARCHAR, new ColumnMeta(255).unique());
            columnBuilder.appendColumn(Inventory_Columns.configuration, ColumnBuilder.ColumnType.MEDIUMTEXT);
            columnBuilder.appendColumn(Inventory_Columns.syncBy, ColumnBuilder.ColumnType.VARCHAR, new ColumnMeta(16));
            columnBuilder.appendColumn(Inventory_Columns.syncDate, ColumnBuilder.ColumnType.BIGINT);
            tableBuilder.columns(columnBuilder);
            tableBuilder.createIfNotExists();
            tableBuilder.index(Inventory_Columns.fileConfiguration);
            int i = statement.executeUpdate(tableBuilder.build());
            if (i > 0) {
                Utils.log("[DB] La tabla del modulo inventarios ha sido creada");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Utils.log("No se pudo conectar a la base de datos [Inventory]");
        }

    }

    public void createConfiguration(String player, CustomConfiguration config, CallBack.SingleCallBack call) throws IOException {
        if (database == null) {
            call.onError();
            return;
        }
        byte[] bytesconfig = Base64.encodeBase64(FileUtils.readFileToByteArray(config.getFile()));
        String base64 = new String(bytesconfig, StandardCharsets.UTF_8);
        long now = new Date().getTime();
        String SQL = "INSERT INTO " + this.database.getDbConfig().getTable("Inventory") + "(" + Inventory_Columns.fileConfiguration + ","
                + Inventory_Columns.configuration + ","
                + Inventory_Columns.syncBy + ","
                + Inventory_Columns.syncDate
                + ") VALUES(?,?,?,?) ON DUPLICATE KEY UPDATE " + Inventory_Columns.fileConfiguration + "=?, "
                + Inventory_Columns.configuration + "=?, " + Inventory_Columns.syncBy + "=?, " + Inventory_Columns.syncDate + "=?";
        try (Connection con = this.database.getConnection();
             PreparedStatement prestat = con.prepareStatement(SQL)) {
            prestat.setString(1, config.getFile().getName().replace(".yml", ""));
            prestat.setString(2, base64);
            prestat.setString(3, player);
            prestat.setLong(4, now);
            prestat.setString(5, config.getFile().getName().replace(".yml", ""));
            prestat.setString(6, base64);
            prestat.setString(7, player);
            prestat.setLong(8, now);
            prestat.executeUpdate();
            call.onSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            call.onError();
        }
    }

    public void fetchInventories(CallBack.ReturnCallBack<ArrayList<InventoryFetch>> callBack) {
        if (database == null) {
            return;
        }
        String SQL = "SELECT * FROM " + database.getDbConfig().getTable("Inventory");
        ArrayList<InventoryFetch> fetching = new ArrayList<>();
        try (Connection con = this.database.getConnection();
             PreparedStatement prestat = con.prepareStatement(SQL)) {
            try (ResultSet set = prestat.executeQuery()) {
                while (set.next()) {
                    String name = set.getString(Inventory_Columns.fileConfiguration);
                    String base64 = set.getString(Inventory_Columns.configuration);
                    InventoryFetch fetch = new InventoryFetch(name);
                    fetch.setBase64(base64);
                    fetching.add(fetch);

                }
                callBack.onSuccess(fetching);
            }
        } catch (Exception e) {
            callBack.onError(fetching);
        }
    }
}
