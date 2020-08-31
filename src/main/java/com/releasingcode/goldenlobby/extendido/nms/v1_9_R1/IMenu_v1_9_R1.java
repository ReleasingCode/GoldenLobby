package com.releasingcode.goldenlobby.extendido.nms.v1_9_R1;

import com.releasingcode.goldenlobby.extendido.nms.IMenu;
import com.releasingcode.goldenlobby.modulos.inventarios.builder.MenuItem;
import net.minecraft.server.v1_9_R1.ChatMessage;
import net.minecraft.server.v1_9_R1.EntityPlayer;
import net.minecraft.server.v1_9_R1.PacketPlayOutOpenWindow;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class IMenu_v1_9_R1 implements IMenu {
    @Override
    public void setTextInventory(Player player, String title) {
        EntityPlayer ep = ((CraftPlayer) player).getHandle();
        PacketPlayOutOpenWindow packet =
                new PacketPlayOutOpenWindow(ep.activeContainer.windowId, "minecraft:container",
                                            new ChatMessage(ChatColor.translateAlternateColorCodes('&', title)),
                                            player.getOpenInventory().getTopInventory().getSize());
        ep.playerConnection.sendPacket(packet);
        ep.updateInventory(ep.activeContainer);
    }

    @Override
    public void setItemContents(Player player, MenuItem[] contents) {

    }
}
