package com.releasingcode.goldenlobby.modulos.playerhider;

import com.releasingcode.goldenlobby.GoldenLobby;
import com.releasingcode.goldenlobby.modulos.inventarios.manager.Inventario;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;

/**
 * Encargarse de agregar el item
 * al jugador que le permitirá mostrar u ocultar jugadores.
 */

public class PlayerHiderListeners implements Listener {

    @EventHandler (priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        Hider hider = new Hider(player);
        Hider.setHiderItem(itemToAdd -> player.setMetadata("showingPlayers", new FixedMetadataValue(GoldenLobby.getInstance(), hider)), player, true);
        if (player.hasMetadata("Vanished")) {
            Inventario.clearItemsSelectorToPlayer(player);
        }
        Hider.hideIncomingPlayers(e.getPlayer());
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getAction() == Action.PHYSICAL || Hider.isNotHiderItem(e.getPlayer().getItemInHand())) return;
        Player player = e.getPlayer();

        if (player.hasMetadata("showingPlayers")) {
            Hider showingPlayers = ((Hider) player.getMetadata("showingPlayers").get(0).value());
            if (showingPlayers.isInCooldown()) return;
            showingPlayers.hidePlayers();
            player.removeMetadata("showingPlayers", GoldenLobby.getInstance());
        } else {
            Hider hider = new Hider(player);
            if (hider.isInCooldown()) return;
            hider.showPlayers(true);
            player.setMetadata("showingPlayers", new FixedMetadataValue(GoldenLobby.getInstance(), hider));
        }
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent e) {
        Hider.removeHider(e.getPlayer());
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (Hider.isNotHiderItem(e.getCurrentItem())) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (Hider.isNotHiderItem(e.getItemDrop().getItemStack())) return;
        e.setCancelled(true);
    }

}
