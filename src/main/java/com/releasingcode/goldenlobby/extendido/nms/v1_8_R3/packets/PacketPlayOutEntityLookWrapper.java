package com.releasingcode.goldenlobby.extendido.nms.v1_8_R3.packets;

import net.minecraft.server.v1_8_R3.PacketPlayOutEntity;

public class PacketPlayOutEntityLookWrapper {
    public PacketPlayOutEntity.PacketPlayOutEntityLook create(double yaw, double pitch, int entity) {
        return new PacketPlayOutEntity.PacketPlayOutEntityLook(
                entity, (byte) ((yaw % 360.) * 256 / 360), (byte) ((pitch % 360.) * 256 / 360), false
        );
    }
}
