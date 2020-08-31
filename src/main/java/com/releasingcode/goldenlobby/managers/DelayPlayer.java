package com.releasingcode.goldenlobby.managers;

import org.bukkit.entity.Player;

import java.util.concurrent.ConcurrentHashMap;

public class DelayPlayer {
    private static final ConcurrentHashMap<String, ConcurrentHashMap<String, Long>> delay = new ConcurrentHashMap<>();

    public static void addDelay(Player player, String key, long timeMillis) {
        ConcurrentHashMap<String, Long> keyMap = new ConcurrentHashMap<>();
        keyMap.put(key.toLowerCase(), System.currentTimeMillis() + timeMillis);
        delay.put(player.getUniqueId().toString(), keyMap);
    }

    public static boolean containsDelay(Player player, String key) {
        if (delay.containsKey(player.getUniqueId().toString())) {
            if (delay.get(player.getUniqueId().toString()).containsKey(key.toLowerCase())) {
                if (delay.get(player.getUniqueId().toString()).get(key.toLowerCase()) <= System.currentTimeMillis()) {
                    delay.get(player.getUniqueId().toString()).remove(key.toLowerCase());
                    return false;
                }
                return true;
            }
        }
        return false;
    }
}
