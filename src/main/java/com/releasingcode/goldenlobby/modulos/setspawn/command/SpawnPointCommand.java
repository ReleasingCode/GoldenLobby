package com.releasingcode.goldenlobby.modulos.setspawn.command;

import com.releasingcode.goldenlobby.BaseCommand;
import com.releasingcode.goldenlobby.languages.Lang;
import com.releasingcode.goldenlobby.managers.LobbyPlayer;
import com.releasingcode.goldenlobby.managers.LobbyPlayerMap;
import com.releasingcode.goldenlobby.managers.SpawnPoint;
import com.releasingcode.goldenlobby.modulos.setspawn.SpawnPointPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnPointCommand extends BaseCommand {
    private final SpawnPointPlugin plugin;

    public SpawnPointCommand(SpawnPointPlugin plugin) {
        super("mcsetspawn", "/mcsetspawn", "Set point of appearance");
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = ((Player) sender).getPlayer();
            if (player.hasPermission("goldenlobby.spawnpoint.admin")) {
                LobbyPlayer lobbyPlayer = LobbyPlayerMap.getJugador(player);
                lobbyPlayer.sendMessage(Lang.YOU_HAVE_STABLISHED_POINT.toString());
                this.plugin.saveLocation(new SpawnPoint(player.getLocation()));
            }
        }
        return true;
    }
}
