package com.releasingcode.goldenlobby.modulos.onjoinitems;

import com.releasingcode.goldenlobby.Utils;
import com.releasingcode.goldenlobby.managers.ItemStackBuilder;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ItemPlayer {
    private final String id;
    private final int slot;
    private String name;
    private String lore;
    private String Item;
    private final String executors;
    private final int useCooldown;
    private final String clickUse;
    private final String permission;
    private final String noPermissionMessage;
    private final boolean DropOnPlayerDeath;
    private final boolean NoMovableItem;
    private final boolean GiveOnRespawn;
    private final ArrayList<ItemPlayerClick> clicks;

    public ItemPlayer(String id, String item, String name, String lore, int slot, String executors, int useCooldown
            , String clickUses, String permission, String noPermissionMessage, boolean DropOnPlayerDeath, boolean NoMovableItem, boolean GiveOnRespawn) {
        this.id = id;
        this.name = name;
        this.lore = lore;
        this.Item = item;
        this.slot = slot;
        this.executors = executors;
        this.useCooldown = useCooldown;
        this.clickUse = clickUses;
        this.permission = permission;
        this.noPermissionMessage = noPermissionMessage;
        this.DropOnPlayerDeath = DropOnPlayerDeath;
        this.NoMovableItem = NoMovableItem;
        this.GiveOnRespawn = GiveOnRespawn;
        clicks = getClickUseArray();
    }

    public boolean isGiveOnRespawn() {
        return GiveOnRespawn;
    }

    public boolean isDropOnPlayerDeath() {
        return DropOnPlayerDeath;
    }

    public boolean isNoMovableItem() {
        return NoMovableItem;
    }

    public String getExecutors() {
        return executors;
    }

    public String getClickUse() {
        return clickUse;
    }

    public String getPermission() {
        return permission;
    }

    public String getNoPermissionMessage() {
        return noPermissionMessage;
    }

    public String getId() {
        return id;
    }

    public ArrayList<ItemPlayerClick> getClickUseArray() {
        if (clickUse.contains(",")) {
            ArrayList<String> clicks = new ArrayList<>(Arrays.asList(clickUse.split(",")));
            Set<ItemPlayerClick> clicksPlayer = new HashSet<>();
            for (String clicksText : clicks) {
                clicksPlayer.add(ItemPlayerClick.fromString(clicksText));
            }
            return new ArrayList<>(clicksPlayer);
        }
        return Stream.of(clickUse).map(ItemPlayerClick::fromString).collect(Collectors.toCollection(ArrayList::new));
    }

    public boolean isRightClick() {
        for (ItemPlayerClick click : clicks) {
            if (click == ItemPlayerClick.RIGHT) {
                return true;
            }
        }
        return false;
    }

    public boolean validateClickUser(Action action) {
        if (action.equals(Action.LEFT_CLICK_AIR) || action.equals(Action.LEFT_CLICK_BLOCK)) {
            return isLeftClick();
        }
        if (action.equals(Action.RIGHT_CLICK_AIR) || action.equals(Action.RIGHT_CLICK_BLOCK)) {
            return isRightClick();
        }
        return false;
    }

    public boolean isLeftClick() {
        for (ItemPlayerClick click : clicks) {
            if (click == ItemPlayerClick.LEFT) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<String> getExecutorsArray() {
        return new ArrayList<>(Arrays.asList(executors.split("\\n")));
    }

    public int getUseCooldown() {
        return useCooldown;
    }

    public int getSlot() {
        return slot;
    }

    public String getItem() {
        return Item;
    }

    public void setItem(String item) {
        Item = item;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLore() {
        return lore;
    }

    public void setLore(String lore) {
        this.lore = lore;
    }

    public ItemStack generateTemporalItemStack() {
        String item = getItem().replace("{player}", "0");
        String name = Utils.chatColor(getName().replace("{player}", ""));
        return new ItemStackBuilder(item).setName(name).addLore(Collections.emptyList()).build();
    }

    public ArrayList<String> getLoreArray() {
        return Utils.stringToArrayList(lore);
    }

    public enum ItemPlayerClick {
        LEFT, RIGHT;

        public static ItemPlayerClick fromString(String click) {
            for (ItemPlayerClick use : values()) {
                if (use.name().trim().toLowerCase().equals(click.trim().toLowerCase())) {
                    return use;
                }
            }
            return LEFT;
        }
    }
}
