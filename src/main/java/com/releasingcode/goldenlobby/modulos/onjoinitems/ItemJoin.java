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
    private boolean clear_hotbar;
    private boolean giveItems;
    private int clear_hotbar_after;
    private int giveItemsAfter; // milliseconds
    private int HeldItemSlotStartOnJoin;
    private int HeldItemSlotStartOnRespawn;

    public ItemJoin(CustomConfiguration customConfiguration) {
        this.configuration = customConfiguration;
        itemPlayers = new ArrayList<>();
        clear_hotbar_after = 20;
        clear_hotbar = false;
        giveItems = true;
        giveItemsAfter = 40;
        HeldItemSlotStartOnJoin = -1;
        HeldItemSlotStartOnRespawn = -1;
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

    public ItemPlayer getItemJoin(ItemStack stack) {
        for (ItemPlayer itemPlayer : getItemPlayers()) {
            ItemStack generated = itemPlayer.generateTemporalItemStack();
            if (generated != null) {
                if (ItemStackBuilder.equalsItem(stack, generated)) {
                    return itemPlayer;
                }
            }
        }
        return null;
    }

    public int getHeldItemSlotStartOnJoin() {
        return HeldItemSlotStartOnJoin;
    }

    public int getHeldItemSlotStartOnRespawn() {
        return HeldItemSlotStartOnRespawn;
    }

    public void loadItemJoin() {
        FileConfiguration config = configuration.getConfig();
        clear_hotbar = config.getBoolean("Items-OnJoin.Clear-Hotbar", true);
        giveItems = config.getBoolean("Items-OnJoin.Give-Items", true);
        clear_hotbar_after = config.getInt("Items-OnJoin.Clear-After", 20);
        giveItemsAfter = config.getInt("Items-OnJoin.Give-Items-After", 40);
        HeldItemSlotStartOnJoin = config.getInt("Held-Item-Slot.On-Join", -1);
        HeldItemSlotStartOnRespawn = config.getInt("Held-Item-Slot.On-Respawn", -1);
        for (String items : config.getConfigurationSection("Items").getKeys(false)) {
            String pathItems = "Items." + items + ".";
            String nameItem = config.getString(pathItems + "Name", "&aDefault Name");
            String item = config.getString(pathItems + "Item", "stone:1:0");
            int slot = config.getInt(pathItems + "Slot", -1);
            ItemPlayer itemPlayer = new ItemPlayer(
                    items.toLowerCase(),
                    item,
                    nameItem,
                    config.getString(pathItems + "Lore", ""),
                    slot,
                    config.getString(pathItems + "Executors", ""),
                    config.getInt(pathItems + "Use-Cooldown", 0),
                    config.getString(pathItems + "Click-Use", "LEFT"),
                    config.getString(pathItems + "Permission.User-Permission", null),
                    config.getString(pathItems + "Permission.No-Permission", null),
                    config.getBoolean(pathItems + "Drop-On-Player-Death", false),
                    config.getBoolean(pathItems + "No-Movable-Item", false),
                    config.getBoolean(pathItems + "Give-On-Respawn", true)
            );
            itemPlayers.add(itemPlayer);
        }
    }

    public void givePlayer(Player player, boolean isJoin) {
        for (ItemPlayer itemPlayer : itemPlayers) {
            int slot = itemPlayer.getSlot();
            if (slot != -1) {
                String item;
                String parsedName;
                ArrayList<String> parsedLore;
                item = itemPlayer.getItem().replace("{player}", player.getName());
                parsedName = Utils.chatColor(itemPlayer.getName().replace("{player}", player.getName()));
                parsedLore = Utils.stringToArrayList(itemPlayer.getLore()).stream().map(lore -> lore.replace("{player}", player.getName())).collect(Collectors.toCollection(ArrayList::new));
                ItemStack stack = new ItemStackBuilder(item).setName(parsedName).addLore(parsedLore).build();
                if (isJoin) {
                    if (stack != null) {
                        player.getInventory().setItem(slot, stack);
                        player.updateInventory();
                    }
                } else {
                    if (itemPlayer.isGiveOnRespawn()) {
                        if (stack != null) {
                            player.getInventory().setItem(slot, stack);
                            player.updateInventory();
                        }
                    }
                }
            }
        }
    }

    public CustomConfiguration getConfiguration() {
        return configuration;
    }
}
