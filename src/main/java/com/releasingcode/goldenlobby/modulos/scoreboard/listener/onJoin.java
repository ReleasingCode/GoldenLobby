package com.releasingcode.goldenlobby.modulos.scoreboard.listener;

import com.releasingcode.goldenlobby.listeners.custom.SecurePlayerJoinEvent;
import com.releasingcode.goldenlobby.modulos.scoreboard.ScoreboardPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class onJoin implements Listener {

    private final ScoreboardPlugin plugin;

    public onJoin(ScoreboardPlugin plugin) {
        this.plugin = plugin;
        plugin.getPlugin().getServer().getPluginManager().registerEvents(this, plugin.getPlugin());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onQuit(PlayerQuitEvent event) {
        plugin.getScoreboardManager().removeScoreboard(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onJoinScoreboard(SecurePlayerJoinEvent event) {
        plugin.getPlugin().getServer().getScheduler().runTaskLaterAsynchronously(plugin.getPlugin(), () -> {
            event.getPlayer().getScoreboard().reset();
            Player player = event.getBukkitPlayer();
            if (player != null) {
                plugin.getScoreboardManager().setScoreboardForPlayer(player);
            }
        }, 1);
    }
}
