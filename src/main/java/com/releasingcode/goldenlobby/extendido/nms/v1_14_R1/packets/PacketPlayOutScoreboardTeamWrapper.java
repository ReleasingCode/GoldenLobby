package com.releasingcode.goldenlobby.extendido.nms.v1_14_R1.packets;

import es.minecub.core.apis.reflection.Reflection;
import net.minecraft.server.v1_14_R1.ChatComponentText;
import net.minecraft.server.v1_14_R1.IChatBaseComponent;
import net.minecraft.server.v1_14_R1.PacketPlayOutScoreboardTeam;

import java.util.Collection;
import java.util.Collections;


public class PacketPlayOutScoreboardTeamWrapper {

    public PacketPlayOutScoreboardTeam createRegisterTeam(String name) {
        PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = new PacketPlayOutScoreboardTeam();

        Reflection.getField(packetPlayOutScoreboardTeam.getClass(), "i", int.class)
                .set(packetPlayOutScoreboardTeam, 0);
        Reflection.getField(packetPlayOutScoreboardTeam.getClass(), "a", String.class)
                .set(packetPlayOutScoreboardTeam, name);
        Reflection.getField(packetPlayOutScoreboardTeam.getClass(), "b", IChatBaseComponent.class)
                .set(packetPlayOutScoreboardTeam, new ChatComponentText(name));
        Reflection.getField(packetPlayOutScoreboardTeam.getClass(), "e", String.class)
                .set(packetPlayOutScoreboardTeam, "never");
        Reflection.getField(packetPlayOutScoreboardTeam.getClass(), "f", String.class)
                .set(packetPlayOutScoreboardTeam, "never");
        Reflection.getField(packetPlayOutScoreboardTeam.getClass(), "j", int.class)
                .set(packetPlayOutScoreboardTeam, 0);
        Reflection.FieldAccessor<Collection> collectionFieldAccessor = Reflection.getField(
                packetPlayOutScoreboardTeam.getClass(), "h", Collection.class);
        collectionFieldAccessor.set(packetPlayOutScoreboardTeam, Collections.singletonList(name));

        return packetPlayOutScoreboardTeam;
    }

    public PacketPlayOutScoreboardTeam createUnregisterTeam(String name) {
        PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = new PacketPlayOutScoreboardTeam();

        Reflection.getField(packetPlayOutScoreboardTeam.getClass(), "i", int.class)
                .set(packetPlayOutScoreboardTeam, 1);
        Reflection.getField(packetPlayOutScoreboardTeam.getClass(), "a", String.class)
                .set(packetPlayOutScoreboardTeam, name);

        return packetPlayOutScoreboardTeam;
    }
}
