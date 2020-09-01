package com.releasingcode.goldenlobby.extendido.nms.v1_8_R3;

import com.releasingcode.goldenlobby.connections.ServerManager;
import com.releasingcode.goldenlobby.extendido.nms.v1_8_R3.packets.*;
import com.releasingcode.goldenlobby.npc.NPCLib;
import com.releasingcode.goldenlobby.npc.api.state.NPCPosition;
import com.releasingcode.goldenlobby.npc.api.state.NPCSlot;
import com.releasingcode.goldenlobby.npc.hologram.Hologram;
import com.releasingcode.goldenlobby.npc.internal.MinecraftVersion;
import com.releasingcode.goldenlobby.npc.internal.NPCBase;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.List;
import java.util.UUID;

public class NPC_v1_8_R3 extends NPCBase {
    public final int South = 0;
    public final int West = 1;
    public final int North = 2;
    public final int East = 3;
    private PacketPlayOutNamedEntitySpawn packetPlayOutNamedEntitySpawn;
    private PacketPlayOutPlayerInfo packetPlayOutPlayerInfoAdd, packetPlayOutPlayerInfoRemove;
    private PacketPlayOutEntityHeadRotation packetPlayOutEntityHeadRotation;
    private PacketPlayOutEntityDestroy packetPlayOutEntityDestroy;

    public NPC_v1_8_R3(NPCLib instance, String name, List<String> lines, UUID uuid, String nameNPC) {
        super(instance, name, lines, uuid, nameNPC);
    }

    @Override
    public void createPackets() {

        this.hologram = new Hologram(MinecraftVersion.getNMSVersion(), location.clone().subtract(0,
                getPositionMemory() == NPCPosition.NORMAL ? -0.5 : (
                        getPositionMemory() == NPCPosition.CORPSE ? 1 : 0.5), 0), text);
        PacketPlayOutPlayerInfoWrapper packetPlayOutPlayerInfoWrapper = new PacketPlayOutPlayerInfoWrapper();
        this.packetPlayOutPlayerInfoAdd = packetPlayOutPlayerInfoWrapper
                .create(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, gameProfile,
                        name); // Second packet to send.
        this.packetPlayOutNamedEntitySpawn = new PacketPlayOutNamedEntitySpawnWrapper()
                .create(uuid, location, entityId); // Third packet to send.
        this.packetPlayOutEntityHeadRotation = new PacketPlayOutEntityHeadRotationWrapper()
                .headRotation(location, entityId); // Fourth packet to send.
        this.packetPlayOutPlayerInfoRemove = packetPlayOutPlayerInfoWrapper
                .create(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, gameProfile,
                        name); // Fifth packet to send (delayed).
        // Packet for destroying the NPC:
        this.packetPlayOutEntityDestroy = new PacketPlayOutEntityDestroy(entityId); // First packet to send.

        if (positionStatus == NPCPosition.SIT) {
            hologramMounted = new Hologram(MinecraftVersion.getNMSVersion(), location.clone().subtract(0,
                    2.1, 0));
        }
    }

    @Override
    public void sendShowPackets(Player player) {
        PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;
        playerConnection.sendPacket(packetPlayOutPlayerInfoAdd);
        playerConnection.sendPacket(packetPlayOutNamedEntitySpawn);
        playerConnection.sendPacket(packetPlayOutEntityHeadRotation);
        // player.sendMessage("-> hologram " + Arrays.asList(hologram.getText()).toString());
        //player.sendMessage("-> from npc " + Arrays.asList(text.toString()));

        Scoreboard sc = player.getScoreboard();
        Team team;
        // -----------------> Registra al NPC al Scoreboard para ocultar el Nametag
        if (sc.getTeam("") != null) {
            team = sc.getTeam("");
        } else {
            team = player.getScoreboard().registerNewTeam("");
        }
        if (!team.getEntries().contains("")) {
            team.addEntry("");
        }
        if (team.getNameTagVisibility() != NameTagVisibility.NEVER) {
            team.setNameTagVisibility(NameTagVisibility.NEVER);
        }//<--------------------
        sendShowStatus(player, positionStatus);
        hologram.showOrUpdate(player, ServerManager.translateVar(text, this));
        // Removing the player info after 6 seconds.
        Bukkit.getScheduler().runTaskLaterAsynchronously(NPCLib.getPlugin(), () ->
                playerConnection.sendPacket(packetPlayOutPlayerInfoRemove), 6 * 20);
    }

