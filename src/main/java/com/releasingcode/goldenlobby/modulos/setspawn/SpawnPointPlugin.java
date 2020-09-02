package com.releasingcode.goldenlobby.modulos.setspawn;

import com.releasingcode.goldenlobby.Utils;
import com.releasingcode.goldenlobby.configuracion.CustomConfiguration;
import com.releasingcode.goldenlobby.loader.LobbyComponente;
import com.releasingcode.goldenlobby.managers.SpawnPoint;
import com.releasingcode.goldenlobby.modulos.setspawn.command.SpawnPointCommand;
import com.releasingcode.goldenlobby.modulos.setspawn.listener.SpawnListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

public class SpawnPointPlugin extends LobbyComponente {
    public static SpawnPoint SPAWNPOINT;
    private CustomConfiguration configuration;

    @Override
    protected void onEnable() {
        Utils.log("Enabling SpawnPlugin");
        loadSpawnPoint();
    }

    public void loadSpawnPoint() {
        new SpawnPointCommand(this).register();
        configuration = new CustomConfiguration("spawnPoint", getPlugin());
        getPlugin().getServer().getPluginManager().registerEvents(new SpawnListener(), getPlugin());
        FileConfiguration config = configuration.getConfig();
        if (config.isSet("SpawnPoint.world")
                && config.isSet("SpawnPoint.x")
                && config.isSet("SpawnPoint.y")
                && config.isSet("SpawnPoint.z")) {
            try {
                String world = config.getString("SpawnPoint.world", null);
                double x = config.getDouble("SpawnPoint.x");
                double y = config.getDouble("SpawnPoint.y");
                double z = config.getDouble("SpawnPoint.z");
                float yaw = (float) config.getDouble("SpawnPoint.yaw");
                float pitch = (float) config.getDouble("SpawnPoint.pitch");
                Location loc = new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
                SPAWNPOINT = new SpawnPoint(loc);
            } catch (Exception e) {
                Utils.log("No appearance point found in SpawnPoint configuration");
            }
        }
    }

    @Override
    protected void onDisable() {
        Utils.log("Disabling SpawnPlugin");
    }

    public void saveLocation(SpawnPoint spawnPoint) {
        SPAWNPOINT = spawnPoint;
        configuration.set("SpawnPoint.world", spawnPoint.getWorld());
        configuration.set("SpawnPoint.x", spawnPoint.getX());
        configuration.set("SpawnPoint.y", spawnPoint.getY());
        configuration.set("SpawnPoint.z", spawnPoint.getZ());
        configuration.set("SpawnPoint.yaw", spawnPoint.getYaw());
        configuration.set("SpawnPoint.pitch", spawnPoint.getPitch());
    }

}

