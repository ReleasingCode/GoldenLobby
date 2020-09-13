package com.releasingcode.goldenlobby.modulos.launchpad;

import com.releasingcode.goldenlobby.BaseCommand;
import com.releasingcode.goldenlobby.languages.Lang;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class LaunchPadReload extends BaseCommand {

    private final LaunchPadPlugin plugin;

    public LaunchPadReload(LaunchPadPlugin plugin) {
        super("jpreload", "/jpreload", "Reloads the " + plugin.getName() + " module's configuration");
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("goldenlobby.jpreload")) {
            sender.sendMessage(Lang.NO_PERMISSION.toString());
            return true;
        }

        plugin.getConfig().reloadConfig();
        sender.sendMessage(ChatColor.GOLD + "Reload complete!");
        return true;
    }
}
