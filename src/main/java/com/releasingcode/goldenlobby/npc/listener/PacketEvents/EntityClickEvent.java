package com.releasingcode.goldenlobby.npc.listener.PacketEvents;


import com.releasingcode.goldenlobby.extendido.classaccess.reflection.ClassAccess;
import com.releasingcode.goldenlobby.extendido.packetlistener.listener.PacketEvent;
import com.releasingcode.goldenlobby.extendido.packetlistener.listener.PacketHandlerList;
import com.releasingcode.goldenlobby.extendido.packetlistener.listener.PacketID;
import com.releasingcode.goldenlobby.extendido.packetlistener.listener.PacketType;
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
