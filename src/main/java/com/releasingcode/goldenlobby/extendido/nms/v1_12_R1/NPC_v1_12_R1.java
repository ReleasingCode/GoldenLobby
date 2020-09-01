package com.releasingcode.goldenlobby.extendido.nms.v1_12_R1;

import com.releasingcode.goldenlobby.connections.ServerManager;
import com.releasingcode.goldenlobby.extendido.nms.v1_12_R1.packets.*;
import com.releasingcode.goldenlobby.npc.NPCLib;
import com.releasingcode.goldenlobby.npc.api.state.NPCPosition;
import com.releasingcode.goldenlobby.npc.api.state.NPCSlot;
import com.releasingcode.goldenlobby.npc.hologram.Hologram;
import com.releasingcode.goldenlobby.npc.internal.MinecraftVersion;
import com.releasingcode.goldenlobby.npc.internal.NPCBase;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;


public class NPC_v1_12_R1 extends NPCBase {

    private PacketPlayOutNamedEntitySpawn packetPlayOutNamedEntitySpawn;
    private PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeamRegister;
    private PacketPlayOutPlayerInfo packetPlayOutPlayerInfoAdd, packetPlayOutPlayerInfoRemove;
    private PacketPlayOutEntityHeadRotation packetPlayOutEntityHeadRotation;
    private PacketPlayOutEntityDestroy packetPlayOutEntityDestroy;

    public NPC_v1_12_R1(NPCLib instance, String name, List<String> lines, UUID uuid, String nameNPC) {
        super(instance, name, lines, uuid, nameNPC);
    }

    @Override
    public void createPackets() {
        this.hologram = new Hologram(MinecraftVersion.V1_12_R1, location.clone().add(0, 0.5, 0), text);

        PacketPlayOutPlayerInfoWrapper packetPlayOutPlayerInfoWrapper = new PacketPlayOutPlayerInfoWrapper();

        // Packets for spawning the NPC:
        this.packetPlayOutScoreboardTeamRegister = new PacketPlayOutScoreboardTeamWrapper()
                .createRegisterTeam(name); // First packet to send.

        this.packetPlayOutPlayerInfoAdd = packetPlayOutPlayerInfoWrapper
                .create(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, gameProfile,
                        name); // Second packet to send.

        this.packetPlayOutNamedEntitySpawn = new PacketPlayOutNamedEntitySpawnWrapper()
                .create(uuid, location, entityId); // Third packet to send.

        this.packetPlayOutEntityHeadRotation = new PacketPlayOutEntityHeadRotationWrapper()
                .create(location, entityId); // Fourth packet to send.

        this.packetPlayOutPlayerInfoRemove = packetPlayOutPlayerInfoWrapper
                .create(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, gameProfile,
                        name); // Fifth packet to send (delayed).

        // Packet for destroying the NPC:
        this.packetPlayOutEntityDestroy = new PacketPlayOutEntityDestroy(entityId); // First packet to send.
    }

    @Override
    public void sendShowPackets(Player player) {
        PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;

        if (hasTeamRegistered.add(player.getUniqueId()))
            playerConnection.sendPacket(packetPlayOutScoreboardTeamRegister);
        playerConnection.sendPacket(packetPlayOutPlayerInfoAdd);
        playerConnection.sendPacket(packetPlayOutNamedEntitySpawn);
        playerConnection.sendPacket(packetPlayOutEntityHeadRotation);

        hologram.showOrUpdate(player, ServerManager.translateVar(text, this));

        // Removing the player info after 10 seconds.
        Bukkit.getScheduler().runTaskLater(NPCLib.getPlugin(), () ->
                playerConnection.sendPacket(packetPlayOutPlayerInfoRemove), 200);
    }

    @Override
    public void sendHidePackets(Player player) {
        PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;

        playerConnection.sendPacket(packetPlayOutEntityDestroy);
        playerConnection.sendPacket(packetPlayOutPlayerInfoRemove);

        hologram.hide(player);
    }

    @Override
    public void setStatus(Player player, NPCPosition position) {

    }

    @Override
    public void setStatus(NPCPosition position) {

    }

    @Override
    public void sendMetadataPacket(Player player) {
        PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;
        PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadataWrapper().create(activeStates, entityId);

        playerConnection.sendPacket(packet);
    }

    @Override
    public void sendEquipmentPacket(Player player, NPCSlot slot, boolean auto) {
        PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;

        EnumItemSlot nmsSlot = slot.getNmsEnum(EnumItemSlot.class);
        ItemStack item = getItem(slot);

        PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(entityId, nmsSlot,
                CraftItemStack.asNMSCopy(item));
        playerConnection.sendPacket(packet);
    }

    @Override
    public void sendLookAtPlayer(Player player) {

    }

    @Override
    public void updateSkin() {
        this.packetPlayOutPlayerInfoAdd = new PacketPlayOutPlayerInfoWrapper()
                .create(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, gameProfile, name);
    }
}
