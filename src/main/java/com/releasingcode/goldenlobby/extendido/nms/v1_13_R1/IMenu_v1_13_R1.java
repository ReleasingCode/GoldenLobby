package com.releasingcode.goldenlobby.extendido.nms.v1_13_R1;

import com.releasingcode.goldenlobby.extendido.nms.IMenu;
import com.releasingcode.goldenlobby.modulos.inventarios.builder.MenuItem;
import net.minecraft.server.v1_13_R1.EntityPlayer;
import net.minecraft.server.v1_13_R1.IChatBaseComponent;
import net.minecraft.server.v1_13_R1.PacketPlayOutOpenWindow;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_13_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class IMenu_v1_13_R1 implements IMenu {
    @Override
    public void setTextInventory(Player player, String title) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        PacketPlayOutOpenWindow packet = new PacketPlayOutOpenWindow(entityPlayer.activeContainer.windowId,
                                                                     "minecraft:container"
                , IChatBaseComponent.ChatSerializer.a("{\"text\": \""
                                                              + ChatColor.translateAlternateColorCodes('&', title + "\"}")),
                                                                     player.getOpenInventory().getTopInventory().getSize());
        entityPlayer.playerConnection.sendPacket(packet);
        entityPlayer.updateInventory(entityPlayer.activeContainer);
    }

    @Override
    public void setItemContents(Player player, MenuItem[] contents) {

    }
}
