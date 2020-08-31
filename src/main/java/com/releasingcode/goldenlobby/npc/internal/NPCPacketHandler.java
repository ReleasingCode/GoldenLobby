package com.releasingcode.goldenlobby.npc.internal;

import org.bukkit.entity.Player;
import com.releasingcode.goldenlobby.npc.api.state.NPCPosition;
import com.releasingcode.goldenlobby.npc.api.state.NPCSlot;

interface NPCPacketHandler {

    void createPackets();

    void sendShowPackets(Player player);

    void sendHidePackets(Player player);

    void sendMetadataPacket(Player player);

    void sendEquipmentPacket(Player player, NPCSlot slot, boolean auto);

    void sendLookAtPlayer(Player player);

    void setStatus(Player player, NPCPosition position);

    void setStatus(NPCPosition position);

    default void sendEquipmentPackets(Player player) {
        for (NPCSlot slot : NPCSlot.values())
            sendEquipmentPacket(player, slot, true);
    }
}
