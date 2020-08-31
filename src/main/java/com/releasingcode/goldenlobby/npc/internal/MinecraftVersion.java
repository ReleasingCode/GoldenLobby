package com.releasingcode.goldenlobby.npc.internal;

import org.bukkit.Bukkit;

public enum MinecraftVersion {

    V1_8_R2,
    V1_8_R3,
    V1_9_R1,
    V1_9_R2,
    V1_10_R1,
    V1_11_R1,
    V1_12_R1,
    V1_13_R1,
    V1_13_R2,
    V1_14_R1,
    V1_15_R1;

    public static MinecraftVersion getNMSVersion() {
        String v = Bukkit.getServer().getClass().getPackage().getName();
        return MinecraftVersion.valueOf(v.substring(v.lastIndexOf('.') + 1).toUpperCase());
    }

    public boolean isAboveOrEqual(MinecraftVersion compare) {
        return ordinal() >= compare.ordinal();
    }
}
