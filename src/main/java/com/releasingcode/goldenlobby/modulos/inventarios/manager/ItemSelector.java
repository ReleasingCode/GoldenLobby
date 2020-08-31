package com.releasingcode.goldenlobby.modulos.inventarios.manager;

import com.releasingcode.goldenlobby.managers.ItemStackBuilder;
import org.bukkit.inventory.ItemStack;

public class ItemSelector {

    private String name;
    private String[] lore;
    private int slot;
    private String builderString;
    private ItemStack item;
    private boolean hasActive;
    private Inventario inventario;

    ItemSelector(Inventario inventario) {
        this.inventario = inventario;
    }

    ItemSelector(String builderString) {
        this.builderString = builderString;
    }

    public static boolean hasItemSelector(ItemStack stack) {
        if (stack != null) {
            for (ItemSelector selector : Inventario.getItemsSelector()) {
                if (ItemStackBuilder.equalsItem(stack, selector.getItem())) {
                    return true;
                }
            }
        }
        return false;
    }

    public ItemStack getItem() {
        return item;
    }

    public String getBuilderString() {
        return builderString;
    }

    public void setBuilderString(String builderString) {
        this.builderString = builderString;
    }

    public Inventario getInventario() {
        return inventario;
    }

    public ItemSelector build() {
        if (!this.builderString.isEmpty()) {
            this.item = new ItemStackBuilder(builderString)
                    .setName(name)
                    .addLore(lore)
                    .build();
            setHasActive(slot != -1);
            return this;
        }
        setHasActive(false);
        return this;
    }


    public void setHasActive(boolean hasActive) {
        this.hasActive = hasActive;
    }

    public boolean hasActive() {
        return hasActive;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getLore() {
        return lore;
    }

    public void setLore(String[] lore) {
        this.lore = lore;
    }

    public int getSlot() {
        return slot;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }
}
