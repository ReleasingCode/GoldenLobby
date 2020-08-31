package com.releasingcode.goldenlobby.modulos.npcserver.object;

import com.releasingcode.goldenlobby.npc.api.NPC;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class LobbyStaffFound {

    private Set<String> npcs;

    public LobbyStaffFound(String data) {
        setAllNPCS(data);
    }

    public LobbyStaffFound() {
        npcs = new HashSet<>();
    }

    public void removeSet(String uid) {
        npcs.remove(uid);
    }

    public boolean addNPC(NPC npc) {
        String uid = npc.getUid();
        if (npcs.contains(uid)) {
            return false;
        }
        npcs.add(uid);
        return true;
    }

    public String allNPCs() {
        if (npcs.isEmpty()) {
            return "";
        }
        return npcs.toString().replaceAll("(\\[|]|\\s)", "");
    }

    public void setAllNPCS(String npcs) {
        this.npcs = (npcs == null || npcs.trim().isEmpty() ? new HashSet<>() : new HashSet<>(Arrays.asList(npcs.split(","))));
    }

    public int found() {
        return this.npcs.size();
    }
}