    @Override
    public void sendHidePackets(Player player) {
        PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;
        playerConnection.sendPacket(packetPlayOutEntityDestroy);
        playerConnection.sendPacket(packetPlayOutPlayerInfoRemove);
        hologram.hide(player);
        removeMounted(player);
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

        if (slot == NPCSlot.OFFHAND) {
            if (!auto) {
                throw new UnsupportedOperationException("Offhand is not supported on servers below 1.9");
            }
            return;
        }
        ItemStack item = getItem(slot);
        PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(entityId, slot.getSlot(),
                CraftItemStack.asNMSCopy(item));
        playerConnection.sendPacket(packet);
    }

    public void sendLookAtArmorStand(Player player) {
        PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;
        PacketPlayOutEntityHeadRotation mob = new PacketPlayOutEntityHeadRotationWrapper()
                .headRotation(location, hologramMounted.getId()); // rotar cabeza
        PacketPlayOutEntity.PacketPlayOutEntityLook lookMob = new PacketPlayOutEntityLookWrapper()
                .create(location.getYaw(), location.getPitch(), hologramMounted.getId());
        playerConnection.sendPacket(mob);
        playerConnection.sendPacket(lookMob);
    }

    @Override
    public void sendLookAtPlayer(Player player) {
        if (positionStatus == NPCPosition.CORPSE) {
            return;
        }

        PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;
        Location vectorLocation = location.setDirection(player.getLocation().subtract(location).toVector());
        double yaw = vectorLocation.getYaw();
        double pitch = vectorLocation.getPitch();
        if (positionStatus == NPCPosition.SIT) {
            pitch = pitch - 5;
            if (hologramMounted.isShowing(player)) {
                PacketPlayOutEntityHeadRotation mob = new PacketPlayOutEntityHeadRotationWrapper()
                        .headRotation(vectorLocation, hologramMounted.getId()); // rotar cabeza
                PacketPlayOutEntity.PacketPlayOutEntityLook lookMob = new PacketPlayOutEntityLookWrapper()
                        .create(yaw, pitch, hologramMounted.getId());
                playerConnection.sendPacket(mob);
                playerConnection.sendPacket(lookMob);
            }
        }
        this.packetPlayOutEntityHeadRotation = new PacketPlayOutEntityHeadRotationWrapper()
                .headRotation(vectorLocation, entityId); // rotar cabeza
        PacketPlayOutEntity.PacketPlayOutEntityLook lookWrapper = new PacketPlayOutEntityLookWrapper()
                .create(yaw, pitch, entityId);
        playerConnection.sendPacket(lookWrapper);
        playerConnection.sendPacket(packetPlayOutEntityHeadRotation);

    }

    public void sendShowStatus(Player player, NPCPosition position) {
        if (position == NPCPosition.CORPSE) {
            sendCorpse(player);
        }
        if (position == NPCPosition.SIT) {
            sendSitPacket(player);
        }
    }

    public void sendCorpse(Player player) {
        PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;
        PacketPlayOutBed bedWrapper = new PacketPlayOutEntityBedWrapper().create(entityId, location);
        PacketPlayOutEntity.PacketPlayOutRelEntityMove move = new PacketPlayOutEntityMoveWrapper().create(entityId);
        Location bed = location.clone();
        bed.setY(0);
        player.sendBlockChange(bed,
                org.bukkit.Material.BED_BLOCK, (byte) yawToFacing(location.getYaw()));

        playerConnection.sendPacket(bedWrapper);
        playerConnection.sendPacket(move);
    }

    public void sendSitPacket(Player player) {
        PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;
        hologramMounted.show(player);
        PacketPlayOutAttachEntity sit = new PacketPlayOutEntitySit().create(entityId, hologramMounted.getId());
        sendLookAtArmorStand(player);
        playerConnection.sendPacket(sit);
    }

