package com.releasingcode.goldenlobby.modulos.welcomemessage.command;

import com.releasingcode.goldenlobby.BaseCommand;
import com.releasingcode.goldenlobby.Utils;
import com.releasingcode.goldenlobby.languages.Lang;
import com.releasingcode.goldenlobby.modulos.welcomemessage.WelcomeMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class WelcomeMessageCmd extends BaseCommand {
    private final WelcomeMessage plugin;

    public WelcomeMessageCmd(WelcomeMessage plugin, String command) {
        super(command);
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("goldenlobby.general.welcomemessagereload")) {
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(Utils.chatColor("&c/welcomemessage reload"));
            return true;
        }
        plugin.reload();
        sender.sendMessage(Utils.chatColor(Lang.WELCOME_MESSAGE_SETTING.toString()));
        return true;
    }
}
