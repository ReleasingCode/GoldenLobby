package com.releasingcode.goldenlobby.modulos.limbo;

import com.releasingcode.goldenlobby.LobbyMC;
import com.releasingcode.goldenlobby.Utils;
import es.minecub.core.utils.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class AfkDetectorsEvent extends BukkitRunnable implements Listener {
    private final Map<String, Long> afkPlayers;
    private final ArrayList<String> playersSending;
    private boolean hasStopping;

    public AfkDetectorsEvent(LimboPlugin main) {
        this.afkPlayers = main.getAfkPlayers();
        playersSending = new ArrayList<>();
        hasStopping = false;
        this.runTaskTimerAsynchronously(LobbyMC.getInstance(), 0, 20);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent e) {
        afkPlayers.remove(e.getPlayer().getName());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(AsyncPlayerChatEvent e) {
        afkPlayers.remove(e.getPlayer().getName());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(AsyncPlayerPreLoginEvent event) {
        if (hasStopping) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "El servidor se está reiniciando");
        }
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onServer(ServerCommandEvent event) {
        if (event.getCommand().contains("stop")) {
            Utils.log("ServerCommandListener", "Se ha ejecutado el comando " + event.getCommand());
            event.setCancelled(true);
            Collection<? extends Player> players = Bukkit.getOnlinePlayers();
            hasStopping = true;
            for (Player p : players) {
                if (playersSending.contains(p.getName())) {
                    continue;
                }
                Bukkit.getScheduler().runTask(LobbyMC.getInstance(), () -> {
                    p.sendMessage("${lobby.afk.limbo.serveroff}");
                    PlayerUtils.sendToServer(p, "limbo");
                    playersSending.add(p.getName());
                });
            }
            Bukkit.getScheduler().runTaskTimer(LobbyMC.getInstance(), () -> {
                if (Bukkit.getOnlinePlayers().size() < 1) {
                    this.cancel();
                    Bukkit.shutdown();
                }
            }, 20, 1);

        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMove(PlayerMoveEvent e) {
        if (e.getPlayer().hasPermission("lobbymc.antiafk.admin")) {
            return;
        }
        afkPlayers.remove(e.getPlayer().getName());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent e) {
        if (e.getPlayer().hasPermission("lobbymc.antiafk.admin")) {
            return;
        }
        afkPlayers.put(e.getPlayer().getName(), System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent e) {
        afkPlayers.remove(e.getPlayer().getName());
    }

    @Override
    public void run() {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.hasPermission("lobbymc.antiafk.admin")) {
                continue;
            }
            afkPlayers.putIfAbsent(onlinePlayer.getName(), System.currentTimeMillis());
        }
        afkPlayers.forEach((name, timeAgo) -> {
            long difference = System.currentTimeMillis() - timeAgo;
            if (TimeUnit.MILLISECONDS.toSeconds(difference) >= LimboPlugin.SECONDS_TO_LIMBO) {
                if (Bukkit.getPlayer(name) == null) {
                    afkPlayers.remove(name);
                    return;
                }
                Bukkit.getPlayer(name).sendMessage("${lobby.afk.limbo}");
                PlayerUtils.sendToServer(Bukkit.getPlayer(name), "limbo");
            }
        });
    }

}
