package com.releasingcode.goldenlobby.managers;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultAPI {
    private final Plugin plugin;
    private Economy econ;
    private Permission perms;
    private Chat chat;

    public VaultAPI(Plugin plugin) {
        this.plugin = plugin;
    }

    public boolean setupEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy>
                rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public boolean setupChat() {
        RegisteredServiceProvider<Chat> rsp = plugin.getServer().getServicesManager().getRegistration(Chat.class);
        chat = rsp.getProvider();
        return chat != null;
    }

    public Chat getChat() {
        return chat;
    }

    public Permission getPerms() {
        return perms;
    }

    public boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp =
                plugin.getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }

    public String getRank(Player player) {
        if (getPerms() != null && getChat() != null) {
            try {
                String playerGroup = perms.getPrimaryGroup(player);
                return chat.getGroupPrefix(player.getWorld(), playerGroup);
            } catch (Exception e) {
                return "";
            }
        }
        return "";
    }

    public double getEconPlayer(OfflinePlayer player) {
        if (getEcon() != null) {
            return getEcon().getBalance(player);
        }
        return 0;
    }

    public double getEconPlayer(Player player) {
        if (getEcon() != null) {
            return getEcon().getBalance(player);
        }
        return 0;
    }

    public Economy getEcon() {
        return econ;
    }
}
