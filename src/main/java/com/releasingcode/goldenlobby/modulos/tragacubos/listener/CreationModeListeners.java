package com.releasingcode.goldenlobby.modulos.tragacubos.listener;

import com.releasingcode.goldenlobby.modulos.tragacubos.TragacubosPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import com.releasingcode.goldenlobby.modulos.tragacubos.objects.tragacubos.MarcoTragaperra;
import com.releasingcode.goldenlobby.modulos.tragacubos.objects.tragacubos.Tragaperra;
import com.releasingcode.goldenlobby.modulos.tragacubos.objects.tragacubos.TragaperraCreationStages;

import static org.bukkit.event.EventPriority.HIGHEST;

public class CreationModeListeners implements Listener {

    private final TragacubosPlugin plugin;

    public CreationModeListeners(TragacubosPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = HIGHEST)
    public void onHangingItemFrame(HangingBreakByEntityEvent event) {
        if (event.getEntity() instanceof ItemFrame) {
            event.getEntity().getLocation().getBlock().getRelative(event.getEntity().getFacing().getOppositeFace());
            for (Tragaperra tragaperra : plugin.getTragaperrasManager().getTragaperras()) {
                for (MarcoTragaperra marco : tragaperra.getMarcos()) {
                    if (marco.isEqualsLocationItemFrame((ItemFrame) event.getEntity(), marco.getLocation())) {
                        event.setCancelled(true);
                        break;
                    }
                }
            }
        }
    }

    @EventHandler(priority = HIGHEST)
    public void onInteractFrame(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof ItemFrame) {
            for (Tragaperra tragaperra : plugin.getTragaperrasManager().getTragaperras()) {
                for (MarcoTragaperra marco : tragaperra.getMarcos()) {
                    if (marco.isEqualsLocationItemFrame((ItemFrame) event.getEntity(), marco.getLocation())) {
                        event.setCancelled(true);
                        break;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInteractItemFrame(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof ItemFrame) {
            ItemFrame frame = (ItemFrame) event.getRightClicked();
            for (Tragaperra tragaperra : plugin.getTragaperrasManager().getTragaperras()) {
                for (MarcoTragaperra marco : tragaperra.getMarcos()) {
                    if (marco.isEqualsLocationItemFrame(frame, marco.getLocation())) {
                        event.setCancelled(true);
                        break;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onTragaperraSet(PlayerInteractEvent e) {
        if (e.getAction() == Action.PHYSICAL
                || e.getAction() == Action.LEFT_CLICK_AIR
                || e.getAction() == Action.RIGHT_CLICK_AIR
                || !e.getPlayer().hasMetadata("creandoTragaperra")) return;

        Player player = e.getPlayer();
        Block clickedBlock = e.getClickedBlock();
        Location location = clickedBlock.getLocation();
        TragaperraCreationStages stage = (TragaperraCreationStages) player.getMetadata("creandoTragaperra").get(0).value();
        e.setCancelled(true);

        if (clickedBlock.getType() == Material.STONE_BUTTON
                && stage.isButtonSet()) {
            player.sendMessage(ChatColor.RED + "¡Ya estableciste el botón!");
            return;
        }

        if (stage.getActualStage() == TragaperraCreationStages.Stages.NONE
                && clickedBlock.getType() != Material.STONE_BUTTON) {
            player.sendMessage(ChatColor.RED + "¡Debes cliquear en un botón antes de establecer los marcos!");
            return;
        }

        if (stage.getMarcos().stream().anyMatch(marcoTragaperra -> marcoTragaperra.getLocation().equals(location))) {
            player.sendMessage(ChatColor.RED + "¡Ya hay un marco establecido en ese bloque!");
            return;
        }

        if (!clickedBlock.getRelative(e.getBlockFace()).isEmpty()
                && clickedBlock.getType() != Material.STONE_BUTTON
                && stage.getActualStage() != TragaperraCreationStages.Stages.CREATING_BUTTON) {
            player.sendMessage(ChatColor.RED + "¡No se puede establecer un marco aquí!");
            return;
        }

        stage.next(player, location, e.getBlockFace());

    }

}
