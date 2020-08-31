package com.releasingcode.goldenlobby.modulos.repartidor.items;

import com.releasingcode.goldenlobby.modulos.repartidor.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class Item {
    private final String name;
    private final String dName;
    private final String permission;
    private final int slot;
    private final int time;
    private final int coins;
    private final ItemType type;
    private final ItemStack item;

    Item(final String name, final String dName, final String permission, final List<String> lore, final int slot, final int time, final int coins, final ItemType type) {
        super();
        this.name = name;
        this.dName = dName;
        this.permission = permission;
        this.slot = slot;
        this.coins = coins;
        this.time = time;
        this.type = type;
        this.item = Utils.createItem(Material.STORAGE_MINECART, ChatColor.GREEN + dName, 1, lore);
    }

    public String getPermission() {
        return this.permission;
    }

    public String getName() {
        return this.name;
    }

    public String getDisplayName() {
        return this.dName;
    }

    public int getCoins() {
        return this.coins;
    }

    public int getSlot() {
        return this.slot;
    }

    public int getTime() {
        return this.time;
    }

    public ItemStack getItem() {
        return this.item;
    }

    public ItemType getType() {
        return this.type;
    }
}
