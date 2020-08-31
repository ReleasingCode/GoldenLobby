package com.releasingcode.goldenlobby.modulos.repartidor.manager;

import com.releasingcode.goldenlobby.LobbyMC;
import com.releasingcode.goldenlobby.modulos.repartidor.RepartidorCorePlugin;
import com.releasingcode.goldenlobby.modulos.repartidor.items.CommandItem;
import com.releasingcode.goldenlobby.modulos.repartidor.npc.NPCRepartidor;
import es.minecub.core.holograms.data.Hologram;
import es.minecub.core.utils.LocationUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import com.releasingcode.goldenlobby.modulos.repartidor.items.Item;
import com.releasingcode.goldenlobby.modulos.repartidor.items.ItemType;
import com.releasingcode.goldenlobby.modulos.repartidor.items.VoteItem;
import com.releasingcode.goldenlobby.modulos.repartidor.playerdata.MinecubPlayer;
import com.releasingcode.goldenlobby.modulos.repartidor.utils.Utils;

import java.sql.*;
import java.util.*;

public class RepartidorManager {
    private static final long DAY_MILLIS = 86400000L;
    private static final Map<String, Item> items = new HashMap<>();
    public static boolean diamonds;
    public static Hologram hologram;
    private static Connection con;
    private static int slots;

    public static void loadRepartidorManager() {
        FileConfiguration config = RepartidorCorePlugin.getInstance().getConfig();
        if (config.contains("TDM")) {
            NPCRepartidor.spawnNPC(LocationUtils.convertStringToLoc(config.getString("TDM")));
        }


        diamonds = false;
        slots = 9 * config.getInt("LineasInv");
        Iterator<String> var3 = config.getConfigurationSection("Items").getKeys(false).iterator();

        while (true) {
            String str;
            ItemType type;
            String dName;
            String permission;
            int slot;
            int coins;
            int time;
            String line;
            do {
                while (true) {
                    do {
                        if (!var3.hasNext()) {
                            openConnection();
                            return;
                        }

                        str = var3.next();
                        type = ItemType.valueOf(config.getString("Items." + str + ".Tipo"));
                        dName = config.getString("Items." + str + ".NombrePublico");
                        permission = config.getString("Items." + str + ".Permiso");
                        slot = config.getInt("Items." + str + ".Slot");
                        coins = config.getInt("Items." + str + ".Coins");
                        time = config.getInt("Items." + str + ".TiempoDias");
                    } while (dName == null);

                    if (type != ItemType.COMMAND) {
                        break;
                    }

                    List<String> tempLore = config.getStringList("Items." + str + ".Descripcion");
                    if (tempLore != null) {
                        List<String> lore = new ArrayList<>();

                        for (String s : tempLore) {
                            line = s;
                            lore.add(ChatColor
                                    .translateAlternateColorCodes('&', line.replaceAll("%mes%", Utils.getMonth())));
                        }

                        items.put(str, new CommandItem(str, dName, permission, lore, slot, time, coins));
                    }
                }
            } while (type != ItemType.VOTEPAGE);

            String link = config.getString("Items." + str + ".Link");
            String serviceName = config.getString("Items." + str + ".Servicio");
            line = config.getString("Items." + str + ".NombreServicioPublico");
            if (link == null || serviceName == null || line == null) {
                return;
            }

            items.put(str, new VoteItem(str, dName, permission, slot, time, coins, link, serviceName, line));
        }
    }

