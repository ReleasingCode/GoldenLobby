package com.releasingcode.goldenlobby.modulos.inventarios.comandos;

import com.releasingcode.goldenlobby.BaseCommand;
import com.releasingcode.goldenlobby.modulos.inventarios.manager.Inventario;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InventarioCommand extends BaseCommand {
    private final Inventario inventario;

    public InventarioCommand(Inventario inventario) {
        super(inventario.getComando());
        this.inventario = inventario;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = ((Player) sender).getPlayer();
            inventario.openInventory(p);
            return true;
        }
        return false;
    }
}
