package com.releasingcode.goldenlobby.modulos.npcserver.db.mysql;

import com.releasingcode.goldenlobby.Utils;
import com.releasingcode.goldenlobby.call.CallBack;
import com.releasingcode.goldenlobby.configuracion.CustomConfiguration;
import com.releasingcode.goldenlobby.database.Database;
import com.releasingcode.goldenlobby.database.IDatabase;
import com.releasingcode.goldenlobby.database.builders.ColumnBuilder;
import com.releasingcode.goldenlobby.database.builders.ColumnMeta;
import com.releasingcode.goldenlobby.database.builders.TableBuilder;
import com.releasingcode.goldenlobby.modulos.npcserver.NPCServerPlugin;
import com.releasingcode.goldenlobby.modulos.npcserver.db.NPCFetch;
import com.releasingcode.goldenlobby.modulos.npcserver.db.NPC_Columns;
import com.releasingcode.goldenlobby.npc.api.NPC;
import com.releasingcode.goldenlobby.npc.internal.NPCManager;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;

public class NPCDB implements IDatabase {
    private final NPCServerPlugin plugin;
    private Database database;

    public NPCDB(NPCServerPlugin plugin) {
        this.plugin = plugin;
        Database.registerCall(this);
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
                    database.getDbConfig().getTable("NPC"));
            ColumnBuilder columnBuilder = new ColumnBuilder("id", ColumnBuilder.ColumnType.INT,
                                                            new ColumnMeta().primaryKey().autoIncrement());
            columnBuilder.appendColumn(NPC_Columns.fileConfiguration,
                                       ColumnBuilder.ColumnType.VARCHAR, new ColumnMeta(255).unique());
            columnBuilder.appendColumn(NPC_Columns.configuration, ColumnBuilder.ColumnType.MEDIUMTEXT);
            columnBuilder.appendColumn(NPC_Columns.directory, ColumnBuilder.ColumnType.VARCHAR, new ColumnMeta(30));
            columnBuilder.appendColumn(NPC_Columns.syncBy, ColumnBuilder.ColumnType.VARCHAR, new ColumnMeta(16));
            columnBuilder.appendColumn(NPC_Columns.syncDate, ColumnBuilder.ColumnType.BIGINT);
            tableBuilder.columns(columnBuilder);
            tableBuilder.createIfNotExists();
            tableBuilder.index(NPC_Columns.fileConfiguration);
            int i = statement.executeUpdate(tableBuilder.build());
            if (i > 0) {
                Utils.log("[DB] La tabla del modulo NPC ha sido creada");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Utils.log("No se pudo conectar a la base de datos [NPC]");
        }
    }

    public void removeConfiguration(String fileConfiguration, CallBack.ReturnCallBack<Integer> callback) {
        String SQL = "DELETE FROM " + this.database.getDbConfig().getTable("NPC") + " WHERE " + NPC_Columns.fileConfiguration + "=?";
        try (Connection con = this.database.getConnection(); PreparedStatement preparedStatement = con.prepareStatement(SQL)) {
            preparedStatement.setString(1, fileConfiguration);
            int rowsAffected = preparedStatement.executeUpdate();
            callback.onSuccess(rowsAffected);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            callback.onError(0);
        }
    }

    public void rebaseConfiguration(String player, CallBack.SingleCallBack call) {
        if (database == null) {
            return;
        }
        String resetIdAutoIncrement = "ALTER TABLE " + this.database.getDbConfig().getTable("NPC") + " AUTO_INCREMENT=1;";
        String table = this.database.getDbConfig().getTable("NPC");
        String delete = "DELETE FROM " + table + ";";
        StringBuilder insert = new StringBuilder("INSERT INTO " + table + "(" + NPC_Columns.fileConfiguration + ","
                                                         + NPC_Columns.configuration + ","
                                                         + NPC_Columns.directory + ","
                                                         + NPC_Columns.syncBy + ","
                                                         + NPC_Columns.syncDate
                                                         + ") VALUES ");
        long now = new Date().getTime();
        for (NPC npc : NPCManager.getAllNPCs()) {
            CustomConfiguration configuration = plugin.getConfigNPC(npc.getName());
            if (configuration != null) {
                insert.append("(?,?,?,?,?),");
            }
        }
        insert.deleteCharAt((insert.length() - 1)); //remover la ultima coma
        insert.append(";");
        String SQL = delete + resetIdAutoIncrement + insert.toString();
        try (Connection con = this.database.getConnection();
             PreparedStatement prestat = con.prepareStatement(SQL)) {
            int prep = 1;
            for (int i = 0; i < NPCManager.getAllNPCs().size(); i++) {
                NPC npc = NPCManager.getAllNPCs().get(i);
                CustomConfiguration configuration = plugin.getConfigNPC(npc.getName());
                if (configuration != null) {
                    try {
                        String dir = npc.getNPCMode().getDirectory();
                        byte[] bytesconfig = Base64.encodeBase64(FileUtils.readFileToByteArray(configuration.getFile()));
                        String base64 = new String(bytesconfig, StandardCharsets.UTF_8);
                        prestat.setString(prep++, configuration.getFile().getName().toLowerCase().replace(".yml", ""));
                        prestat.setString(prep++, base64);
                        prestat.setString(prep++, dir);
                        prestat.setString(prep++, player);
                        prestat.setLong(prep++, now);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            prestat.executeUpdate();
            call.onSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            call.onError();
        }
    }

    public void createConfiguration(String player, CustomConfiguration config, String dir, CallBack.SingleCallBack call) throws IOException {
        if (database == null) {
            return;
        }
        byte[] bytesconfig = Base64.encodeBase64(FileUtils.readFileToByteArray(config.getFile()));
        String base64 = new String(bytesconfig, StandardCharsets.UTF_8);
        long now = new Date().getTime();
        String SQL = "INSERT INTO " + this.database.getDbConfig().getTable("NPC") + "(" + NPC_Columns.fileConfiguration + ","
                + NPC_Columns.configuration + ","
                + NPC_Columns.directory + ","
                + NPC_Columns.syncBy + ","
                + NPC_Columns.syncDate
                + ") VALUES(?,?,?,?,?) ON DUPLICATE KEY UPDATE " + NPC_Columns.fileConfiguration + "=?, "
                + NPC_Columns.configuration + "=?, " + NPC_Columns.directory + "=?, "
                + NPC_Columns.syncBy + "=?, " + NPC_Columns.syncDate + "=?";
        try (Connection con = this.database.getConnection();
             PreparedStatement prestat = con.prepareStatement(SQL)) {
            prestat.setString(1, config.getFile().getName().replace(".yml", ""));
            prestat.setString(2, base64);
            prestat.setString(3, dir);
            prestat.setString(4, player);
            prestat.setLong(5, now);
            prestat.setString(6, config.getFile().getName().replace(".yml", ""));
            prestat.setString(7, base64);
            prestat.setString(8, dir);
            prestat.setString(9, player);
            prestat.setLong(10, now);
            prestat.executeUpdate();
            call.onSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            call.onError();
        }
    }

    public void fetchNPCs(CallBack.ReturnCallBack<ArrayList<NPCFetch>> callBack) {
        if (database == null) {
            Utils.log("Database nula");
            return;
        }
        String SQL = "SELECT * FROM " + database.getDbConfig().getTable("NPC");
        ArrayList<NPCFetch> fetching = new ArrayList<>();
        try (Connection con = this.database.getConnection();
             PreparedStatement prestat = con.prepareStatement(SQL)) {
            try (ResultSet set = prestat.executeQuery()) {
                while (set.next()) {
                    String name = set.getString(NPC_Columns.fileConfiguration);
                    String dir = set.getString(NPC_Columns.directory);
                    String base64 = set.getString(NPC_Columns.configuration);
                    NPCFetch fetch = new NPCFetch(name, dir);
                    fetch.setBase64(base64);
                    fetching.add(fetch);
                }
                set.close();
                callBack.onSuccess(fetching);
            }
        } catch (Exception e) {
            e.printStackTrace();
            callBack.onError(fetching);
        }
    }
}