    public static void openConnection() {
        try {
            if (con != null && !con.isClosed()) {
                return;
            }

            FileConfiguration config = RepartidorCorePlugin.getInstance().getConfig();
            con = DriverManager.getConnection("jdbc:mysql://" + config.getString("Db.Host") + ":3306/" + config
                            .getString("Db.Db") + "?autoReconnect=true", config.getString("Db.User"),
                    config.getString("Db.Pass"));
            StringBuilder builder = new StringBuilder();

            for (String item : items.keySet()) {
                builder.append(item).append(" VARCHAR(256), ");
            }

            PreparedStatement s = con.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS TDMManager (id INT NOT NULL AUTO_INCREMENT, User VARCHAR(256), " + builder
                            .toString() + "PRIMARY KEY (id), UNIQUE (User));");
            s.execute();
            s.close();
        } catch (SQLException var4) {
            var4.printStackTrace();
        }

        Bukkit.getScheduler().runTaskTimer(LobbyMC.getInstance(), RepartidorManager::neverGiveUp, 6000L, 6000L);
    }

    static void neverGiveUp() {
        try {
            if (con == null || con.isClosed()) {
                openConnection();
            }

            PreparedStatement s = con.prepareStatement("SELECT id FROM TDMManager LIMIT 1;");
            s.executeQuery();
            s.close();
        } catch (SQLException var1) {
            Bukkit.getServer().broadcastMessage(ChatColor.RED + ChatColor.BOLD
                    .toString() + "¡IMPORTANTE! ¡AVISA A UN ADMINISTRADOR DE ESTO! " + ChatColor.GREEN + ChatColor.BOLD + "Ha ocurrido un error en la conexión a la base de datos de minecubos - TDM" + ChatColor.RED + ChatColor.BOLD + " ¡AVISA A UN ADMINISTRADOR DE ESTO! ¡IMPORTANTE!");
            var1.printStackTrace();
        }

    }


    public static void loadItems(final MinecubPlayer mp) {
        Bukkit.getScheduler()
                .runTaskAsynchronously(LobbyMC.getInstance(), () -> {
                    try (PreparedStatement statement = RepartidorManager.con
                            .prepareStatement("SELECT * FROM TDMManager WHERE User=?;")
                    ) {
                        statement.setString(1, mp.getPlayer().getName());
                        try (ResultSet set = statement.executeQuery()) {
                            long now = System.currentTimeMillis();
                            while (set.next()) {
                                for (String item : RepartidorManager.getItems().keySet()) {
                                    String stringValue = set.getString(item);
                                    if (stringValue != null) {
                                        long value = Long.parseLong(stringValue);
                                        Item it = RepartidorManager.getItems().get(item);
                                        long time = now - value;
                                        long unit = 86400000L * (long) it.getTime();
                                        if (time < unit) {
                                            mp.getLongs().put(item, value);
                                        } else {
                                            RepartidorManager.removeLong(mp.getPlayer().getName(), item);
                                        }
                                    }
                                }
                            }
                            mp.setLoaded(true);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        mp.setError(true);
                    }
                });
    }

    public static void updateLong(final String player, final String key, final long value) {
        Bukkit.getServer().getScheduler()
                .runTaskAsynchronously(LobbyMC.getInstance(), () -> {
                    try (PreparedStatement statement = RepartidorManager.con.prepareStatement(
                            "INSERT INTO TDMManager (`User`, `" + key + "`) VALUES " + "('" + player + "', '" + value + "') ON DUPLICATE KEY " + "UPDATE " + key + "='" + value + "';")) {
                        statement.executeUpdate();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    public static void removeLong(final String player, final String key) {
        Bukkit.getServer().getScheduler()
                .runTaskAsynchronously(LobbyMC.getInstance(), () -> {
                    try (PreparedStatement s = RepartidorManager.con.prepareStatement(
                            "UPDATE TDMManager SET " + key + "= NULL WHERE User='" + player + "';")) {
                        s.executeUpdate();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    public static void toggleDiamonds() {
        diamonds = !diamonds;
    }

    public static void spawnDiamonds() {
        if (!diamonds) {
            //new DiamondRunnable();
        }
    }

    public static int getSlots() {
        return slots;
    }

    public static boolean isDiamondsTaskRunning() {
        return diamonds;
    }

    public static Map<String, Item> getItems() {
        return items;
    }


}
