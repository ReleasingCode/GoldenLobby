package com.releasingcode.goldenlobby.listeners.custom;

import com.releasingcode.goldenlobby.managers.LobbyPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SecurePlayerJoinEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final LobbyPlayer player;

    public SecurePlayerJoinEvent(LobbyPlayer player) {
        this.player = player;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public LobbyPlayer getPlayer() {
        return player;
    }

    public Player getBukkitPlayer() {
        return player.getPlayer();
    }

    public HandlerList getHandlers() {
        return handlers;
    }
}
