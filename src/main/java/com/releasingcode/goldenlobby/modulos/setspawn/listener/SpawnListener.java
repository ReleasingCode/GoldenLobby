package com.releasingcode.goldenlobby.modulos.setspawn.listener;

import com.releasingcode.goldenlobby.Utils;
import com.releasingcode.goldenlobby.managers.SpawnPoint;
import com.releasingcode.goldenlobby.modulos.setspawn.SpawnPointPlugin;
import es.minecub.core.minecubos.MinecubosAPI;
import es.minecub.core.ranks.RanksCore;
import es.minecub.core.sync.player.PlayerManager;
import es.minecub.serverdata.data.PlayerDatabase;
import org.bukkit.ChatColor;
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
        boolean hasKiller = e.getEntity().getKiller() != null;

        if (hasKiller) {
            Player victim = e.getEntity();
            Player killer = victim.getKiller();

            String victimName = (PlayerManager.getInstance().getDatabase(victim).contains("Nickname"))
                    ? PlayerManager.getInstance().getDatabase(victim).get("Nickname").asString()
                    : victim.getName();

            String killerName = (PlayerManager.getInstance().getDatabase(killer).contains("Nickname"))
                    ? PlayerManager.getInstance().getDatabase(killer).get("Nickname").asString()
                    : killer.getName();

            victim.sendMessage(Utils.chatColor("&7Has sido asesinado por &6" + killerName + "&7."));
            killer.sendMessage(Utils.chatColor("&7Has asesinado a &6" + victimName + "&7."));

            boolean isUser = RanksCore.getDefaultRank().equals(RanksCore.getPlayerRank(killer.getPlayer()));
            int minecubos = (isUser) ? 1 : 2;
            String plural = (isUser) ? "." : "s.";

            killer.sendMessage(ChatColor.GREEN + "Has ganado " + minecubos + " minecubo" + plural);
            killer.addPotionEffect(REGENERATION);
            MinecubosAPI.giveMinecubos(killer, minecubos, false);

        } else {
            e.getEntity().sendMessage(Utils.chatColor("&7Has muerto."));
        }

        e.setDeathMessage(null);
        e.getDrops().clear();
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
