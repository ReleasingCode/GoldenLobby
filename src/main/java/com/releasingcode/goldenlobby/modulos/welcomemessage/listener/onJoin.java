package com.releasingcode.goldenlobby.modulos.welcomemessage.listener;

import com.releasingcode.goldenlobby.listeners.custom.SecurePlayerJoinEvent;
import com.releasingcode.goldenlobby.modulos.welcomemessage.WelcomeMessage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class onJoin implements Listener {
    private final WelcomeMessage plugin;

    public onJoin(WelcomeMessage plugin) {
        this.plugin = plugin;
        plugin.getPlugin().getServer().getPluginManager().registerEvents(this, plugin.getPlugin());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoinMessage(SecurePlayerJoinEvent event) {
        event.getPlayer().sendMessage(plugin.getMessages());
    }
}
