package com.releasingcode.goldenlobby.modulos.regions.tienda;

import com.releasingcode.goldenlobby.modulos.regions.RegionPlugin;
import es.minecub.core.minecubos.MinecubosAPI;
import es.minecub.core.translations.translator.TranslatorAPI;
import es.minecub.core.utils.PlayerUtils;
import net.minecraft.server.v1_8_R3.ItemArmor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.NoSuchElementException;

public class ShopInventory implements Listener {
    private final RegionPlugin plugin;

    public ShopInventory(RegionPlugin plugin) {
        this.plugin = plugin;
    }

    public void openInventory(Player player) {
        player.openInventory(buildInventory());
    }

    private Inventory buildInventory() {
        ItemStackConfigParser parser = plugin.getShopItemsParser();
        Inventory inv = Bukkit.createInventory(null, parser.getInvSize(), plugin.getShopItemsParser().getInvName());
        parser.getItems().forEach((slot, item) -> {
            inv.setItem(slot, item.getItem());
        });

        return inv;
    }

    @EventHandler
    private void onItemClick(InventoryClickEvent e) {
        if (!plugin.getShopItemsParser().getInvName().equals(e.getView().getTopInventory().getTitle())
                || !(e.getWhoClicked() instanceof Player)
                || !e.getClickedInventory().equals(e.getView().getTopInventory())) return;

        e.setCancelled(true);
        Player player = ((Player) e.getWhoClicked());

        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage("${lobbymc.regions.shop.inventoryfull}");
            return;
        }

        int slot = e.getSlot();

        if (!plugin.getShopItemsParser().itemExistsAtSlot(slot)) return;

        ItemStackConfigParser.ShopItem shopItem = plugin.getShopItemsParser().getItems().get(slot);
        ItemStack item = shopItem.getItem();
        boolean isAnArmor = isArmor(item);

        if (isAnArmor && hasArmor(player, item)) {
            player.sendMessage("${lobbymc.regions.shop.armorequipped}");
            return;
        }

        int minecubos = MinecubosAPI.getMinecubos(player);
        int price = shopItem.getPrice();

        if (minecubos < price) {
            player.sendMessage("${lobbymc.regions.shop.notenoughminecubos}");
            return;
        }

        if (isAnArmor) {
            setArmor(player, item);
        } else {
            player.getInventory().addItem(removeLore(item));
        }

        item = TranslatorAPI.translateItem(item, PlayerUtils.getLanguage(player));

        MinecubosAPI.takeMinecubos(player, price);
        player.sendMessage("${lobbymc.regions.shop.bought}[" + item.getAmount() + ", " + ChatColor.RESET + item.getItemMeta().getDisplayName() + "]");
    }

    private ItemStack removeLore(ItemStack item) {
        item = item.clone();
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setLore(new ArrayList<>());
        item.setItemMeta(itemMeta);
        return item;
    }

    private boolean isArmor(ItemStack item) {
        return CraftItemStack.asNMSCopy(item).getItem() instanceof ItemArmor;
    }

    private boolean hasArmor(Player player, ItemStack armor) {
        String endsWith = armor.getType().name().split("_")[1];
        PlayerInventory inventory = player.getInventory();

        switch (endsWith) {
            case "HELMET":
                return inventory.getHelmet() != null;
            case "CHESTPLATE":
                return inventory.getChestplate() != null;
            case "LEGGINGS":
                return inventory.getLeggings() != null;
            case "BOOTS":
                return inventory.getBoots() != null;
            default:
                player.sendMessage("${error}[SHOPSx01]");
                throw new NoSuchElementException("No se encontró armadura para el item que termina con: " + endsWith);
        }
    }

    private void setArmor(Player player, ItemStack armor) {
        String endsWith = armor.getType().name().split("_")[1];
        PlayerInventory inventory = player.getInventory();

        switch (endsWith) {
            case "HELMET":
                inventory.setHelmet(armor);
                break;
            case "CHESTPLATE":
                inventory.setChestplate(armor);
                break;
            case "LEGGINGS":
                inventory.setLeggings(armor);
                break;
            case "BOOTS":
                inventory.setBoots(armor);
                break;
            default:
                player.sendMessage("${error}[SHOPSx01]");
                throw new NoSuchElementException("No se encontró armadura para el item que termina con: " + endsWith);
        }

    }

}
