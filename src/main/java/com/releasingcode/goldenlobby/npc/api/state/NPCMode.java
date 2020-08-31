package com.releasingcode.goldenlobby.npc.api.state;

import org.bukkit.ChatColor;

public enum NPCMode {
    STAFF("staff"),
    HISTORY("history"),
    COMMAND("command");

    private final String directory;

    NPCMode(String directory) {
        this.directory = directory;
    }

    public static NPCMode from(String parse, NPCMode refDefault) {
        for (NPCMode a : values()) {
            if (a.name().equals(parse.toUpperCase())) {
                return a;
            }
        }
        return refDefault;
    }

    public static NPCMode from(String parse) {
        for (NPCMode a : values()) {
            if (a.name().equals(parse.toUpperCase())) {
                return a;
            }
        }
        return null;
    }

    public String getDirectory() {
        return directory;
    }

    @Override
    public String toString() {
        return this == STAFF ? ChatColor.GOLD + name() : ChatColor.YELLOW + name();
    }
}
