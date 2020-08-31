package com.releasingcode.goldenlobby.modulos.inventarios.builder;

import com.releasingcode.goldenlobby.modulos.inventarios.manager.Inventario;
import com.releasingcode.goldenlobby.modulos.inventarios.manager.ItemMenu;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class ItemMenuHolder implements InventoryHolder {
    private final ItemMenu menu;
    private final Inventory inventory;
    private final Inventario inventario;

    public ItemMenuHolder(ItemMenu menu, Inventory inventory, Inventario inventario) {
        this.menu = menu;
        this.inventory = inventory;
        this.inventario = inventario;
    }

    public Inventario getInventario() {
        return inventario;
    }

    public ItemMenu getMenu() {
        return menu;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}