package com.releasingcode.goldenlobby.extendido.nms.v1_10_R1.packets;


import com.releasingcode.goldenlobby.extendido.reflection.Reflection;
import net.minecraft.server.v1_10_R1.PacketPlayOutEntityHeadRotation;
import org.bukkit.Location;


public class PacketPlayOutEntityHeadRotationWrapper {

    public PacketPlayOutEntityHeadRotation create(Location location, int entityId) {
        PacketPlayOutEntityHeadRotation packetPlayOutEntityHeadRotation = new PacketPlayOutEntityHeadRotation();

        Reflection.getField(packetPlayOutEntityHeadRotation.getClass(), "a", int.class).
                set(packetPlayOutEntityHeadRotation, entityId);
        Reflection.getField(packetPlayOutEntityHeadRotation.getClass(), "b", byte.class)
                .set(packetPlayOutEntityHeadRotation, (byte) ((int) location.getYaw() * 256.0F / 360.0F));

        return packetPlayOutEntityHeadRotation;
    }
}
