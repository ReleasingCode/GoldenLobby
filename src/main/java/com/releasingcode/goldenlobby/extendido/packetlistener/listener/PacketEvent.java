package com.releasingcode.goldenlobby.extendido.packetlistener.listener;

import io.netty.channel.Channel;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

public abstract class PacketEvent implements Cancellable {
    public final Player player;
    public final Channel channel;
    public final Object handle;
    private boolean c;

    public PacketEvent(Object player, Channel channel, Object handle) {
        if (player instanceof Player) {
            this.player = (Player) player;
        } else {
            this.player = null;
        }
        this.channel = channel;
        this.handle = handle;
        c = false;
    }

    public Player getPlayer() {
        return player;
    }

    public boolean hasPlayer() {
        return player != null;
    }


    public Channel getChannel() {
        return channel;
    }

    public Object getPacket() {
        return handle;
    }

    @Override
    public boolean isCancelled() {
        return c;
    }

    @Override
    public void setCancelled(boolean cancel) {
        c = cancel;
    }

    abstract public PacketHandlerList getPacketHandlerList();
}
