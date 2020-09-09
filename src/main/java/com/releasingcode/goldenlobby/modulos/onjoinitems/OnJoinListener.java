package com.releasingcode.goldenlobby.modulos.onjoinitems;

import com.releasingcode.goldenlobby.Utils;
import com.releasingcode.goldenlobby.listeners.custom.SecurePlayerJoinEvent;
import com.releasingcode.goldenlobby.managers.DelayPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class OnJoinListener implements Listener {
    private final OnJoinItemsPlugin plugin;

    public OnJoinListener(OnJoinItemsPlugin plugin) {
        this.plugin = plugin;
        plugin.getPlugin().getServer().getPluginManager().registerEvents(this, plugin.getPlugin());
    }

    @EventHandler
    public void onSecureJoin(SecurePlayerJoinEvent event) {
        Player player = event.getBukkitPlayer();
        ItemJoin joinItem = plugin.getItemJoin();
        if (joinItem != null) {
            if (joinItem.isClear_hotbar()) {
                plugin.getPlugin().getServer().getScheduler().runTaskLater(plugin.getPlugin(), () -> Utils.clearHotbarInventory(player),
                        joinItem.getClear_hotbar_after());
            }
            if (joinItem.getHeldItemSlotStartOnJoin() >= 0) {
                player.getInventory().setHeldItemSlot(joinItem.getHeldItemSlotStartOnJoin());
            }
            if (joinItem.isGiveItems()) {
                plugin.getPlugin().getServer().getScheduler().runTaskLater(plugin.getPlugin(),
                        () -> joinItem.givePlayer(player, true),
                        joinItem.getGiveItemsAfter());
            }
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void OnUseItem(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player.getItemInHand() != null && !player.getItemInHand().getType().equals(Material.AIR)) {
            ItemPlayer itemPlayer = plugin.getItemJoin().getItemJoin(player.getItemInHand());
            if (itemPlayer != null) {
                event.setCancelled(true);
                if (itemPlayer.validateClickUser(event.getAction())) {
                    String permission = itemPlayer.getPermission();
                    if (permission != null && !permission.trim().isEmpty()) {
                        if (!player.hasPermission(permission)) {
                            String message = itemPlayer.getNoPermissionMessage();
                            if (message != null) {
                                player.sendMessage(Utils.chatColor(message));
                            }
                            return;
                        }
                    }
                    if (itemPlayer.getUseCooldown() > 0) {
                        if (DelayPlayer.containsDelay(player, itemPlayer.getId())) {
                            return;
                        }
                        DelayPlayer.addDelay(player, itemPlayer.getId(), itemPlayer.getUseCooldown());
                    }
                    for (String comandos : itemPlayer.getExecutorsArray()) {
                        Utils.evaluateCommand(comandos, player);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        ItemJoin joinItem = plugin.getItemJoin();
        if (joinItem != null) {
            if (joinItem.getHeldItemSlotStartOnRespawn() >= 0) {
                player.getInventory().setHeldItemSlot(joinItem.getHeldItemSlotStartOnRespawn());
            }
            if (joinItem.isGiveItems()) {
                plugin.getPlugin().getServer().getScheduler().runTaskLater(plugin.getPlugin(),
                        () -> joinItem.givePlayer(player, false),
                        15);
            }
        }
    }

    @EventHandler
    public void OnDeathPlayer(PlayerDeathEvent event) {
        if (event.getDrops() != null) {
            event.getDrops().removeIf(stack -> {
                ItemPlayer item = plugin.getItemJoin().getItemJoin(stack);
                return item != null && !item.isDropOnPlayerDeath();
            });
        }
    }
    /*
     * TODO
     * JoinListener
     * - Agregar booleano que permita dropear o no dropear el item
     * - Agregar booleano que permita desaparecer el item al dropearlo
     * - Agregar booleano de No movable Item
     * - Agregar booleano de Remove Item After Death
     * - Agregar String de Item Por Mundo
     */

}
