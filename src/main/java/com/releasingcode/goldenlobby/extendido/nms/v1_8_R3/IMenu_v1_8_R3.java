package com.releasingcode.goldenlobby.extendido.nms.v1_8_R3;

import com.releasingcode.goldenlobby.extendido.nms.IMenu;
import com.releasingcode.goldenlobby.modulos.inventarios.builder.ItemMenuHolder;
import com.releasingcode.goldenlobby.modulos.inventarios.builder.MenuItem;
import io.netty.handler.codec.DecoderException;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public class IMenu_v1_8_R3 implements IMenu {

    @Override
    public void setTextInventory(Player player, String title) {
        EntityPlayer ep = ((CraftPlayer) player).getHandle();
        title = ChatColor.translateAlternateColorCodes('&', title);
        title = title.length() > 32 ? title.substring(0, 31) : title;
        PacketPlayOutOpenWindow packet = new PacketPlayOutOpenWindow(ep.activeContainer.windowId
                , "minecraft:container", new ChatMessage(title)
                , player.getOpenInventory().getTopInventory().getSize());
        try {
            ep.playerConnection.sendPacket(packet);
            ep.updateInventory(ep.activeContainer);
        } catch (DecoderException ignored) {
        }
    }

    public List<net.minecraft.server.v1_8_R3.ItemStack> toListStack(MenuItem[] original) {
        List<net.minecraft.server.v1_8_R3.ItemStack> items = new ArrayList<>();
        int i = 0;
        while (i < original.length) {
            if (original[i] != null) {
                items.add(CraftItemStack.asNMSCopy(original[i].getIcon()));
                ++i;
                continue;
            }
            items.add(CraftItemStack.asNMSCopy(new ItemStack(Material.AIR, 1)));
            ++i;
        }
        return items;
    }

    @Override
    public void setItemContents(Player player, MenuItem[] contents) {
        InventoryView view = player.getOpenInventory();
        if (view != null && view.getTopInventory() != null && view.getTopInventory().getHolder() instanceof ItemMenuHolder) {
            PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;
            try {
                Class<?> classED = Class.forName("net.minecraft.server.v1_8_R3.PacketPlayOutWindowItems");
                if (classED.getConstructors().length > 0) {
                    Constructor<?> a = classED.getConstructors()[1];
                    PacketPlayOutWindowItems window;
                    if (a.getParameterTypes().length > 0) {
                        if (a.getParameterTypes()[0] == int.class) { //Spigot Oficial
                            window = (PacketPlayOutWindowItems) classED.getConstructors()[1].newInstance(playerConnection.player.activeContainer.windowId
                                    , toListStack(contents));
                        } else {//compatibilidad con ImanitySpigot
                            window = (PacketPlayOutWindowItems) classED.getConstructors()[1].newInstance(playerConnection.player.activeContainer
                                    , toListStack(contents));
                        }
                        playerConnection.sendPacket(window);
                    }
                }
                //PacketPlayOutWindowItems packet = new PacketPlayOutWindowItems(playerConnection.player.activeContainer.windowId
                //           , toListStack(contents));
                //playerConnection.sendPacket(packet);
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
