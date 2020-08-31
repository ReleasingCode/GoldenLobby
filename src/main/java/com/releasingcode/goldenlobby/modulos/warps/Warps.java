package com.releasingcode.goldenlobby.modulos.warps;

import com.releasingcode.goldenlobby.LobbyMC;
import com.releasingcode.goldenlobby.configuracion.CustomConfiguration;
import com.releasingcode.goldenlobby.managers.SpawnPoint;

import java.util.Collection;
import java.util.HashMap;

public class Warps {
    final static HashMap<String, Warps> warps = new HashMap<>();
    private final String name;
    private final SpawnPoint location;

    public Warps(String name, SpawnPoint location) {
        this.name = name;
        this.location = location;
    }

    public static void addWarp(Warps warp, boolean createFile) {
        warps.put(warp.getName().toLowerCase(), warp);
        if (createFile) {
            CustomConfiguration customConfiguration = new CustomConfiguration(warp.getName().toLowerCase(), "/warps/", LobbyMC.getInstance());
            customConfiguration.set("Location.World", warp.getLocation().getWorld());
            customConfiguration.set("Location.X", warp.getLocation().getX());
            customConfiguration.set("Location.Y", warp.getLocation().getY());
            customConfiguration.set("Location.Z", warp.getLocation().getZ());
            customConfiguration.set("Location.Yaw", warp.getLocation().getYaw());
            customConfiguration.set("Location.Pitch", warp.getLocation().getPitch());
        }
    }

    public static void removeWarp(String name, boolean deleteFile) {
        warps.remove(name.toLowerCase());
        if (deleteFile) {
            CustomConfiguration.deleteFile("/warps", name.toLowerCase() + ".yml");
        }
    }

    public static void clear() {
        warps.clear();
    }

    public static boolean existWarp(String name) {
        return warps.containsKey(name.toLowerCase());
    }

    public static Warps getWarp(String name) {
        return warps.getOrDefault(name.toLowerCase(), null);
    }

    public static Collection<Warps> getWarps() {
        return warps.values();
    }

    public String getName() {
        return name;
    }

    public SpawnPoint getLocation() {
        return location;
    }
}
