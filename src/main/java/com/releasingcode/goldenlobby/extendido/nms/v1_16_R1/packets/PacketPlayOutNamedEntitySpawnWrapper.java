package com.releasingcode.goldenlobby.extendido.nms.v1_16_R1.packets;

import com.releasingcode.goldenlobby.extendido.reflection.Reflection;
import net.minecraft.server.v1_16_R1.PacketPlayOutNamedEntitySpawn;
import org.bukkit.Location;

import java.util.UUID;


public class PacketPlayOutNamedEntitySpawnWrapper {

    public PacketPlayOutNamedEntitySpawn create(UUID uuid, Location location, int entityId) {
        PacketPlayOutNamedEntitySpawn packetPlayOutNamedEntitySpawn = new PacketPlayOutNamedEntitySpawn();

        Reflection.getField(packetPlayOutNamedEntitySpawn.getClass(), "a", int.class)
                .set(packetPlayOutNamedEntitySpawn, entityId);
        Reflection.getField(packetPlayOutNamedEntitySpawn.getClass(), "b", UUID.class)
                .set(packetPlayOutNamedEntitySpawn, uuid);
        Reflection.getField(packetPlayOutNamedEntitySpawn.getClass(), "c", double.class)
                .set(packetPlayOutNamedEntitySpawn, location.getX());
        Reflection.getField(packetPlayOutNamedEntitySpawn.getClass(), "d", double.class)
                .set(packetPlayOutNamedEntitySpawn, location.getY());
        Reflection.getField(packetPlayOutNamedEntitySpawn.getClass(), "e", double.class)
                .set(packetPlayOutNamedEntitySpawn, location.getZ());
        Reflection.getField(packetPlayOutNamedEntitySpawn.getClass(), "f", byte.class)
                .set(packetPlayOutNamedEntitySpawn, (byte) ((int) (location.getYaw() * 256.0F / 360.0F)));
        Reflection.getField(packetPlayOutNamedEntitySpawn.getClass(), "g", byte.class)
                .set(packetPlayOutNamedEntitySpawn, (byte) ((int) (location.getPitch() * 256.0F / 360.0F)));

        return packetPlayOutNamedEntitySpawn;
    }
}
