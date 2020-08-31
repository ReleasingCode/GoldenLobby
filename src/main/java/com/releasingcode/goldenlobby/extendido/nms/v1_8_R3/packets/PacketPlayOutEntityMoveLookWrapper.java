package com.releasingcode.goldenlobby.extendido.nms.v1_8_R3.packets;

import net.minecraft.server.v1_8_R3.PacketPlayOutEntity;


public class PacketPlayOutEntityMoveLookWrapper {
    public PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook create(int entityId) {
        return new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook(
                entityId, (byte) 0, (byte) (+2.2),
                (byte) 0, (byte) 20, (byte) 20, false);
    }

    public PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook createReset(int entityId) {
        return new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook(
                entityId, (byte) 0, (byte) (+2.2),
                (byte) 0, (byte) 20, (byte) 20, false);
    }
}
