package com.releasingcode.goldenlobby.modulos.onjoinitems;

import com.releasingcode.goldenlobby.Utils;
import com.releasingcode.goldenlobby.listeners.custom.SecurePlayerJoinEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

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
            if (joinItem.isGiveItems()) {
                plugin.getPlugin().getServer().getScheduler().runTaskLater(plugin.getPlugin(), () -> joinItem.givePlayer(player),
                        joinItem.getGiveItemsAfter());
            }
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
