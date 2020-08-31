package com.releasingcode.goldenlobby.extendido.scoreboard;


import com.releasingcode.goldenlobby.extendido.nms.ObjectiveSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;


class OnlinePlayersListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent ev) {
        //AGREGAR LOGGED PLAYER UNO POR UNO
        //reinica datos antiguos del sidebar cache
        ObjectiveSender.handleLogin(ev.getPlayer().getUniqueId());
    }
}
