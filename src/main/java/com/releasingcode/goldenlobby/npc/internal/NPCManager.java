package com.releasingcode.goldenlobby.npc.internal;

import com.releasingcode.goldenlobby.npc.api.NPC;
import com.releasingcode.goldenlobby.npc.api.state.NPCMode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


public final class NPCManager {
    private final static HashSet<NPC> npcsFullyLoaded = new HashSet<>();
    private static int entitiesIdGenerator = Integer.MAX_VALUE;

    private static ConcurrentHashMap<String, NPC> npcs = new ConcurrentHashMap<>();
    private NPCManager() {
        throw new SecurityException("No puedes inicializar está clase");
    }

    public static ArrayList<NPC> getAllNPCs() {
        return new ArrayList<>(npcs.values());
    }

    public static ArrayList<NPC> getAllNPCs(NPCMode mode) {
        return npcs.values().stream().filter(base -> base.getNPCMode().equals(mode)).collect(Collectors.toCollection(ArrayList::new));
    }

    public static void clearNpcs() {
        npcs = new ConcurrentHashMap<>();
    }

    public static Set<NPC> getFullyLoadedNPCS() {
        return npcsFullyLoaded;
    }

    public static int getEntitiesIdGenerator() {
        return entitiesIdGenerator;
    }

    public static void clearEntitiesIdGenerator() {
        entitiesIdGenerator = Integer.MAX_VALUE;
    }

    public static void addFullyLoaded(NPC base) {
        npcsFullyLoaded.add(base);
    }

    public static void clearFullyLoaded() {
        npcsFullyLoaded.clear();
    }

    public static boolean alreadyNPC(String name) {
        return npcs.containsKey(name.toLowerCase());
    }

    public static void add(String name, NPCBase npc) {
        npcs.put(name.toLowerCase(), npc);
    }

    public static void remove(String name) {
        npcs.remove(name.toLowerCase());
    }


    public static NPC getNPC(String editing) {
        if (editing == null || editing.trim().isEmpty()) {
            return null;
        }
        return npcs.getOrDefault(editing.toLowerCase(), null);
    }

    public static void reduceEntitiesIdGenerator() {
        entitiesIdGenerator--;
    }
}
