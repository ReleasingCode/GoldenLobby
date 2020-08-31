package com.releasingcode.goldenlobby.modulos.repartidor;

import com.releasingcode.goldenlobby.configuracion.CustomConfiguration;
import com.releasingcode.goldenlobby.loader.LobbyComponente;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import com.releasingcode.goldenlobby.modulos.repartidor.commands.RepartidorCommand;
import com.releasingcode.goldenlobby.modulos.repartidor.listener.MainListener;
import com.releasingcode.goldenlobby.modulos.repartidor.manager.RepartidorManager;
import com.releasingcode.goldenlobby.modulos.repartidor.playerdata.MinecubPlayer;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class RepartidorCorePlugin extends LobbyComponente {
    private static final ConcurrentHashMap<String, MinecubPlayer> players = new ConcurrentHashMap<>();
    private static RepartidorCorePlugin plugin;
    private CustomConfiguration configuration;

    public static ConcurrentHashMap<String, MinecubPlayer> getPlayers() {
        return players;
    }

    public static Collection<MinecubPlayer> getPlayersCollections() {
        return players.values();
    }

    public static MinecubPlayer getPlayer(Player player) {
        return players.get(player.getName());
    }

    public static void addPlayer(Player player) {
        players.put(player.getName(), new MinecubPlayer(player.getName()));
    }

    public static void removePlayer(Player player) {
        players.remove(player.getName());
    }

    public static RepartidorCorePlugin getInstance() {
        return plugin;
    }

    @Override
    public void onEnable() {
        plugin = this;
        configuration = new CustomConfiguration("repartidor", getPlugin());
        new MainListener();
        RepartidorManager.loadRepartidorManager();
        new RepartidorCommand("repartidor").register();
    }

    public void onDisable() {
    }

    public FileConfiguration getConfig() {
        return configuration.getConfig();
    }

    public void saveConfig() {
        configuration.save();
    }
}
