package com.releasingcode.goldenlobby.extendido.nms.v1_8_R3.packets;


import es.minecub.core.apis.reflection.Reflection;
import net.minecraft.server.v1_8_R3.PacketPlayOutAttachEntity;

public class PacketPlayOutEntitySit {


    /*public PacketPlayOutSpawnEntityLiving spawnEntityLiving(Location locationSit, Entity vehicle) {
        PacketPlayOutSpawnEntityLiving packetVehicle = new PacketPlayOutSpawnEntityLiving(vehicle);
        Reflection.getField(packetVehicle.getClass(), "c", int.class).
                set(packetVehicle, MathHelper.floor(locationSit.getX() * 32.0D));
        Reflection.getField(packetVehicle.getClass(), "d", int.class).
                set(packetVehicle, MathHelper.floor(locationSit.getY() * 32.0D));
        Reflection.getField(packetVehicle.getClass(), "e", int.class).
                set(packetVehicle, MathHelper.floor(locationSit.getZ() * 32.0D));
        Reflection.getField(packetVehicle.getClass(), "i", byte.class).
                set(packetVehicle, (byte) ((int) (locationSit.getYaw() * 256.0F / 360.0F)));
        Reflection.getField(packetVehicle.getClass(), "j", byte.class).
                set(packetVehicle, (byte) ((int) (locationSit.getPitch() * 256.0F / 360.0F)));
        DataWatcher dataWatcher = new DataWatcher(null);
        dataWatcher.a(10, (byte) 127);
        dataWatcher.a(0, (byte) 32);
        Reflection.getField(packetVehicle.getClass(), "l", DataWatcher.class)
                .set(packetVehicle, dataWatcher);
        return packetVehicle;
    }
*/
    public PacketPlayOutAttachEntity create(int entityId, int entityVehicle) {
        PacketPlayOutAttachEntity attachEntity = new PacketPlayOutAttachEntity();
        Reflection.getField(attachEntity.getClass(), "a", int.class).
                set(attachEntity, 0);
        Reflection.getField(attachEntity.getClass(), "b", int.class).
                set(attachEntity, entityId);
        Reflection.getField(attachEntity.getClass(), "c", int.class).
                set(attachEntity, entityVehicle);
        return attachEntity;
    }
}
