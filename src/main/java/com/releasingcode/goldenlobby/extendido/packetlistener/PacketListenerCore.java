package com.releasingcode.goldenlobby.extendido.packetlistener;

import com.google.common.collect.Maps;
import com.releasingcode.goldenlobby.Utils;
import com.releasingcode.goldenlobby.extendido.classaccess.reflection.ClassAccess;
import com.releasingcode.goldenlobby.extendido.packetlistener.channel.ChannelWrapper;
import com.releasingcode.goldenlobby.extendido.packetlistener.handler.PacketHandler;
import com.releasingcode.goldenlobby.extendido.packetlistener.handler.ReceivedPacket;
import com.releasingcode.goldenlobby.extendido.packetlistener.handler.SentPacket;
import com.releasingcode.goldenlobby.extendido.packetlistener.listener.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PacketListenerCore implements IPacketListener, Listener {

    private ChannelInjector channelInjector;
    public boolean injected = false;

    public void load() {
        channelInjector = new ChannelInjector();
        if (injected = channelInjector.inject(this)) {
            channelInjector.addServerChannel();
            Utils.log("Injected custom channel handlers.");
        } else {
            Utils.log("Failed to inject channel handlers");
        }

    }


    public void init(Plugin plugin) {
        //Register our events
        //APIManager.registerEvents(this, this);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        Utils.log("Adding channels for online players...");
        for (Player player : Bukkit.getOnlinePlayers()) {
            channelInjector.addChannel(player);
        }
    }

    //This gets called either by #disableAPI above or #disableAPI in one of the requiring plugins
    public void disable(Plugin plugin) {
        if (!injected) {
            return;//Not enabled
        }
        Utils.log("Removing channels for online players...");
        for (Player player : Bukkit.getOnlinePlayers()) {
            channelInjector.removeChannel(player);
        }

        Utils.log("Removing packet handlers (" + PacketHandler.getHandlers().size() + ")...");
        while (!PacketHandler.getHandlers().isEmpty()) {
            PacketHandler.removeHandler(PacketHandler.getHandlers().get(0));
        }
    }

    /**
     * @param handler PacketHandler to add
     * @return <code>true</code> if the handler was added
     * @see PacketHandler#addHandler(PacketHandler)
     */
    public static boolean addPacketHandler(PacketHandler handler) {
        return PacketHandler.addHandler(handler);
    }

    /**
     * @param handler PacketHandler to remove
     * @return <code>true</code> if the handler was removed
     * @see PacketHandler#removeHandler(PacketHandler)
     */
    public static boolean removePacketHandler(PacketHandler handler) {
        return PacketHandler.removeHandler(handler);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e) {
        channelInjector.addChannel(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent e) {
        channelInjector.removeChannel(e.getPlayer());
    }

    @Override
    public Object onPacketReceive(Object sender, Object packet, Cancellable cancellable) {
        ReceivedPacket receivedPacket;
        if (sender instanceof Player) {
            receivedPacket = new ReceivedPacket(packet, cancellable, (Player) sender);
        } else {
            receivedPacket = new ReceivedPacket(packet, cancellable, (ChannelWrapper<?>) sender);
        }
        PacketHandler.notifyHandlers(receivedPacket);
        if (receivedPacket.getPacket() != null) {
            return receivedPacket.getPacket();
        }
        return packet;
    }

    @Override
    public Object onPacketSend(Object receiver, Object packet, Cancellable cancellable) {
        SentPacket sentPacket;
        if (receiver instanceof Player) {
            sentPacket = new SentPacket(packet, cancellable, (Player) receiver);
        } else {
            sentPacket = new SentPacket(packet, cancellable, (ChannelWrapper<?>) receiver);
        }
        PacketHandler.notifyHandlers(sentPacket);
        if (sentPacket.getPacket() != null) {
            return sentPacket.getPacket();
        }
        return packet;
    }

    static Map<Class<?>, Set<RegisteredPacket>> registered(PacketListener a, Plugin b) {
        Map<Class<?>, Set<RegisteredPacket>> c = Maps.newHashMap();

        for (Method d : a.getClass().getDeclaredMethods()) {
            com.releasingcode.goldenlobby.extendido.packetlistener.listener.PacketHandler e = d.getAnnotation(com.releasingcode.goldenlobby.extendido.packetlistener.listener.PacketHandler.class);
            if (e == null
                    || d.getParameterTypes().length != 1
                    || !PacketEvent.class.isAssignableFrom(d
                    .getParameterTypes()[0]))
                continue;
            Class<?> f = d.getParameterTypes()[0].asSubclass(PacketEvent.class);
            PacketID g = f.getAnnotation(PacketID.class);
            if (g == null)
                continue;
            d.setAccessible(true);
            if (!c.containsKey(f))
                c.put(f, new HashSet<>());

            PacketExecutor h = (a1, b1) -> {
                try {
                    d.invoke(a1, b1);
                } catch (IllegalAccessException | InvocationTargetException e1) {
                    throw new RuntimeException(e1);
                } catch (IllegalArgumentException e1) {
                    throw new IllegalArgumentException(e1);
                }
            };
            c.get(f).add(
                    new RegisteredPacket(g.id(), a, h, e.priority(), b, e
                            .ignoreCancelled(), new ClassAccess(f)));
        }
        return c;
    }

    public static void registerPackets(PacketListener listener, Plugin plugin) {
        if (!plugin.isEnabled())
            return;
        for (Map.Entry<Class<?>, Set<RegisteredPacket>> c : registered(listener, plugin).entrySet()) {
            PacketHandlerList d = new ClassAccess(c.getKey())
                    .get(null, PacketHandlerList.class, 0);
            if (d == null) continue;
            d.registerAll(c.getValue());
        }
    }
}
