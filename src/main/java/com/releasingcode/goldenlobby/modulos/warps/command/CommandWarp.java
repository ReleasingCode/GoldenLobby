package com.releasingcode.goldenlobby.modulos.warps.command;

import com.releasingcode.goldenlobby.BaseCommand;
import com.releasingcode.goldenlobby.languages.Lang;
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
                    if (!player.hasPermission("goldenlobby.admin.warps")) {
                        lobbyPlayer.sendMessage(Lang.NO_PERMISSION.toString());
                        return true;
                    }
                    if (args.length == 1) {
                        lobbyPlayer.sendMessage(Lang.SPECIFY_WARP_NAME.toString(),
                                "&f - &a/mcwarps create &6[nombre]");
                        return true;
                    }
                    String nombre = args[1];
                    if (Warps.existWarp(nombre)) {
                        lobbyPlayer.sendMessage(Lang.ALREADY_WARP_WITCH_NAME.toString());
                        return true;
                    }
                    Warps warps = new Warps(nombre, new SpawnPoint(player.getLocation()));
                    Warps.addWarp(warps, true);
                    lobbyPlayer.sendMessage(Lang.YOU_HAVE_CREATED_WARP.toString() + nombre + " &aCoord: &7" + warps.getLocation().toString());
                    return true;
                }
                case "delete": {
                    if (!player.hasPermission("goldenlobby.admin.warps")) {
                        player.sendMessage(Lang.NO_PERMISSION.toString());
                        return true;
                    }
                    if (args.length == 1) {
                        lobbyPlayer.sendMessage(Lang.YOU_SPECIFY_WARP_NAME.toString(),
                                "&f - &a/mcwarps delete &6[name]");
                        return true;
                    }
                    String nombre = args[1];
                    if (!Warps.existWarp(nombre)) {
                        lobbyPlayer.sendMessage(Lang.THERE_NO_WARP_THISNAME.toString());
                        return true;
                    }
                    Warps.removeWarp(nombre, true);
                    lobbyPlayer.sendMessage(Lang.WARP_HAS_BEEN_ERASED.toString() + nombre.toLowerCase());
                    return true;
                }
                case "reload": {
                    if (!player.hasPermission("goldenlobby.admin.warps")) {
                        lobbyPlayer.sendMessage(Lang.NO_PERMISSION.toString());
                        return true;
                    }
                    WarpsPlugin.getInstance().reload();
                    lobbyPlayer.sendMessage(Lang.WARP_RELOADED.toString());
                    return true;
                }
                case "list": {
                    if (!player.hasPermission("goldenlobby.admin.warps")) {
                        lobbyPlayer.sendMessage(Lang.NO_PERMISSION.toString());
                        return true;
                    }
                    if (Warps.getWarps().size() == 0) {
                        lobbyPlayer.sendMessage(Lang.NO_WARPS_AVAILABLE.toString());
                        return true;
                    }
                    for (Warps warp : Warps.getWarps()) {
                        lobbyPlayer.sendMessageWithSuggest(new MessageSuggest(" - &e" + warp.getName() + " &7[" + warp.getLocation().getWorld() + "]",
                                "mcwarps tp " + warp.getName(), Lang.CLICK_TP_WARP.toString() + warp.getName()));
                    }
                    return true;
                }
                case "tp": {
                    if (args.length == 1) {
                        lobbyPlayer.sendMessage(Lang.SPECIFY_WARP_NAME.toString(),
                                "&f - &a/mcwarps delete &6[name]");
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
        if (lobbyPlayer.getPlayer().hasPermission("goldenlobby.admin.warps")) {
            lobbyPlayer.sendMessage(
                    "&f - &a/mcwarps create [nombre]"
                    , "&f - &a/mcwarps delete [nombre]"
                    , "&f - &a/mcwarps sync [rebase]"
                    , "&f - &a/mcwarps list"
                    , "&f - &a/mcwarps tp [nombre]");
            return;
        }
        lobbyPlayer.sendMessage("&f - &a/mcwarps tp [name]");
    }
}
