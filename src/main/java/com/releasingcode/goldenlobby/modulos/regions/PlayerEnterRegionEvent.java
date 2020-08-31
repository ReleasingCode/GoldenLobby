package com.releasingcode.goldenlobby.modulos.regions;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import com.releasingcode.goldenlobby.modulos.regions.cuboid.StageOfCreation;

public class PlayerEnterRegionEvent extends PlayerEvent {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private StageOfCreation.Regions region;
    private StageOfCreation.Regions oldRegion;

    public PlayerEnterRegionEvent(Player player, StageOfCreation.Regions region, StageOfCreation.Regions oldRegion) {
        super(player);
        this.region = region;
        this.oldRegion = oldRegion;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public StageOfCreation.Regions getRegion() {
        return region;
    }

    public StageOfCreation.Regions getOldRegion() {
        return oldRegion;
    }

    @Override public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

}
