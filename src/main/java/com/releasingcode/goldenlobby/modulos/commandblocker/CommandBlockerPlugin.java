package com.releasingcode.goldenlobby.modulos.commandblocker;

import com.releasingcode.goldenlobby.Utils;
import com.releasingcode.goldenlobby.configuracion.CustomConfiguration;
import com.releasingcode.goldenlobby.loader.LobbyComponente;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import com.releasingcode.goldenlobby.modulos.commandblocker.listener.CommandEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CommandBlockerPlugin extends LobbyComponente {
    private CustomConfiguration configuration;
    private Map<String, BlockedCommand> blockedCommands;

    @Override
    protected void onEnable() {
        configuration = new CustomConfiguration("blocked_commands", getPlugin());
        if (!configuration.getConfig().getBoolean("enabled")) return;
        Utils.log(" - Cargando módulo de Bloqueador de Comandos");
        getPlugin().getServer().getPluginManager().registerEvents(new CommandEvent(this), getPlugin());
        blockedCommands = loadCommands();
    }

    private Map<String, BlockedCommand> loadCommands() {
        Map<String, BlockedCommand> blockedCommands = new HashMap<>();
        FileConfiguration config = configuration.getConfig();
        ConfigurationSection blockedCommandsSection = config.getConfigurationSection("blocked-commands");

        Set<String> commands = blockedCommandsSection.getKeys(false);
        commands.forEach(command -> {
            ConfigurationSection section = blockedCommandsSection.getConfigurationSection(command);
            String mensaje = section.getString("mensaje");
            String permission = section.getString("permission");
            blockedCommands.put("/" + command, new BlockedCommand(mensaje, permission));
        });

        if (!blockedCommands.isEmpty()) Utils.log("Cargados: " + blockedCommands.size() + " comandos bloqueados.");
        return blockedCommands;
    }

    @Override
    protected void onDisable() {
        Utils.log(" - Inhabilitando módulo de Bloqueador de Comandos");
    }

    public Map<String, BlockedCommand> getBlockedCommands() {
        return blockedCommands;
    }

}
