package com.releasingcode.goldenlobby.modulos.inventarios.builder;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class ItemClickEvent {
    private final Player player;
    private final ClickType clicktype;
    private final ItemStack stack;
    private boolean goBack = false;
    private boolean close = false;
    private boolean update = false;

    public ItemClickEvent(Player player, ItemStack stack, ClickType type) {
        this.player = player;
        this.stack = stack;
        this.clicktype = type;
    }

    public Player getPlayer() {
        return player;
    }

    public ClickType getClickType() {
        return clicktype;
    }

    public ItemStack getClickedItem() {
        return stack;
    }

    public boolean willGoBack() {
        return goBack;
    }

    public void setWillGoBack(boolean goBack) {
        this.goBack = goBack;
        if (goBack) {
            close = false;
            update = false;
        }
    }

    public boolean willClose() {
        return close;
    }

    public void setWillClose(boolean close) {
        this.close = close;
        if (close) {
            goBack = false;
            update = false;
        }
    }

    public boolean willUpdate() {
        return update;
    }

    public void setWillUpdate(boolean update) {
        this.update = update;
        if (update) {
            goBack = false;
            close = false;
        }
    }
}