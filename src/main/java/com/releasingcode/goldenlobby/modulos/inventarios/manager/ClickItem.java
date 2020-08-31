package com.releasingcode.goldenlobby.modulos.inventarios.manager;


import com.releasingcode.goldenlobby.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import com.releasingcode.goldenlobby.modulos.inventarios.builder.ItemClickEvent;
import com.releasingcode.goldenlobby.modulos.inventarios.builder.MenuItem;

public class ClickItem extends MenuItem {
    private final ItemSlot itemSlot;
    private ItemStack stack;

    public ClickItem(ItemSlot itemSlot, ItemStack stack) {
        super(stack);
        this.stack = stack;
        this.itemSlot = itemSlot;
    }

    @Override
    public ItemStack getIcon() {
        return stack;
    }

    @Override
    public void setIcon(ItemStack newIcon) {
        this.stack = newIcon;
    }

    @Override
    public void onItemClick(ItemClickEvent event) {
        Player player = event.getPlayer();
        if (event.getClickedItem().getType() == Material.AIR) {
            return;
        }
        for (String comandos : this.itemSlot.getCommands()) {
            if (comandos != null) {
                Utils.evaluateCommand(comandos, player);
            }
        }
    }
}
