package com.releasingcode.goldenlobby.modulos.warps;

import com.releasingcode.goldenlobby.Utils;
import com.releasingcode.goldenlobby.configuracion.CustomConfiguration;
import com.releasingcode.goldenlobby.loader.LobbyComponente;
import com.releasingcode.goldenlobby.managers.SpawnPoint;
import com.releasingcode.goldenlobby.modulos.warps.command.CommandWarp;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.File;

public class WarpsPlugin extends LobbyComponente {


    private static WarpsPlugin plugin;

    public static WarpsPlugin getInstance() {
        return plugin;
    }

    @Override
    protected void onEnable() {
        plugin = this;
        new CommandWarp("mcwarps").register();
        Utils.log("Starting Warp Plugin");
        loadWarps();
    }

    @Override
    protected void onDisable() {
        Utils.log("Desabling Warp Plugin");
    }

    public void loadWarps() {
        File[] files = CustomConfiguration.getFilesPath("/warps/");
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".yml")) {
                CustomConfiguration warpConfig = new CustomConfiguration(file, getPlugin());
                String world = warpConfig.getConfig().getString("Location.World", null);
                if (world == null) {
                    continue;
                }
                double x = warpConfig.getConfig().getDouble("Location.X", 0);
                double y = warpConfig.getConfig().getDouble("Location.Y", 0);
                double z = warpConfig.getConfig().getDouble("Location.Z", 0);
                float yaw = (float) warpConfig.getConfig().getDouble("Location.Yaw", 0);
                float pitch = (float) warpConfig.getConfig().getDouble("Location.Pitch", 0);
                SpawnPoint point = new SpawnPoint(new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch));
                Warps.addWarp(new Warps(file.getName().replace(".yml", "").toLowerCase(), point), false);
            }
        }
        Utils.log("Warps", "Warp(s) loaded &e" + Warps.getWarps().size() + "");
    }

    public void reload() {
        Warps.clear();
        loadWarps();
    }
}
