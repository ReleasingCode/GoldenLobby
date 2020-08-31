package com.releasingcode.goldenlobby.extendido.nms.v1_8_R3.packets;

import net.minecraft.server.v1_8_R3.PacketPlayOutEntity;


public class PacketPlayOutEntityMoveWrapper {
    public PacketPlayOutEntity.PacketPlayOutRelEntityMove create(int entityId) {
        return new PacketPlayOutEntity.PacketPlayOutRelEntityMove(
                entityId, (byte) 0, (byte) (+2.2),
                (byte) 0, false);
    }

    public PacketPlayOutEntity.PacketPlayOutRelEntityMove createReset(int entityId) {
        return new PacketPlayOutEntity.PacketPlayOutRelEntityMove(
                entityId, (byte) 0, (byte) (-2.2),
                (byte) 0, false);
    }
}
