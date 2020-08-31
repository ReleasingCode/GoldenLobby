package com.releasingcode.goldenlobby.listeners;

import com.google.common.collect.ImmutableSet;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Set;

public class BasicCancelledEvents implements Listener {
    private static final Set<Material> UNALLOWED_MATERIALS = ImmutableSet.of(Material.ENCHANTMENT_TABLE,
                                                                             Material.WORKBENCH,
                                                                             Material.FURNACE,
                                                                             Material.BURNING_FURNACE,
                                                                             Material.ANVIL,
                                                                             Material.CHEST,
                                                                             Material.NOTE_BLOCK,
                                                                             Material.FIRE);

    public BasicCancelledEvents() {
        setDaylightCicleOff();
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent e) {
        cancelIfNotAllowed(e.getPlayer(), e, "lobbymc.modification.blockbreak");
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent e) {
        cancelIfNotAllowed(e.getPlayer(), e, "lobbymc.modification.blockplace");
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onArmorManip(PlayerArmorStandManipulateEvent e) {
        if (!e.getPlayer().hasPermission("lobbymc.modification.armormanip")) e.setCancelled(true);
    }

    @EventHandler
    public void onBlockTeleport(BlockFromToEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onEntityHit(EntityDamageByEntityEvent e) {
        switch (e.getEntity().getType()) {
            case ARMOR_STAND:
                cancelIfNotAllowed(e.getDamager(), e, "lobbymc.modification.armormanip");
                break;
            case ITEM_FRAME:
                cancelIfNotAllowed(e.getDamager(), e, "lobbymc.modification.itemframebreak");
                break;
            case MINECART:
                cancelIfNotAllowed(e.getDamager(), e, "lobbymc.modification.minecartbreak");
                break;
        }
    }

    @EventHandler
    public void hangingBreakEvent(HangingBreakByEntityEvent e) {
        cancelIfNotAllowed(e.getRemover(), e, "lobbymc.modification.itemframebreak");
    }

    @EventHandler
    public void onItemInteract(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null) return;
        if (e.getClickedBlock().getType() == Material.SOIL) {
            cancelIfNotAllowed(e.getPlayer(), e, "lobbymc.modification.cropbreak");
            return;
        }

        if (UNALLOWED_MATERIALS.contains(e.getClickedBlock().getType())) {
            cancelIfNotAllowed(e.getPlayer(), e, "lobbymc.modification.interact");
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent e) {
        if (e.getRightClicked().getType() == EntityType.ITEM_FRAME) {
            cancelIfNotAllowed(e.getPlayer(), e, "lobbymc.modification.itemframeflip");
        }
    }

    private void cancelIfNotAllowed(Entity player, Cancellable e, String permission) {
        if (!player.hasPermission(permission)) e.setCancelled(true);
    }

    private void setDaylightCicleOff() {
        Bukkit.getWorlds().forEach(world -> world.setGameRuleValue("doDaylightCycle", "false"));
    }

}
