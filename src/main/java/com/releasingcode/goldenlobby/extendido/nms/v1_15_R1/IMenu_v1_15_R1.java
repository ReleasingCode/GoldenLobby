package com.releasingcode.goldenlobby.extendido.nms.v1_15_R1;

import com.releasingcode.goldenlobby.extendido.nms.IMenu;
import com.releasingcode.goldenlobby.modulos.inventarios.builder.MenuItem;
import net.minecraft.server.v1_15_R1.Containers;
import net.minecraft.server.v1_15_R1.EntityPlayer;
import net.minecraft.server.v1_15_R1.IChatBaseComponent;
import net.minecraft.server.v1_15_R1.PacketPlayOutOpenWindow;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class IMenu_v1_15_R1 implements IMenu {
    @Override
    public void setTextInventory(Player player, String title) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        PacketPlayOutOpenWindow packet = new PacketPlayOutOpenWindow(entityPlayer.activeContainer.windowId,
                                                                     WindowType_1_15_R1.guessBySlots(entityPlayer.activeContainer.getBukkitView().getTopInventory().getSize()).getType()
                , IChatBaseComponent.ChatSerializer.a("{\"text\": \""
                                                              + ChatColor.translateAlternateColorCodes('&', title + "\"}")));
        entityPlayer.playerConnection.sendPacket(packet);
        entityPlayer.updateInventory(entityPlayer.activeContainer);
    }

    @Override
    public void setItemContents(Player player, MenuItem[] contents) {

    }

    public enum WindowType_1_15_R1 {
        GENERIC_9_1(Containers.GENERIC_9X1),
        GENERIC_9_2(Containers.GENERIC_9X2),
        GENERIC_9_3(Containers.GENERIC_9X3),
        GENERIC_9_4(Containers.GENERIC_9X4),
        GENERIC_9_5(Containers.GENERIC_9X5),
        GENERIC_9_6(Containers.GENERIC_9X6);

        private final Containers<?> type;

        WindowType_1_15_R1(Containers<?> type) {
            this.type = type;
        }

        public static WindowType_1_15_R1 guessBySlots(int slots) {
            if (slots % 9 == 0) {
                switch (slots / 9) {
                    case 1:
                        return GENERIC_9_1;
                    case 2:
                        return GENERIC_9_2;
                    case 3:
                        return GENERIC_9_3;
                    case 4:
                        return GENERIC_9_4;
                    case 5:
                        return GENERIC_9_5;
                    case 6:
                        return GENERIC_9_6;
                }
            }
            return GENERIC_9_3;
        }

        public Containers<?> getType() {
            return type;
        }
    }
}
