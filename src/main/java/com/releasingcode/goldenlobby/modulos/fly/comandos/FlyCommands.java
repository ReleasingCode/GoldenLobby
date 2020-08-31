package com.releasingcode.goldenlobby.modulos.fly.comandos;

import com.releasingcode.goldenlobby.BaseCommand;
import com.releasingcode.goldenlobby.LobbyMC;
import com.releasingcode.goldenlobby.managers.LobbyPlayer;
import com.releasingcode.goldenlobby.managers.LobbyPlayerMap;
import com.releasingcode.goldenlobby.modulos.regions.cuboid.StageOfCreation;
import es.minecub.core.lang.Lang;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;


public class FlyCommands extends BaseCommand {
    public FlyCommands() {
        super("fly", "/fly", "Volar jugador");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player && sender.hasPermission("lobbymc.benefits.fly")) {
            Player player = ((Player) sender).getPlayer();

            if (isInPvpZone(player)) {
                player.sendMessage(ChatColor.RED + "${lobbymc.flight.player.inpvpregion}");
                return false;
            }

            LobbyPlayer lobbyPlayer = LobbyPlayerMap.getJugador(player);
            if (lobbyPlayer == null) {
                return true;
            }
            if (!player.isFlying()) {
                lobbyPlayer.setFlyer(true);
                player.setAllowFlight(true);
                player.setFlying(true);
                player.setMetadata("userFlying", new FixedMetadataValue(LobbyMC.getInstance(), null));
                player.removeMetadata("doubleJump", LobbyMC.getInstance());
                lobbyPlayer.sendMessage("${lobbymc.flight.player.enabled}");
            } else {
                lobbyPlayer.setFlyer(false);
                player.removeMetadata("userFlying", LobbyMC.getInstance());
                player.setFlying(false);
                lobbyPlayer.sendMessage("${lobbymc.flight.player.disabled}");
            }
        } else if (!sender.hasPermission("lobbymc.benefits.fly")) {
            sender.sendMessage(Lang.NO_PERMISSIONS);
        }
        return true;
    }

    private boolean isInPvpZone(Player p) {
        if (!LobbyMC.getInstance().getCuboidManager().getRegions().containsKey(StageOfCreation.Regions.PVP))
            return false;
        return LobbyMC.getInstance().getCuboidManager().getRegions().get(StageOfCreation.Regions.PVP).isIn(p);
    }

}
