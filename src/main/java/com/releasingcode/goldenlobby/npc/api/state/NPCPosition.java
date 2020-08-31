package com.releasingcode.goldenlobby.npc.api.state;

public enum NPCPosition {
    NORMAL,
    SIT,
    CORPSE;

    public static NPCPosition from(String name) {
        for (NPCPosition position : values()) {
            if (position.name().toLowerCase().equals(name.toLowerCase())) {
                return position;
            }
        }
        return null;
    }
}
