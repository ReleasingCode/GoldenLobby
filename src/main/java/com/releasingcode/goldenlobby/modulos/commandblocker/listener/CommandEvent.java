package com.releasingcode.goldenlobby.modulos.commandblocker.listener;

import com.releasingcode.goldenlobby.Utils;
import com.releasingcode.goldenlobby.modulos.commandblocker.CommandBlockerPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandEvent implements Listener {
    private final CommandBlockerPlugin main;

    public CommandEvent(CommandBlockerPlugin main) {
        this.main = main;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommandPreProcess(PlayerCommandPreprocessEvent e) {
        String command = e.getMessage().split(" ")[0];
        if (isABlockedCommand(command) && !e.getPlayer().hasPermission(getPermissionFromCommand(command))) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(Utils.chatColor(getMessageOfBlockedCommand(command)));
        }
    }

    private boolean isABlockedCommand(String arg) {
        return main.getBlockedCommands().containsKey(arg);
    }

    private String getPermissionFromCommand(String command) {
        return main.getBlockedCommands().get(command).getPermission();
    }

    private String getMessageOfBlockedCommand(String command) {
        return main.getBlockedCommands().get(command).getMessageWhenExecuted();
    }

}
