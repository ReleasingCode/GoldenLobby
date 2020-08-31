package com.releasingcode.goldenlobby.extendido.nms.v1_8_R3.packets;

import es.minecub.core.apis.reflection.Reflection;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.PacketPlayOutBed;
import org.bukkit.Location;


public class PacketPlayOutEntityBedWrapper {
    public PacketPlayOutBed create(int entityId, Location location) {
        PacketPlayOutBed sleep = new PacketPlayOutBed();
        BlockPosition position = new BlockPosition(location.getBlockX(), 0, location.getBlockZ());
        Reflection.getField(sleep.getClass(), "a", int.class).
                set(sleep, entityId);
        Reflection.getField(sleep.getClass(), "b", BlockPosition.class).
                set(sleep, position);
        return sleep;
    }
}