    @Override
    public void setStatus(NPCPosition position) {
        if (position == NPCPosition.CORPSE) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                removeMounted(player);
                removeCorpse(player);
                hologram.hide(player);
            }
            hologram.clearExceptText();
            hologram.setStart(location.clone().subtract(0,
                    getPositionMemory() == NPCPosition.NORMAL ? -0.5 : (
                            getPositionMemory() == NPCPosition.CORPSE ? 1 : 0.5), 0));
            hologram.createPackets();
            for (Player player : Bukkit.getOnlinePlayers()) {
                hologram.showOrUpdate(player, ServerManager.translateVar(text, this));
                sendCorpse(player);
            }
        }
        if (position == NPCPosition.NORMAL) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                removeMounted(player);
                removeCorpse(player);
                hologram.hide(player);
            }
            hologram.clearExceptText();
            hologram.setStart(location.clone().add(0,
                    getPositionMemory() == NPCPosition.NORMAL ? -0.5 : (
                            getPositionMemory() == NPCPosition.CORPSE ? 1 : 0.5), 0));
            hologram.createPackets();
            for (Player player : Bukkit.getOnlinePlayers()) {
                sendShowPackets(player); // este ya contiene el showOrUpdate
            }
        }
        if (position == NPCPosition.SIT) {
            hologramMounted = new Hologram(MinecraftVersion.getNMSVersion(), location.clone().subtract(0,
                    2.1, 0));
            for (Player player : Bukkit.getOnlinePlayers()) {
                removeMounted(player);
                removeCorpse(player);
                hologram.hide(player);
            }
            hologram.clearExceptText();
            hologram.setStart(location.clone().subtract(0,
                    getPositionMemory() == NPCPosition.NORMAL ? -0.5 : (
                            getPositionMemory() == NPCPosition.CORPSE ? 1 : 0.5), 0));
            hologram.createPackets();
            for (Player player : Bukkit.getOnlinePlayers()) {
                hologram.showOrUpdate(player, ServerManager.translateVar(text, this));
                sendSitPacket(player);
            }
        }
    }

    @Override
    public void setStatus(Player player, NPCPosition position) {
        if (position == NPCPosition.CORPSE) {
            removeMounted(player);
            removeCorpse(player);
            hologram.hide(player);
            hologram.clearExceptText();
            hologram.setStart(location.clone().subtract(0,
                    getPositionMemory() == NPCPosition.NORMAL ? -0.5 : (
                            getPositionMemory() == NPCPosition.CORPSE ? 1 : 0.5), 0));
            hologram.createPackets();
            hologram.showOrUpdate(player, ServerManager.translateVar(text, this));
            sendCorpse(player);
        }
        if (position == NPCPosition.NORMAL) {
            removeCorpse(player);
            removeMounted(player);
            sendHidePackets(player);
            hologram.clearExceptText();
            hologram.setStart(location.clone().subtract(0,
                    getPositionMemory() == NPCPosition.NORMAL ? -0.5 : (
                            getPositionMemory() == NPCPosition.CORPSE ? 1 : 0.5), 0));
            hologram.createPackets();
            sendShowPackets(player);
        }
        if (position == NPCPosition.SIT) {
            hologramMounted = new Hologram(MinecraftVersion.getNMSVersion(), location.clone().subtract(0,
                    2.1, 0));
            removeMounted(player);
            hologram.hide(player);
            hologram.clearExceptText();
            hologram.setStart(location.clone().subtract(0,
                    getPositionMemory() == NPCPosition.NORMAL ? -0.5 : (
                            getPositionMemory() == NPCPosition.CORPSE ? 1 : 0.5), 0));
            hologram.createPackets();
            sendSitPacket(player);
        }
    }

    public void removeCorpse(Player player) {
        Location bed = location.clone();
        Block block = bed.getBlock();
        bed.setY(0);
        player.sendBlockChange(block.getLocation(),
                block.getType(), block.getData());

    }

    public void removeMounted(Player p) {
        if (hologramMounted != null) {
            hologramMounted.hideOnlyHologram(p);
        }
    }

    private int yawToFacing(float yaw) {
        int facing = North;
        if (yaw >= -45 && yaw <= 45) {
            facing = South;
        } else if (yaw >= 45 && yaw <= 135) {
            facing = West;
        } else if (yaw <= -45 && yaw >= -135) {
            facing = East;
        } else if (yaw <= -225 && yaw >= -315) {
            facing = West;
        } else if (yaw >= 225 && yaw <= 315) {
            facing = East;
        } else if (yaw >= 315) {
            facing = South;
        } else if (yaw <= -315) {
            facing = South;
        }
        return facing;
    }

    @Override
    public void updateSkin() {
        PacketPlayOutPlayerInfoWrapper packetPlayOutPlayerInfoWrapper = new PacketPlayOutPlayerInfoWrapper();
        this.packetPlayOutPlayerInfoAdd = packetPlayOutPlayerInfoWrapper
                .create(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, gameProfile, name);
        this.packetPlayOutPlayerInfoRemove = packetPlayOutPlayerInfoWrapper
                .create(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, gameProfile, name);
    }
}
