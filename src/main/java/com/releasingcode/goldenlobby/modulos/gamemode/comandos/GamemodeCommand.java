package com.releasingcode.goldenlobby.modulos.gamemode.comandos;

import com.releasingcode.goldenlobby.BaseCommand;
import com.releasingcode.goldenlobby.languages.Lang;
import com.releasingcode.goldenlobby.managers.LobbyPlayer;
import com.releasingcode.goldenlobby.managers.LobbyPlayerMap;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GamemodeCommand extends BaseCommand {

    public GamemodeCommand() {
        super("gm", "/gm [1][2][3]", "Player's gamemode");
    }

    // gamemode 0 survival
    // gm 1 creativo
    // gm 2 adventure
    // gm 3 espectador
    // gm  -> escribe el modo de juego
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = ((Player) sender).getPlayer();
            LobbyPlayer lobbyPlayer = LobbyPlayerMap.getJugador(player);
            if (args.length == 0) {
                lobbyPlayer.sendMessage(Lang.WRITTE_GAMEMODE.toString());
                lobbyPlayer.sendMessage("&a0.- Survival");
                lobbyPlayer.sendMessage("&a1.- Creative");
                lobbyPlayer.sendMessage("&a2.- Adventure");
                lobbyPlayer.sendMessage("&a2.- Spectator");
                return true;
            }
            String modoDeJuego = args[0];
            switch (modoDeJuego) {
                case "0": {
                    lobbyPlayer.incrementKills();
                    if (player.hasPermission("goldenlobby.gm.0")) {
                        lobbyPlayer.setGameMode(GameMode.SURVIVAL);
                        lobbyPlayer.sendMessage(Lang.GAMEMODE_CHANGED.toString());
                    } else {
                        player.sendMessage(Lang.NO_PERMISSION.toString());
                    }
                    break;
                }
                case "1": {
                    lobbyPlayer.setGameMode(GameMode.CREATIVE);
                    lobbyPlayer.sendMessage(Lang.GAMEMODE_CHANGED_CREATIVE.toString());
                    break;
                }
                case "2": {
                    lobbyPlayer.setGameMode(GameMode.ADVENTURE);
                    lobbyPlayer.sendMessage(Lang.GAMEMODE_CHANGED_ADVENTURE.toString());
                    break;
                }
                case "3": {
                    lobbyPlayer.setGameMode(GameMode.SPECTATOR);
                    lobbyPlayer.sendMessage(Lang.GAMEMODE_CHANGED_SPECTACTOR.toString());
                    break;
                }
                default: {
                    lobbyPlayer.sendMessage(Lang.GAMEMODE_CHANGED_NOTEXIST.toString());
                    lobbyPlayer.sendMessage(Lang.WRITTE_GAMEMODE.toString());
                    lobbyPlayer.sendMessage("&a0.- Survival");
                    lobbyPlayer.sendMessage("&a1.- Creative");
                    lobbyPlayer.sendMessage("&a2.- Adventure");
                    lobbyPlayer.sendMessage("&a2.- Spectator");
                }
            }
        }
        return true;
    }
}
