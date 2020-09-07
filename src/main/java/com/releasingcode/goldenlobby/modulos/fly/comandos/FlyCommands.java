package com.releasingcode.goldenlobby.modulos.fly.comandos;

import com.releasingcode.goldenlobby.BaseCommand;
import com.releasingcode.goldenlobby.GoldenLobby;
import com.releasingcode.goldenlobby.languages.Lang;
import com.releasingcode.goldenlobby.managers.LobbyPlayer;
import com.releasingcode.goldenlobby.managers.LobbyPlayerMap;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;


public class FlyCommands extends BaseCommand {
    public FlyCommands() {
        super("fly", "/fly", "Flying player");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player && sender.hasPermission("goldenlobby.benefits.fly")) {
            Player player = ((Player) sender).getPlayer();
            LobbyPlayer lobbyPlayer = LobbyPlayerMap.getJugador(player);
            if (lobbyPlayer == null) {
                return true;
            }
            if (!player.isFlying()) {
                lobbyPlayer.setFlyer(true);
                player.setAllowFlight(true);
                player.setFlying(true);
                player.setMetadata("userFlying", new FixedMetadataValue(GoldenLobby.getInstance(), null));
                player.removeMetadata("doubleJump", GoldenLobby.getInstance());
                lobbyPlayer.sendMessage("${lobbymc.flight.player.enabled}");
            } else {
                lobbyPlayer.setFlyer(false);
                player.removeMetadata("userFlying", GoldenLobby.getInstance());
                player.setFlying(false);
                lobbyPlayer.sendMessage("${lobbymc.flight.player.disabled}");
            }
        } else if (!sender.hasPermission("goldenlobby.benefits.fly")) {
            sender.sendMessage(Lang.NO_PERMISSION.toString());
        }
        return true;
    }

}