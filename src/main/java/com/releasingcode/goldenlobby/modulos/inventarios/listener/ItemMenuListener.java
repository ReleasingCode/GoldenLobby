package com.releasingcode.goldenlobby.modulos.inventarios.listener;

import com.releasingcode.goldenlobby.GoldenLobby;
import com.releasingcode.goldenlobby.managers.LobbyPlayer;
import com.releasingcode.goldenlobby.managers.LobbyPlayerMap;
import com.releasingcode.goldenlobby.modulos.inventarios.builder.ItemMenuHolder;
import com.releasingcode.goldenlobby.modulos.inventarios.manager.ItemSelector;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;

public class ItemMenuListener implements Listener {
    private static final ItemMenuListener instancia = new ItemMenuListener();
    private Plugin plugin = null;

    private ItemMenuListener() {
    }

    public static ItemMenuListener getInstance() {
        return instancia;
    }

    public static void closeOpenMenus() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getOpenInventory() != null) {
                Inventory inventory = player.getOpenInventory().getTopInventory();
                if (inventory.getHolder() instanceof ItemMenuHolder) {
                    player.closeInventory();
                }
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            LobbyPlayer lobbyPlayer = LobbyPlayerMap.getJugador(player);
            Inventory inventory = player.getOpenInventory().getTopInventory();
            if (inventory.getHolder() instanceof ItemMenuHolder) {
                lobbyPlayer.getInventoryManager().resetIndexing();
                ((ItemMenuHolder) inventory.getHolder()).getInventario().removePlayer(player.getUniqueId());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player && event.getInventory().getHolder() != null
                && event.getInventory().getHolder() instanceof ItemMenuHolder && event.getCurrentItem() != null
                && event.getInventory().getType() != InventoryType.FURNACE) {
            event.setCancelled(true);
            ((ItemMenuHolder) event.getInventory().getHolder()).getMenu().onInventoryClick(event);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryItem(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            ItemStack current = event.getCurrentItem();
            if (event.getClick() == ClickType.NUMBER_KEY) {
                current = event.getView().getBottomInventory().getItem(event.getHotbarButton());
            }
            if (ItemSelector.hasItemSelector(current)) {
                event.setCancelled(true);
            } else if (event.getAction() == InventoryAction.HOTBAR_SWAP) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onDrop(PlayerDropItemEvent event) {
        ItemStack stack = event.getItemDrop().getItemStack();
        if (ItemSelector.hasItemSelector(stack)) {
            event.setCancelled(true);
        }
    }

    public void register(GoldenLobby plugin) {
        if (!isRegistered(plugin)) {
            plugin.getServer().getPluginManager().registerEvents(instancia, plugin);
            this.plugin = plugin;
        }
    }

    public boolean isRegistered(GoldenLobby plugin) {
        if (plugin.equals(this.plugin)) {
            for (RegisteredListener listener : HandlerList.getRegisteredListeners(plugin)) {
                return listener.getListener().equals(instancia);
            }
        }
        return false;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPluginDisable(PluginDisableEvent event) {
        if (event.getPlugin().equals(plugin)) {
            closeOpenMenus();
            plugin = null;
        }
    }
}