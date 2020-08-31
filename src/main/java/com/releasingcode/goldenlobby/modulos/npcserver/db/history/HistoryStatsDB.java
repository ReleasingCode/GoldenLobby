package com.releasingcode.goldenlobby.modulos.npcserver.db.history;

import com.releasingcode.goldenlobby.LobbyMC;
import com.releasingcode.goldenlobby.Utils;
import com.releasingcode.goldenlobby.call.CallBack;
import com.releasingcode.goldenlobby.database.Database;
import com.releasingcode.goldenlobby.database.IDatabase;
import com.releasingcode.goldenlobby.database.builders.ColumnBuilder;
import com.releasingcode.goldenlobby.database.builders.ColumnMeta;
import com.releasingcode.goldenlobby.database.builders.TableBuilder;
import com.releasingcode.goldenlobby.database.pubsub.NetworkStream;
import com.releasingcode.goldenlobby.managers.LobbyPlayer;
import com.releasingcode.goldenlobby.managers.history.LobbyPlayerHistory;
import com.releasingcode.goldenlobby.modulos.npcserver.NPCServerPlugin;
import org.bukkit.Bukkit;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class HistoryStatsDB implements IDatabase {
    private final NPCServerPlugin plugin;
    private final LobbyMC lobbyMC;
    private Database database;

    public HistoryStatsDB(NPCServerPlugin plugin) {
        this.plugin = plugin;
        lobbyMC = LobbyMC.getInstance();
        Database.registerCall(this);
    }

    @Override
    public void onLoaded(Database database) {
        this.database = database;
    }

    @Override
    public void onCreateTable(Database database) {
        if (database == null) {
            return;
        }
        try (Connection con = database.getConnection();
             Statement statement = con.createStatement()) {
            TableBuilder tableBuilder = new TableBuilder(
                    database.getDbConfig().getTable("NPC.PlayerStats"));
            ColumnBuilder columnBuilder = new ColumnBuilder("id", ColumnBuilder.ColumnType.INT,
                    new ColumnMeta().primaryKey().autoIncrement());
            //cambiar estructura de tabla al usarse en minecub
            columnBuilder.appendColumn(PlayerHistoryStatsColumns.uid,
                    ColumnBuilder.ColumnType.BIGINT, new ColumnMeta().unique());
            columnBuilder.appendColumn(PlayerHistoryStatsColumns.username, ColumnBuilder.ColumnType.VARCHAR,
                    new ColumnMeta(17));
            columnBuilder.appendColumn(PlayerHistoryStatsColumns.previus_target, ColumnBuilder.ColumnType.VARCHAR,
                    new ColumnMeta(19));
            columnBuilder.appendColumn(PlayerHistoryStatsColumns.current_target, ColumnBuilder.ColumnType.VARCHAR,
                    new ColumnMeta(19));
            columnBuilder.appendColumn(PlayerHistoryStatsColumns.history, ColumnBuilder.ColumnType.MEDIUMTEXT);
            columnBuilder.appendColumn(PlayerHistoryStatsColumns.history_playing, ColumnBuilder.ColumnType.BIT,
                    new ColumnMeta().defaultValue(0));
            columnBuilder
                    .appendColumn(PlayerHistoryStatsColumns.history_current_playing, ColumnBuilder.ColumnType.VARCHAR,
                            new ColumnMeta(19));
            columnBuilder.appendColumn(PlayerHistoryStatsColumns.history_dash, ColumnBuilder.ColumnType.TINYINT,
                    new ColumnMeta().defaultValue(-1));
            columnBuilder.appendColumn(PlayerHistoryStatsColumns.staff_founds, ColumnBuilder.ColumnType.MEDIUMTEXT);
            tableBuilder.columns(columnBuilder);
            tableBuilder.createIfNotExists();
            tableBuilder.index(PlayerHistoryStatsColumns.uid);
            int i = statement.executeUpdate(tableBuilder.build());
            if (i > 0) {
                Utils.log("[DB] La tabla del modulo NPC -> PlayerStats ha sido creada");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Utils.log("No se pudo conectar a la base de datos [NPC - HistoryStats]");
        }
    }

    public void savePlayer(LobbyPlayer lp, CallBack.SingleCallBack call, boolean removeKey) {
        if (database == null) {
            call.onError();
            return;
        }
        LobbyPlayerHistory historia = lp.getHistory(); // administrador de historia por jugador y stats del jugador
        Bukkit.getScheduler().runTaskAsynchronously(LobbyMC.getInstance(), () -> {
            String SQL = "INSERT INTO " + this.database.getDbConfig().getTable("NPC.PlayerStats")
                    + "(" + PlayerHistoryStatsColumns.uid + ","
                    + PlayerHistoryStatsColumns.username + ","
                    + PlayerHistoryStatsColumns.previus_target + ","
                    + PlayerHistoryStatsColumns.current_target + ","
                    + PlayerHistoryStatsColumns.history + ", "
                    + PlayerHistoryStatsColumns.history_playing + ", "
                    + PlayerHistoryStatsColumns.history_current_playing + ", "
                    + PlayerHistoryStatsColumns.history_dash + ", "
                    + PlayerHistoryStatsColumns.staff_founds
                    + ") VALUES(?,?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE " +
                    PlayerHistoryStatsColumns.username + "=?, "
                    + PlayerHistoryStatsColumns.previus_target + "=?, "
                    + PlayerHistoryStatsColumns.current_target + "=?, "
                    + PlayerHistoryStatsColumns.history + "=?, "
                    + PlayerHistoryStatsColumns.history_playing + "=?, "
                    + PlayerHistoryStatsColumns.history_current_playing + "=?, "
                    + PlayerHistoryStatsColumns.history_dash + "=?, "
                    + PlayerHistoryStatsColumns.staff_founds + "=?";
            int uid = lp.getId();
            String name = lp.getName();
            String previus_target = historia.getPreviusTarget() == null ? "" : historia.getPreviusTarget();
            String current_target = historia.getActualTarget() == null ? "" : historia.getActualTarget();
            String thenHistory = historia.registredString();
            String staff_found = lp.getLobbyStaffFound().allNPCs();
            String currentPlaying = historia.getCurrentPlaying() == null ? "" : historia.getCurrentPlaying();
            boolean playing = historia.isPlaying();
            int dashPlayed = historia.getPlayedMessage();
            /*
             * Aquí es establece los datos a redis y se manda un mensaje para los clientes en un pipeline
             * los clientes recibirán el onMessage y actualizarán su cache local por persona
             *
             */
            if (removeKey) {
                delKeyRedis(lp);
            } else {
                setPlayerRedis(lp);
            }
            try (Connection con = this.database.getConnection();
                 PreparedStatement prestat = con.prepareStatement(SQL)) {
                prestat.setInt(1, uid);
                prestat.setString(2, name);
                prestat.setString(3, previus_target);
                prestat.setString(4, current_target);
                prestat.setString(5, thenHistory);
                prestat.setBoolean(6, playing);
                prestat.setString(7, currentPlaying);
                prestat.setInt(8, dashPlayed);
                prestat.setString(9, staff_found);
                prestat.setString(10, name);
                prestat.setString(11, previus_target);
                prestat.setString(12, current_target);
                prestat.setString(13, thenHistory);
                prestat.setBoolean(14, playing);
                prestat.setString(15, currentPlaying);
                prestat.setInt(16, dashPlayed);
                prestat.setString(17, staff_found);
                prestat.executeUpdate();
                call.onSuccess();
            } catch (Exception e) {
                e.printStackTrace();
                call.onError();
            }
        });
    }

    public void setPlayerRedis(LobbyPlayer lobbyPlayer) {
        Bukkit.getScheduler().runTaskAsynchronously(lobbyMC, () -> {
            try (Jedis jedis = lobbyMC.getRedisManager().getJedisEditor()) {
                //byte[] channel = SubChannel.NPCSTAFF_PLAYERSLOBBY.lower().getBytes();
                NPCHistoryPlayerNetwork playernet = new NPCHistoryPlayerNetwork();
                LobbyPlayerHistory history = lobbyPlayer.getHistory();
                String staff_found = lobbyPlayer.getLobbyStaffFound() != null ? lobbyPlayer.getLobbyStaffFound()
                        .allNPCs() : "";
                playernet.uid = lobbyPlayer.getId();
                playernet.name = lobbyPlayer.getName();
                playernet.previus_target = history.getPreviusTarget() == null ? "" : history.getPreviusTarget();
                playernet.current_target = history.getActualTarget() == null ? "" : history.getActualTarget();
                playernet.history = history.registredString();
                playernet.history_is_playing = history.isPlaying();
                playernet.history_current_playing = history.getCurrentPlaying() == null ? "" : history
                        .getCurrentPlaying();
                playernet.history_dash = history.getPlayedMessage();
                playernet.staff_found = staff_found;
                //* enviarlo en un solo proceso de conexión
                Pipeline pd = jedis.pipelined();
                ///pd.publish(channel, NetworkStream.toByte(playernet));
                String key = "lobbymc/historynpcs/newstats/player/" + lobbyPlayer.toStringUUID();
                try {
                    pd.set(key.getBytes(), NetworkStream.toByte(playernet));
                    pd.expire(key.getBytes(), 172800);
                    pd.sync();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void resetPlayer(LobbyPlayer lobbyPlayer, CallBack.SingleCallBack call) {
        lobbyPlayer.setHistory(new LobbyPlayerHistory("", null, null));
        savePlayer(lobbyPlayer, call, true);
    }

    public void delKeyRedis(LobbyPlayer lobbyPlayer) {
        try (Jedis jedis = lobbyMC.getRedisManager().getJedisEditor()) {
            Pipeline pd = jedis.pipelined();
            String key = "lobbymc/historynpcs/newstats/player/" + lobbyPlayer.toStringUUID();
            pd.del(key);
            pd.sync();
        }
    }

    public void loadPlayer(String uid, CallBack.ReturnCallBack<NPCHistoryPlayerNetwork> call) {
        if (database == null) {
            return;
        }
        String SQL = "SELECT * FROM " + database.getDbConfig().getTable("NPC.PlayerStats")
                + " WHERE " + PlayerHistoryStatsColumns.uid + "=?";
        Bukkit.getScheduler().runTaskAsynchronously(LobbyMC.getInstance(), () -> {
            try (Jedis jedis = lobbyMC.getRedisManager().getJedisEditor()) {
                //byte[] channel = SubChannel.NPCSTAFF_PLAYERSLOBBY.lower().getBytes();
                String key = "lobbymc/historynpcs/newstats/player/" + uid;
                Pipeline pipeline = jedis.pipelined();
                Response<Boolean> exist = pipeline.exists(key);
                Response<byte[]> data = pipeline.get(key.getBytes());
                pipeline.sync();
                boolean has = exist.get();
                if (has) {
                    byte[] stream = data.get();
                    try {
                        Object obj = NetworkStream.fromByte(stream);
                        if (obj instanceof NPCHistoryPlayerNetwork) {
                            NPCHistoryPlayerNetwork npcnet = (NPCHistoryPlayerNetwork) obj;
                            call.onSuccess(npcnet);
                            //Utils.log("Obtenido de Redis [historia/stafffound]: " + npcnet.history);
                            return;
                        }
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                        pipeline.del(key);
                        pipeline.sync();
                    }
                }
                try (Connection con = this.database.getConnection();
                     PreparedStatement prestat = con.prepareStatement(SQL)) {
                    prestat.setString(1, uid);
                    try (ResultSet set = prestat.executeQuery()) {
                        int uidSet = -1;
                        String nameSet = null;
                        String previus_target = "";
                        String current_target = "";
                        String history_current_playing = "";
                        String history = "";
                        boolean history_is_playing = false;
                        int history_dash = -1;
                        String staff_found = "";
                        if (set.next()) { // existe el jugador
                            uidSet = set.getInt(PlayerHistoryStatsColumns.uid);
                            nameSet = set.getString(PlayerHistoryStatsColumns.username);
                            previus_target = set.getString(PlayerHistoryStatsColumns.previus_target);
                            current_target = set.getString(PlayerHistoryStatsColumns.current_target);
                            history = set.getString(PlayerHistoryStatsColumns.history);
                            history_is_playing = set.getBoolean(PlayerHistoryStatsColumns.history_playing);
                            history_current_playing = set.getString(PlayerHistoryStatsColumns.history_current_playing);
                            history_dash = set.getInt(PlayerHistoryStatsColumns.history_dash);
                            staff_found = set.getString(PlayerHistoryStatsColumns.staff_founds);
                        }
                        NPCHistoryPlayerNetwork net = new NPCHistoryPlayerNetwork();
                        net.uid = uidSet;
                        net.name = nameSet;
                        net.previus_target = previus_target;
                        net.current_target = current_target;
                        net.history = history;
                        net.history_is_playing = history_is_playing;
                        net.history_current_playing = history_current_playing;
                        net.history_dash = history_dash;
                        net.staff_found = staff_found;
                        call.onSuccess(net);
                        //Utils.log("Sacado de mysql [history]: " + history);
                    }
                } catch (Exception e) {
                    call.onError(null);
                }
            }

        });
    }
}
