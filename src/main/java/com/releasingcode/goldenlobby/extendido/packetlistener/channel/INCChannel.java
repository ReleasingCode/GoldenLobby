package com.releasingcode.goldenlobby.extendido.packetlistener.channel;

import com.releasingcode.goldenlobby.extendido.packetlistener.Cancellable;
import com.releasingcode.goldenlobby.extendido.packetlistener.IPacketListener;
import com.releasingcode.goldenlobby.extendido.packetlistener.listener.PacketEvent;
import com.releasingcode.goldenlobby.extendido.packetlistener.listener.PacketHandlerList;
import com.releasingcode.goldenlobby.extendido.packetlistener.listener.RegisteredPacket;
import com.releasingcode.goldenlobby.extendido.reflection.minecraft.Minecraft;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.net.SocketAddress;
import java.util.ArrayList;

public class INCChannel extends ChannelAbstract {

    private static final Field channelField = networkManagerFieldResolver.resolveByFirstTypeSilent(io.netty.channel.Channel.class);

    public INCChannel(IPacketListener iPacketListener) {
        super(iPacketListener);
    }

    @Override
    public void addChannel(final Player player) {
        try {
            final io.netty.channel.Channel channel = getChannel(player);
            addChannelExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        channel.pipeline().addBefore(KEY_HANDLER, KEY_PLAYER, new ChannelHandler(player));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to add channel for " + player, e);
        }
    }

    @Override
    public void removeChannel(Player player) {
        try {
            final io.netty.channel.Channel channel = getChannel(player);
            removeChannelExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (channel.pipeline().get(KEY_PLAYER) != null) {
                            channel.pipeline().remove(KEY_PLAYER);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to remove channel for " + player, e);
        }
    }

    io.netty.channel.Channel getChannel(Player player) throws ReflectiveOperationException {
        final Object handle = Minecraft.getHandle(player);
        final Object connection = playerConnection.get(handle);
        return (io.netty.channel.Channel) channelField.get(networkManager.get(connection));
    }

    @Override
    public IListenerList<?> newListenerList() {
        return new ListenerList<>();
    }

    class ListenerList<E> extends ArrayList<E> implements IListenerList<E> {

        @Override
        public boolean add(E paramE) {
            try {
                final E a = paramE;
                addChannelExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            io.netty.channel.Channel channel = null;
                            while (channel == null) {
                                channel = (io.netty.channel.Channel) channelField.get(a);
                            }
                            if (channel.pipeline().get(KEY_SERVER) == null) {
                                channel.pipeline().addBefore(KEY_HANDLER, KEY_SERVER, new ChannelHandler(new INCChannelWrapper(channel)));
                            }
                        } catch (Exception e) {
                        }
                    }
                });
            } catch (Exception e) {
            }
            return super.add(paramE);
        }

        @Override
        public boolean remove(Object arg0) {
            try {
                final Object a = arg0;
                removeChannelExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            io.netty.channel.Channel channel = null;
                            while (channel == null) {
                                channel = (io.netty.channel.Channel) channelField.get(a);
                            }
                            channel.pipeline().remove(KEY_SERVER);
                        } catch (Exception e) {
                        }
                    }
                });
            } catch (Exception e) {
            }
            return super.remove(arg0);
        }
    }

    class ChannelHandler extends ChannelDuplexHandler implements IChannelHandler {

        private final Object owner;

        public ChannelHandler(Player player) {
            this.owner = player;
        }

        public ChannelHandler(ChannelWrapper<?> channelWrapper) {
            this.owner = channelWrapper;
        }

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            Cancellable cancellable = new Cancellable();
            Object pckt = msg;
            if (Packet.isAssignableFrom(msg.getClass())) {
                for (RegisteredPacket listener : PacketHandlerList.getAllRegisteredPacketListeners()) {
                    if (!listener.getType().name().equals(msg.getClass().getSimpleName())) continue;
                    if (!(owner instanceof Player)) {
                        continue;
                    }
                    PacketEvent packet = listener.getAccessor().newInstance(0, owner, ctx.channel(), msg);
                    try {
                        listener.callEvent(packet);
                    } catch (Exception e) {
                        System.out.println("No se ha podido cambiar el paquete: " + packet.getPacket().getClass().getName() + " Error: " + e.getMessage());
                        e.printStackTrace();
                        return;
                    }
                    if (packet.isCancelled())
                        return;
                }
                pckt = onPacketSend(this.owner, msg, cancellable);
            }
            if (cancellable.isCancelled()) {
                return;
            }
            super.write(ctx, pckt, promise);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            Cancellable cancellable = new Cancellable();
            Object pckt = msg;
            if (Packet.isAssignableFrom(msg.getClass())) {
                for (RegisteredPacket listener : PacketHandlerList.getAllRegisteredPacketListeners()) {
                    if (!listener.getType().name().equals(msg.getClass().getSimpleName())) continue;
                    if (!(owner instanceof Player)) {
                        continue;
                    }
                    PacketEvent packet = listener.getAccessor().newInstance(0, owner, ctx.channel(), msg);
                    try {
                        listener.callEvent(packet);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                    if (packet.isCancelled())
                        return;
                }
                pckt = onPacketReceive(this.owner, msg, cancellable);
            }
            if (cancellable.isCancelled()) {
                return;
            }
            super.channelRead(ctx, pckt);
        }

        @Override
        public String toString() {
            return "INCChannel$ChannelHandler@" + hashCode() + " (" + this.owner + ")";
        }

    }

    static class INCChannelWrapper extends ChannelWrapper<io.netty.channel.Channel> implements IChannelWrapper {

        public INCChannelWrapper(io.netty.channel.Channel channel) {
            super(channel);
        }

        @Override
        public SocketAddress getRemoteAddress() {
            return this.channel().remoteAddress();
        }

        @Override
        public SocketAddress getLocalAddress() {
            return this.channel().localAddress();
        }
    }

}