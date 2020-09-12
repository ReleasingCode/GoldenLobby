package com.releasingcode.goldenlobby.modulos.TabList;

import com.releasingcode.goldenlobby.BaseCommand;
import com.releasingcode.goldenlobby.Utils;
import com.releasingcode.goldenlobby.languages.Lang;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TabListCommand extends BaseCommand {
    public TabListPlugin plugin;

    public TabListCommand(TabListPlugin plugin) {
        super("tbl", "/tbl", "Todo referente al TabList");
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player p = (Player) sender;
        if (!p.hasPermission("goldenlobby.tablist.admin")) {
            p.sendMessage(Lang.NO_PERMISSION.toString());
            return true;
        }

        if (args.length == 0) {
            p.sendMessage(Utils.chatColor("&e  - /tbl reload"));
            return true;
        }
        if (args[0].equalsIgnoreCase("reload")) {

            plugin.reload();
            p.sendMessage("&a - TabList Reloaded");
        }
        return false;
    }

}
