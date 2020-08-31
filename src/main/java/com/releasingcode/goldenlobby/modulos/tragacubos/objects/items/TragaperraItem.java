package com.releasingcode.goldenlobby.modulos.tragacubos.objects.items;

import es.minecub.core.minecubos.MinecubosAPI;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class TragaperraItem {
    private final ItemStack item;

    public TragaperraItem(ItemStack item) {
        this.item = item;
    }

    public boolean giftItem(Player player) {
        player.sendMessage(ChatColor.RED + "¡No ganaste ningún premio! Pero se te han devuelto 50 minecubos.");
        MinecubosAPI.giveMinecubos(player, 50, true);
        return false;
    }

    public ItemStack getItem() {
        return item;
    }

}
