package com.releasingcode.goldenlobby.modulos.repartidor.playerdata;


import com.releasingcode.goldenlobby.modulos.repartidor.manager.RepartidorManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

public class MinecubPlayer {
    private final Map<String, Long> longs;
    private final String p;
    private final Inventory inv;
    private boolean error;
    private boolean loaded;

    public MinecubPlayer(final String p) {
        this.longs = new HashMap<>();
        this.p = p;
        RepartidorManager.loadItems(this);
        this.inv = Bukkit
                .createInventory(null, this.error ? 9 : RepartidorManager.getSlots(), "${lobby.inventories.deliveryman}");
    }

    public void setError(final boolean error) {
        this.error = error;
    }

    public void setLoaded(final boolean loaded) {
        this.loaded = loaded;
    }

    public boolean hasAnyError() {
        return this.error;
    }

    public Map<String, Long> getLongs() {
        return this.longs;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(p);
    }

    public Inventory getInventory() {
        return this.inv;
    }

    public boolean hasLoaded() {
        return this.loaded;
    }

    public boolean hasOnline() {
        return getPlayer() != null && getPlayer().isOnline();
    }
}
