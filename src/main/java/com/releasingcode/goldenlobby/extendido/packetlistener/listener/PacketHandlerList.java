package com.releasingcode.goldenlobby.extendido.packetlistener.listener;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class PacketHandlerList {
    private static final ArrayList<PacketHandlerList> allLists = Lists.newArrayList();
    private final EnumMap<PacketPriority, ArrayList<RegisteredPacket>> handlerslots = Maps
            .newEnumMap(PacketPriority.class);
    private volatile RegisteredPacket[] handlers = null;

    public PacketHandlerList() {
        PacketPriority[] priority = PacketPriority.values();
        for (PacketPriority packetPriority : priority) this.handlerslots.put(packetPriority, new ArrayList<>());
        synchronized (allLists) {
            allLists.add(this);
        }
    }

    protected static void unregisterAll() {
        synchronized (allLists) {
            for (PacketHandlerList handler : allLists) {
                Collection<ArrayList<RegisteredPacket>> list = handler.handlerslots.values();
                for (ArrayList<RegisteredPacket> packet : list)
                    packet.clear();
                handler.handlers = null;
            }
        }
    }

    public static void unregisterAll(Plugin plugin) {
        synchronized (allLists) {
            for (PacketHandlerList packet : allLists)
                packet.unregister(plugin);
        }
    }


    public synchronized void register(RegisteredPacket listener) {
        if (handlerslots.get(listener.getPriority()).contains(listener))
            return;
        handlers = null;
        handlerslots.get(listener.getPriority()).add(listener);
    }

    public void registerAll(Collection<RegisteredPacket> listeners) {
        listeners.forEach(this::register);
    }

    public synchronized void unregister(Plugin plugin) {
        boolean changed = false;
        for (List<RegisteredPacket> priority : handlerslots.values()) {
            ListIterator<RegisteredPacket> i = priority.listIterator();
            while (i.hasNext()) {
                if (i.next().getPlugin().equals(plugin)) {
                    i.remove();
                    changed = true;
                }
            }
        }
        if (changed) this.handlers = null;
    }

    public RegisteredPacket[] getRegisteredPacketListeners() {
        RegisteredPacket[] handlers = this.handlers;
        if (this.handlers != null) return handlers;

        ArrayList<RegisteredPacket> entries = Lists.newArrayList();
        this.handlerslots.forEach((key, value) -> entries.addAll(value));
        return this.handlers = entries.toArray(new RegisteredPacket[0]);
    }

    public static RegisteredPacket[] getAllRegisteredPacketListeners() {
        int size = allLists.size();
        RegisteredPacket[] listeners = new RegisteredPacket[size];
        synchronized (allLists) {
            int i = 0;
            for (PacketHandlerList handler : allLists)
                for (RegisteredPacket packet : handler.getRegisteredPacketListeners())
                    listeners[i++] = packet;
        }
        return listeners;
    }
}
