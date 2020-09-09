package com.releasingcode.goldenlobby.modulos.inventarios.listener;

import com.releasingcode.goldenlobby.managers.DelayPlayer;
import com.releasingcode.goldenlobby.modulos.inventarios.builder.ItemMenuHolder;
import com.releasingcode.goldenlobby.modulos.inventarios.manager.Inventario;
import com.releasingcode.goldenlobby.modulos.inventarios.manager.ItemSelector;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class ItemInventory implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Inventario.clearItemsSelectorToPlayer(event.getPlayer());
        Inventario.setSelectorToPlayer(event.getPlayer());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Inventario.clearItemsSelectorToPlayer(player);
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onItemClick(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        if (p.getItemInHand() != null && !p.getItemInHand().getType().equals(Material.AIR)) {
            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK
                    || event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
                ItemStack hand = p.getItemInHand();
                /*
                Comprobar si el objeto en mano es igual a alugno de los selectores
                 */
                ItemSelector selector = Inventario.getItemByItemStack(hand);
                if (selector != null) {
                    if (selector.getInventario() != null) {
                        event.setCancelled(true);
                        event.setUseItemInHand(Event.Result.DENY);
                        event.setUseInteractedBlock(Event.Result.DENY);
                        if (p.getOpenInventory() != null && p.getOpenInventory().getTopInventory() != null
                                && p.getOpenInventory().getTopInventory().getHolder() instanceof ItemMenuHolder) {
                            return;
                        }
                        if (DelayPlayer.containsDelay(p, "item_openinventory")) {
                            return;
                        }
                        DelayPlayer.addDelay(p, "item_openinventory", 1000);
                        p.closeInventory();
                        selector.getInventario().openInventory(p);
                        p.updateInventory();
                    }
                }
            }
        }
    }
}
