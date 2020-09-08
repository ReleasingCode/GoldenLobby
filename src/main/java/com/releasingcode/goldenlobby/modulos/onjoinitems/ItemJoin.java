package com.releasingcode.goldenlobby.modulos.onjoinitems;

import com.releasingcode.goldenlobby.Utils;
import com.releasingcode.goldenlobby.configuracion.CustomConfiguration;
import com.releasingcode.goldenlobby.managers.ItemStackBuilder;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class ItemJoin {
    private final CustomConfiguration configuration;
    private final ArrayList<ItemPlayer> itemPlayers;
    private final boolean clear_hotbar, giveItems;
    private final int clear_hotbar_after, giveItemsAfter; // milliseconds

    public ItemJoin(CustomConfiguration customConfiguration) {
        this.configuration = customConfiguration;
        itemPlayers = new ArrayList<>();
        clear_hotbar_after = 500;
        clear_hotbar = false;
        giveItems = true;
        giveItemsAfter = 1000;
    }

    public int getClear_hotbar_after() {
        return clear_hotbar_after;
    }

    public int getGiveItemsAfter() {
        return giveItemsAfter;
    }

    public boolean isGiveItems() {
        return giveItems;
    }

    public boolean isClear_hotbar() {
        return clear_hotbar;
    }

    public void clearItemsPlayers() {
        itemPlayers.clear();
    }

    public ArrayList<ItemPlayer> getItemPlayers() {
        return itemPlayers;
    }

    public void loadItemJoin() {
        FileConfiguration config = configuration.getConfig();
        for (String items : config.getConfigurationSection("Items").getKeys(false)) {
            String pathItems = "Items." + items + ".";
            String nameItem = config.getString(pathItems + "Name", "&aDefault Name");
            String item = config.getString(pathItems + "Item", "stone:1:0");
            int slot = config.getInt(pathItems + "Slot", -1);
            ItemPlayer itemPlayer = new ItemPlayer(item, nameItem, config.getString(pathItems + "Lore", ""), slot);
            itemPlayers.add(itemPlayer);
        }
    }

    public void givePlayer(Player player) {
        for (ItemPlayer itemPlayer : itemPlayers) {
            int slot = itemPlayer.getSlot();
            if (slot != -1) {
                String item = itemPlayer.getItem().replace("{player}", player.getName());
                String parsedName = Utils.chatColor(itemPlayer.getName().replace("{player}", player.getName()));
                ArrayList<String> parsedLore = Utils.stringToArrayList(itemPlayer.getLore()).stream().map(lore -> lore.replace("{player}", player.getName())).collect(Collectors.toCollection(ArrayList::new));
                ItemStack stack = new ItemStackBuilder(item).setName(parsedName).addLore(parsedLore).build();
                if (stack != null) {
                    player.getInventory().setItem(slot, stack);
                    player.updateInventory();
                }
            }
        }
    }

    public CustomConfiguration getConfiguration() {
        return configuration;
    }
}
