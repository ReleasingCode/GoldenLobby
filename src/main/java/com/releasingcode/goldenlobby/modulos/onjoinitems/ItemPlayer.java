package com.releasingcode.goldenlobby.modulos.onjoinitems;

import com.releasingcode.goldenlobby.Utils;

import java.util.ArrayList;

public class ItemPlayer {
    private final int slot;
    private String name;
    private String lore;
    private String Item;

    public ItemPlayer(String item, String name, String lore, int slot) {
        this.name = name;
        this.lore = lore;
        this.Item = item;
        this.slot = slot;
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

    public ArrayList<String> getLoreArray() {
        return Utils.stringToArrayList(lore);
    }
}
