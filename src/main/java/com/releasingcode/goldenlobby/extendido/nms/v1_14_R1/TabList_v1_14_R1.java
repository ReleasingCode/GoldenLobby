package com.releasingcode.goldenlobby.extendido.nms.v1_14_R1;

import com.releasingcode.goldenlobby.extendido.nms.ITablList;
import com.releasingcode.goldenlobby.extendido.reflection.Reflection;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.server.v1_14_R1.IChatBaseComponent;
import net.minecraft.server.v1_14_R1.PacketPlayOutPlayerListHeaderFooter;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class TabList_v1_14_R1 implements ITablList {
    @Override
    public void tabList(Player p, String header, String fooder) {

        PacketPlayOutPlayerListHeaderFooter packet = new PacketPlayOutPlayerListHeaderFooter();
        BaseComponent[] componentHeader = TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', header));
        BaseComponent[] componentFooter = TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', fooder));
        Object headerSerializer = IChatBaseComponent.ChatSerializer.a(ComponentSerializer.toString(componentHeader));
        Object footerSerializer = IChatBaseComponent.ChatSerializer.a(ComponentSerializer.toString(componentFooter));
        try {
            Reflection.getField(packet.getClass(), "a").set(packet, headerSerializer);
            Reflection.getField(packet.getClass(), "b").set(packet, footerSerializer);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);

    }
}
