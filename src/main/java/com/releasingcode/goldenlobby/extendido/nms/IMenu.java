package com.releasingcode.goldenlobby.extendido.nms;

import org.bukkit.entity.Player;
import com.releasingcode.goldenlobby.modulos.inventarios.builder.MenuItem;

public interface IMenu {
    void setTextInventory(Player player, String title);

    void setItemContents(Player player, MenuItem[] contents);
}
