package com.releasingcode.goldenlobby.modulos.bossbar;

import com.releasingcode.goldenlobby.BaseCommand;
import com.releasingcode.goldenlobby.languages.Lang;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class BossBarReload extends BaseCommand {

    private final BossBarPlugin plugin;

    public BossBarReload(BossBarPlugin plugin) {
        super("bbreload", "/bbreload", "Recarga la configuracion de BossBar");
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("goldenlobby.bbreload")) {
            sender.sendMessage(Lang.NO_PERMISSION.toString());
            return true;
        }

        plugin.reload();
        sender.sendMessage(ChatColor.GOLD + "BossBar reloaded successfully!");
        return true;
    }
}
