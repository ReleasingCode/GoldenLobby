package com.releasingcode.goldenlobby.modulos.spawn;

import com.releasingcode.goldenlobby.BaseCommand;
import com.releasingcode.goldenlobby.managers.SpawnPoint;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.releasingcode.goldenlobby.modulos.setspawn.SpawnPointPlugin;

public class SpawnCommand extends BaseCommand {

    public SpawnCommand() {
        super("spawn", "/spawn", "Volver al punto de spawn.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return false;

        Player player = (Player) sender;
        SpawnPoint location = SpawnPointPlugin.SPAWNPOINT;

        if (location == null) {
            player.sendMessage(ChatColor.RED + "Ocurrió un error al ejecutar este comando. [ERRx05]");
            return false;
        }

        player.teleport(location.getLocation());
        return true;
    }

}
