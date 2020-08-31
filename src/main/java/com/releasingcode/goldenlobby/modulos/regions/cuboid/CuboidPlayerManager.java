package com.releasingcode.goldenlobby.modulos.regions.cuboid;

import com.releasingcode.goldenlobby.LobbyMC;
import com.releasingcode.goldenlobby.managers.LobbyPlayer;
import com.releasingcode.goldenlobby.managers.LobbyPlayerMap;
import com.releasingcode.goldenlobby.modulos.regions.PlayerEnterRegionEvent;
import com.releasingcode.goldenlobby.modulos.setspawn.SpawnPointPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;

public class CuboidPlayerManager extends BukkitRunnable implements Listener {
    private final EnumMap<StageOfCreation.Regions, Set<String>> players = new EnumMap<>(StageOfCreation.Regions.class);
    private EnumMap<StageOfCreation.Regions, Cuboid> regions = new EnumMap<>(StageOfCreation.Regions.class);

    public CuboidPlayerManager() {
        runTaskTimerAsynchronously(LobbyMC.getInstance(), 0, 5);
        LobbyMC.getInstance().setCuboidManager(this);
    }

    @Override
    public void run() {
        if (!regions.containsKey(StageOfCreation.Regions.LOBBY)) return;
        Bukkit.getOnlinePlayers().forEach(player -> {
            StageOfCreation.Regions isIn = getActualRegion(player);
            if (isIn == null) return;
            regions.forEach((region, cuboid) -> {
                if (isIn != region) return;
                if (region == StageOfCreation.Regions.PVP) {
                    removePlayerOfRegion(StageOfCreation.Regions.LOBBY, player);
                    addPlayerToRegion(StageOfCreation.Regions.PVP, player);
                } else {
                    if (isPlayerInsidePvpRegion(player)) return;
                    removePlayerOfRegion(StageOfCreation.Regions.PVP, player);
                    addPlayerToRegion(StageOfCreation.Regions.LOBBY, player);
                }
            });
        });
    }

    private void handlePlayerOutsideRegions(Player player) {
        if (SpawnPointPlugin.SPAWNPOINT != null) player.teleport(SpawnPointPlugin.SPAWNPOINT.getLocation());
        player.sendMessage(ChatColor.RED + "¡No salgas del lobby!");
    }

    /**
     * Obtener la región en la que el jugador está.
     *
     * @param player juador
     * @return región si es que está en una. Retornará null si no está dentro de ninguna. En ese caso,
     * se tepea devuelta al spawn.
     */

    private StageOfCreation.Regions getActualRegion(Player player) {
        if (isPlayerInsidePvpRegion(player)) {
            return StageOfCreation.Regions.PVP;
        } else if (isPlayerInLobbyRegion(player)) {
            return StageOfCreation.Regions.LOBBY;
        } else {
            handlePlayerOutsideRegions(player);
            return null;
        }
    }

    private boolean isPlayerInLobbyRegion(Player player) {
        if (!regions.containsKey(StageOfCreation.Regions.LOBBY)) return false;
        return regions.get(StageOfCreation.Regions.LOBBY).isIn(player);
    }

    private boolean isPlayerInsidePvpRegion(Player player) {
        if (!regions.containsKey(StageOfCreation.Regions.PVP)) return false;
        return regions.get(StageOfCreation.Regions.PVP).isIn(player);
    }

    private void removePlayerOfRegion(StageOfCreation.Regions regionToBeRemovedFrom, Player player) {
        players.computeIfPresent(regionToBeRemovedFrom, (k, v) -> {
            v.remove(player.getName());
            return v;
        });
    }

    private void addPlayerToRegion(StageOfCreation.Regions regionToBeAddedTo, Player player) {
        players.computeIfPresent(regionToBeAddedTo, (k, v) -> {
            LobbyPlayer jugador = LobbyPlayerMap.getJugador(player);
            if (jugador == null || jugador.getActualRegion() == regionToBeAddedTo) return v;
            Bukkit.getPluginManager().callEvent(new PlayerEnterRegionEvent(player, regionToBeAddedTo, jugador.getActualRegion()));
            jugador.setActualRegion(regionToBeAddedTo);
            v.add(player.getName());
            return v;
        });
    }

    public void createCuboid(StageOfCreation.Regions region, Cuboid cuboid) {
        regions.put(region, cuboid);
        players.put(region, new HashSet<>());
    }

    public void removeRegion(StageOfCreation.Regions region) {
        regions.remove(region);
        players.remove(region);
    }

    public EnumMap<StageOfCreation.Regions, Cuboid> getRegions() {
        return regions;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        // Agregar el jugador a la region del lobby al entrar, si es que está ahí dentro.
        players.computeIfPresent(StageOfCreation.Regions.LOBBY, (k, v) -> {
            if (regions.get(k).isIn(e.getPlayer())) v.add(e.getPlayer().getName());
            return v;
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        players.forEach((k, v) -> v.remove(e.getPlayer().getName()));
    }


}
