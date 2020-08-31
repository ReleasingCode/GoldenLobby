package com.releasingcode.goldenlobby.modulos.inventarios.builder;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class MenuItem {
    private String displayName;
    private ItemStack icon;
    private List<String> lore;

    public MenuItem(ItemStack stack) {
        this.icon = stack;
    }

    public MenuItem(String displayName, ItemStack icon, String... lore2) {
        this.displayName = displayName;
        this.icon = icon;
        this.lore = new ArrayList<>();
        for (String preLore : lore2) {
            lore.add(ChatColor.translateAlternateColorCodes('&', preLore));
        }
    }

    public MenuItem(String displayName, ItemStack icon, List<String> lore2) {
        this.displayName = displayName;
        this.icon = icon;
        this.lore = new ArrayList<>();
        for (String preLore : lore2) {
            lore.add(ChatColor.translateAlternateColorCodes('&', preLore));
        }
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public void setDisplayName(String name) {
        this.displayName = name;
    }

    public ItemStack getIcon() {
        return this.icon;
    }

    public void setIcon(ItemStack newIcon) {
        this.icon = newIcon;
    }

    public List<String> getLore() {

        return this.lore;
    }

    public ItemStack getFinalIcon(Player player) {
        return setNameAndLore(this.getIcon().clone(), this.getDisplayName(), this.getLore());
    }

    public ItemStack setNameAndLore(ItemStack itemStack, String displayName, List<String> lore) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(displayName);
        List<String> loreColor = new ArrayList<>();
        for (String preLore : lore) {
            loreColor.add(ChatColor.translateAlternateColorCodes('&', preLore));
        }
        meta.setLore(loreColor);
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    public void onItemClick(ItemClickEvent event) {
        // NO Hacer nada por defecto
    }
}