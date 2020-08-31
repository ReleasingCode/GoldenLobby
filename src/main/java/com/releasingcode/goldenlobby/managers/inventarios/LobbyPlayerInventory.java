package com.releasingcode.goldenlobby.managers.inventarios;

import com.releasingcode.goldenlobby.managers.indexing.LobbyPlayerIndexing;
import com.releasingcode.goldenlobby.modulos.inventarios.manager.ItemMenu;

public class LobbyPlayerInventory {

    private ItemMenu inventario;
    private LobbyPlayerIndexing indexing;

    public LobbyPlayerInventory() {
        inventario = null;
        indexing = new LobbyPlayerIndexing();
    }

    public void setInventario(ItemMenu inventario) {
        this.inventario = inventario;
    }

    public void resetIndexing() {
        indexing = new LobbyPlayerIndexing();
    }

    public LobbyPlayerIndexing getIndexing() {
        return indexing;
    }

    public ItemMenu getMenu() {
        return inventario;
    }

}
