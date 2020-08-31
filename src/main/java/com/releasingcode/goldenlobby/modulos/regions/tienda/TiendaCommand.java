package com.releasingcode.goldenlobby.modulos.regions.tienda;

import com.releasingcode.goldenlobby.BaseCommand;
import com.releasingcode.goldenlobby.modulos.regions.RegionPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TiendaCommand extends BaseCommand {
    private final RegionPlugin plugin;

    public TiendaCommand(RegionPlugin plugin) {
        super(plugin.getShopItemsParser().getCommand(), "/" + plugin.getShopItemsParser().getCommand(), "Abrir invenatio de la tienda de armamento.");
        this.plugin = plugin;
    }

    @Override public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return false;
        plugin.getShopInventory().openInventory(((Player) sender));
        return false;
    }

}
