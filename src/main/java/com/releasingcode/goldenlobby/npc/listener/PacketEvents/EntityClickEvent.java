package com.releasingcode.goldenlobby.npc.listener.PacketEvents;


import es.minecub.core.apis.classaccess.reflection.ClassAccess;
import es.minecub.core.apis.packetlistener.listener.PacketEvent;
import es.minecub.core.apis.packetlistener.listener.PacketHandlerList;
import es.minecub.core.apis.packetlistener.listener.PacketID;
import es.minecub.core.apis.packetlistener.listener.PacketType;
import io.netty.channel.Channel;
import org.bukkit.entity.Player;

@PacketID(id = PacketType.PacketPlayInUseEntity)
public class EntityClickEvent extends PacketEvent {
    private final static PacketHandlerList handler = new PacketHandlerList();
    private final static ClassAccess access = new ClassAccess("{nms}.PacketPlayInUseEntity");

    public EntityClickEvent(Player player, Channel channel, Object handle) {
        super(player, channel, handle);
    }

    public int getID() {
        return access.get(getPacket(), 0);
    }

    public String getAction() {
        return access.get(getPacket(), 1).toString();
    }

    @Override
    public PacketHandlerList getPacketHandlerList() {
        return handler;
    }
}
