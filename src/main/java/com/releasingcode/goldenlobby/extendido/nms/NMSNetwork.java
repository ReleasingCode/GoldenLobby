package com.releasingcode.goldenlobby.extendido.nms;


import com.releasingcode.goldenlobby.exception.IncompatibleMinecraftVersionException;
import es.minecub.core.apis.reflection.Reflection;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public final class NMSNetwork {
    private final static Class<?> craftPlayerClass;
    private final static Class<?> entityPlayerClass;
    private final static Class<?> packetClass;
    private final static Method sendPacketMethod;

    static {
        try {
            craftPlayerClass = Reflection.getBukkitClassByName("entity.CraftPlayer");
            entityPlayerClass = Reflection.getMinecraftClassByName("EntityPlayer");

            packetClass = Reflection.getMinecraftClassByName("Packet");
            sendPacketMethod = Reflection.getMinecraftClassByName("PlayerConnection").getDeclaredMethod("sendPacket", packetClass);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            throw new IncompatibleMinecraftVersionException("Cannot load classes needed to send network packets", e);
        }
    }

    private NMSNetwork() {
    }


    static public Object getPlayerHandle(Player player) throws InvocationTargetException {
        try {
            Object craftPlayer = craftPlayerClass.cast(player);
            return Reflection.call(craftPlayer, "getHandle");
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new IncompatibleMinecraftVersionException("Cannot retrieve standard Bukkit or NBS object while getting a player's handle, is the current Bukkit/Minecraft version supported by this API?", e);
        }
    }

    public static Class<?> getNMSClass(String string) {
        String string2 = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        try {
            return Class.forName("net.minecraft.server." + string2 + "." + string);
        } catch (ClassNotFoundException classNotFoundException) {
            return null;
        }
    }

    public static Class<?> getOBCClass(String string) {
        String string2 = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        try {
            return Class.forName("org.bukkit.craftbukkit." + string2 + "." + string);
        } catch (ClassNotFoundException classNotFoundException) {
            classNotFoundException.printStackTrace();
            return null;
        }
    }

    static public Object getPlayerConnection(Object playerHandle) throws InvocationTargetException {
        try {
            if (!entityPlayerClass.isAssignableFrom(playerHandle.getClass()))
                throw new ClassCastException("Cannot retrieve a player connection from another class that net.minecraft.server.<version>.EntityPlayer (got " + playerHandle.getClass().getName() + ").");

            return Reflection.getFieldValue(playerHandle, "playerConnection");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IncompatibleMinecraftVersionException("Cannot retrieve standard Bukkit or NBS object while getting a player's connection, is the current Bukkit/Minecraft version supported by this API?", e);
        }
    }


    static public Object getPlayerConnection(Player player) throws InvocationTargetException {
        return getPlayerConnection(getPlayerHandle(player));
    }


    static public void sendPacket(Object playerConnection, Object packet) throws InvocationTargetException {
        try {
            if (!packetClass.isAssignableFrom(packet.getClass()))
                throw new ClassCastException("Cannot send a packet object if the object is not a subclass of net.minecraft.server.<version>.Packet (got " + packet.getClass().getName() + ").");

            sendPacketMethod.invoke(playerConnection, packet);
        } catch (IllegalAccessException e) {
            throw new IncompatibleMinecraftVersionException("Cannot retrieve standard Bukkit or NBS object while sending a packet to a player, is the current Bukkit/Minecraft version supported by this API?", e);
        }
    }


    static public void sendPacket(Player player, Object packet) throws InvocationTargetException {
        sendPacket(getPlayerConnection(player), packet);
    }
}
