package com.releasingcode.goldenlobby.npc.api.events;

import com.releasingcode.goldenlobby.npc.api.NPC;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;


public class NPCHideEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final NPC npc;
    private final Player player;
    private final boolean automatic;
    private boolean cancelled = false;

    public NPCHideEvent(NPC npc, Player player, boolean automatic) {
        this.npc = npc;
        this.player = player;
        this.automatic = automatic;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public NPC getNPC() {
        return npc;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}

