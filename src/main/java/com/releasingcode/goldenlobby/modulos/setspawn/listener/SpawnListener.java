package com.releasingcode.goldenlobby.modulos.setspawn.listener;

import com.releasingcode.goldenlobby.managers.SpawnPoint;
import com.releasingcode.goldenlobby.modulos.setspawn.SpawnPointPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SpawnListener implements Listener {
    private static final PotionEffect REGENERATION = new PotionEffect(PotionEffectType.REGENERATION, 20 * 2, 2);

    @EventHandler(priority = EventPriority.NORMAL)
    public void onJoin(PlayerJoinEvent event) {
        SpawnPoint spawnPoint = SpawnPointPlugin.SPAWNPOINT;
        if (spawnPoint != null) {
            event.getPlayer().teleport(spawnPoint.getLocation());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent e) {
        e.setDeathMessage(null);
        // e.getDrops().clear();
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onRespawn(PlayerRespawnEvent event) {
        SpawnPoint spawnPoint = SpawnPointPlugin.SPAWNPOINT;
        if (spawnPoint != null) {
            event.setRespawnLocation(spawnPoint.getLocation());
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            Player player = (Player) e.getEntity();
            if (e.getCause() == EntityDamageEvent.DamageCause.VOID) {
                SpawnPoint point = SpawnPointPlugin.SPAWNPOINT;
                if (point != null) {
                    e.setCancelled(true);
                    player.setFallDistance(0);
                    player.teleport(point.getLocation());
                }
            }
        }
    }
}
