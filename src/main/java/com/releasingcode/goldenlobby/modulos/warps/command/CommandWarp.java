package com.releasingcode.goldenlobby.modulos.warps.command;

import com.releasingcode.goldenlobby.BaseCommand;
import com.releasingcode.goldenlobby.managers.LobbyPlayer;
import com.releasingcode.goldenlobby.managers.LobbyPlayerMap;
import com.releasingcode.goldenlobby.managers.MessageSuggest;
import com.releasingcode.goldenlobby.managers.SpawnPoint;
import com.releasingcode.goldenlobby.modulos.warps.Warps;
import com.releasingcode.goldenlobby.modulos.warps.WarpsPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandWarp extends BaseCommand {
    public CommandWarp(String command) {
        super(command);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = ((Player) sender).getPlayer();
            LobbyPlayer lobbyPlayer = LobbyPlayerMap.getJugador(player);
            if (args.length == 0) {
                sendHelpPlayer(lobbyPlayer);
                return true;
            }
            String argumento = args[0];
            switch (argumento) {
                case "create": {
                    if (!player.hasPermission("lobbymc.admin.warps")) {
                        lobbyPlayer.sendMessage("&cNo tienes permisos para realizar esta acción");
                        return true;
                    }
                    if (args.length == 1) {
                        lobbyPlayer.sendMessage("&cDebes especificar un nombre de warp",
                                "&f - &a/mcwarps create &6[nombre]");
                        return true;
                    }
                    String nombre = args[1];
                    if (Warps.existWarp(nombre)) {
                        lobbyPlayer.sendMessage("&cYa existe un warp con este nombre");
                        return true;
                    }
                    Warps warps = new Warps(nombre, new SpawnPoint(player.getLocation()));
                    Warps.addWarp(warps, true);
                    lobbyPlayer.sendMessage("&aHas creado el warp: &e" + nombre + " &aCoord: &7" + warps.getLocation().toString());
                    return true;
                }
                case "delete": {
                    if (!player.hasPermission("lobbymc.admin.warps")) {
                        player.sendMessage("&cNo tienes permisos para realizar esta acción");
                        return true;
                    }
                    if (args.length == 1) {
                        lobbyPlayer.sendMessage("&cDebes especificar un nombre de warp",
                                "&f - &a/mcwarps delete &6[nombre]");
                        return true;
                    }
                    String nombre = args[1];
                    if (!Warps.existWarp(nombre)) {
                        lobbyPlayer.sendMessage("&cNo existe un warp con este nombre");
                        return true;
                    }
                    Warps.removeWarp(nombre, true);
                    lobbyPlayer.sendMessage("&cSe ha borrado el warp: &e" + nombre.toLowerCase());
                    return true;
                }
                case "reload": {
                    if (!player.hasPermission("lobbymc.admin.warps")) {
                        lobbyPlayer.sendMessage("&cNo tienes permisos para realizar esta acción");
                        return true;
                    }
                    WarpsPlugin.getInstance().reload();
                    lobbyPlayer.sendMessage("&aSe ha recargado los warps");
                    return true;
                }
                case "list": {
                    if (!player.hasPermission("lobbymc.admin.warps")) {
                        lobbyPlayer.sendMessage("&cNo tienes permisos para realizar esta acción");
                        return true;
                    }
                    if (Warps.getWarps().size() == 0) {
                        lobbyPlayer.sendMessage("&cNo hay ninguna warps disponible");
                        return true;
                    }
                    for (Warps warp : Warps.getWarps()) {
                        lobbyPlayer.sendMessageWithSuggest(new MessageSuggest(" - &e" + warp.getName() + " &7[" + warp.getLocation().getWorld() + "]",
                                "mcwarps tp " + warp.getName(), "&aClick para teletransportarse a la warp &e" + warp.getName()));
                    }
                    return true;
                }
                case "tp": {
                    if (args.length == 1) {
                        lobbyPlayer.sendMessage("&cDebes especificar un nombre de warp",
                                "&f - &a/mcwarps delete &6[nombre]");
                        return true;
                    }
                    String nombre = args[1];
                    Warps warp = Warps.getWarp(nombre);
                    if (warp == null) {
                        return true;
                    }
                    try {
                        player.teleport(warp.getLocation().getLocation());
                    } catch (Exception e) {
                    }
                    return true;
                }
                default: {
                    sendHelpPlayer(lobbyPlayer);
                    break;
                }
            }
        }
        return false;
    }

    public void sendHelpPlayer(LobbyPlayer lobbyPlayer) {
        if (lobbyPlayer.getPlayer().hasPermission("lobbymc.admin.warps")) {
            lobbyPlayer.sendMessage(
                    "&f - &a/mcwarps create [nombre]"
                    , "&f - &a/mcwarps delete [nombre]"
                    , "&f - &a/mcwarps sync [rebase]"
                    , "&f - &a/mcwarps list"
                    , "&f - &a/mcwarps tp [nombre]");
            return;
        }
        lobbyPlayer.sendMessage("&f - &a/mcwarps tp [nombre]");
    }
}
