package com.releasingcode.goldenlobby.modulos.gamemode.comandos;

import com.releasingcode.goldenlobby.BaseCommand;
import com.releasingcode.goldenlobby.managers.LobbyPlayer;
import com.releasingcode.goldenlobby.managers.LobbyPlayerMap;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GamemodeCommand extends BaseCommand {

    public GamemodeCommand() {
        super("gm", "/gm [1][2][3]", "Gamemode del jugador");
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
                lobbyPlayer.sendMessage("&cPor favor escribe el modo de juego");
                lobbyPlayer.sendMessage("&a0.- Survival");
                lobbyPlayer.sendMessage("&a1.- Creativo");
                lobbyPlayer.sendMessage("&a2.- Aventura");
                lobbyPlayer.sendMessage("&a2.- Espectador");
                return true;
            }
            String modoDeJuego = args[0];
            switch (modoDeJuego) {
                case "0": {
                    lobbyPlayer.incrementKills();
                    if (player.hasPermission("lobbymc.gm.0")) {
                        lobbyPlayer.setGameMode(GameMode.SURVIVAL);
                        lobbyPlayer.sendMessage("&aTu modo de juego ha cambiado a survival");
                    } else {
                        player.sendMessage(ChatColor.RED + "No tienes permisos para realizar esta acción");
                    }
                    break;
                }
                case "1": {
                    lobbyPlayer.setGameMode(GameMode.CREATIVE);
                    lobbyPlayer.sendMessage("&aTu modo de juego ha cambiado a Creativo");
                    break;
                }
                case "2": {
                    lobbyPlayer.setGameMode(GameMode.ADVENTURE);
                    lobbyPlayer.sendMessage("&aTu modo de juego ha cambiado a Aventura");
                    break;
                }
                case "3": {
                    lobbyPlayer.setGameMode(GameMode.SPECTATOR);
                    lobbyPlayer.sendMessage("&aTu modo de juego ha cambiado a Espectador");
                    break;
                }
                default: {
                    lobbyPlayer.sendMessage("No existe el modo de juego que estás escribiendo");
                    lobbyPlayer.sendMessage("&cPor favor escribe el modo de juego");
                    lobbyPlayer.sendMessage("&a0.- Survival");
                    lobbyPlayer.sendMessage("&a1.- Creativo");
                    lobbyPlayer.sendMessage("&a2.- Aventura");
                    lobbyPlayer.sendMessage("&a2.- Espectador");
                }
            }
        }
        return true;
    }
}
