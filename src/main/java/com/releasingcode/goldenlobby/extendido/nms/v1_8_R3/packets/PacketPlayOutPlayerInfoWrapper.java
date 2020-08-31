package com.releasingcode.goldenlobby.extendido.nms.v1_8_R3.packets;

import com.mojang.authlib.GameProfile;
import es.minecub.core.apis.reflection.Reflection;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_8_R3.WorldSettings;

import java.util.Collections;
import java.util.List;


public class PacketPlayOutPlayerInfoWrapper {
    public PacketPlayOutPlayerInfo create(PacketPlayOutPlayerInfo.EnumPlayerInfoAction action, GameProfile gameProfile, String name) {
        PacketPlayOutPlayerInfo packetPlayOutPlayerInfo = new PacketPlayOutPlayerInfo();
        Reflection.getField(packetPlayOutPlayerInfo.getClass(), "a", PacketPlayOutPlayerInfo.EnumPlayerInfoAction.class)
                .set(packetPlayOutPlayerInfo, action);
        PacketPlayOutPlayerInfo.PlayerInfoData playerInfoData = packetPlayOutPlayerInfo.new
                PlayerInfoData(gameProfile, 1,
                               WorldSettings.EnumGamemode.NOT_SET,
                               IChatBaseComponent.ChatSerializer.a(
                                       "{\"text\":\"§8[NPC] " + name + "\",\"color\":\"dark_gray\"}"));
        Reflection.FieldAccessor<List> fieldAccessor =
                Reflection.getField(packetPlayOutPlayerInfo.getClass(), "b", List.class);
        fieldAccessor.set(packetPlayOutPlayerInfo, Collections.singletonList(playerInfoData));
        return packetPlayOutPlayerInfo;
    }
}
