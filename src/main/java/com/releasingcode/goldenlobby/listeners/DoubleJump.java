package com.releasingcode.goldenlobby.listeners;

import com.releasingcode.goldenlobby.managers.LobbyPlayer;
import com.releasingcode.goldenlobby.managers.LobbyPlayerMap;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;

import java.util.HashSet;
import java.util.Set;

public class DoubleJump implements Listener {
    private final Set<String> playersInDoubleJump = new HashSet<>();

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        playersInDoubleJump.remove(e.getPlayer().getName());
    }

    @EventHandler
    public void setJump(PlayerToggleFlightEvent e) {
        Player p = e.getPlayer();
        if (p.getGameMode() == GameMode.CREATIVE
                || p.getGameMode() == GameMode.SPECTATOR
                || p.isFlying()
                || playersInDoubleJump.contains(p.getName())
                || p.hasPermission("goldenlobby.benefits.fly")
                && p.hasMetadata("userFlying")) {
            return;
        }
        e.setCancelled(true);
        p.setAllowFlight(false);
        p.setFlying(false);
        LobbyPlayer lp = LobbyPlayerMap.getJugador(p);
        if (lp != null) {
            lp.setFlyer(false);
        }
        p.setFallDistance(100);
        playersInDoubleJump.add(p.getName());
        p.setVelocity(e.getPlayer().getLocation().getDirection().multiply(1.75).setY(1.5));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFallDamage(EntityDamageEvent e) {
        if (e.getCause() != EntityDamageEvent.DamageCause.FALL || !(e.getEntity() instanceof Player)) return;
        Player player = (Player) e.getEntity();
        if (playersInDoubleJump.remove(e.getEntity().getName())) {
            e.setCancelled(true);
            player.setAllowFlight(true);
            LobbyPlayer lp = LobbyPlayerMap.getJugador(player);
            if (lp != null) {
                lp.setFlyer(true);
            }
        }
    }

}
