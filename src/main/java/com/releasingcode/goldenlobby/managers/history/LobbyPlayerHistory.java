package com.releasingcode.goldenlobby.managers.history;

import com.releasingcode.goldenlobby.languages.Lang;
import com.releasingcode.goldenlobby.managers.indexing.LobbyPlayerIndexing;
import com.releasingcode.goldenlobby.modulos.npcserver.NPCServerPlugin;
import com.releasingcode.goldenlobby.npc.api.NPC;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class LobbyPlayerHistory {
    private final LobbyPlayerIndexing indexing;
    private Set<String> registred;
    private String actualTarget;
    private String currentPlaying;
    private String previusTarget;
    private boolean playing;
    private String objective;
    private int playedMessage;


    public LobbyPlayerHistory(String npcs, String actualTarget, String previusTarget) {
        setRegistredHistory(npcs);
        this.actualTarget = actualTarget != null && actualTarget.trim().isEmpty() ? null : actualTarget;
        this.previusTarget = previusTarget != null && previusTarget.trim().isEmpty() ? null : previusTarget;
        this.currentPlaying = null;
        playing = false;
        this.objective = null;
        playedMessage = -1;
        indexing = new LobbyPlayerIndexing();
    }


    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public LobbyPlayerIndexing getIndexing() {
        return indexing;
    }

    public String getActualTarget() {
        if (actualTarget != null && actualTarget.trim().isEmpty()) {
            actualTarget = null;
        }
        return actualTarget;
    }

    public boolean isNullActualTarget() {
        return actualTarget == null || actualTarget.trim().isEmpty();
    }

    public void setActualTarget(String actualTarget) {
        this.actualTarget = actualTarget != null ? actualTarget.replace("-", "") : null;
    }

    public boolean containsRegistred(String uid) {
        return registred.contains(uid.replace("-", ""));
    }

    public Set<String> getRegistred() {
        return registred;
    }

    public String getPreviusTarget() {
        if (previusTarget != null && previusTarget.trim().isEmpty()) {
            previusTarget = null;
        }
        return previusTarget;
    }

    public void setPreviusTarget(String target) {
        this.previusTarget = target != null ? target.replace("-", "") : null;
    }

    public String registredString() {
        if (registred.isEmpty()) {
            return "";
        }
        return registred.toString().replaceAll("(\\[|]|\\s)", "");
    }

    public boolean addNextHistory(NPC npc) {
        String uid = npc.getUid();
        if (registred.contains(uid)) {
            return false;
        }
        registred.add(npc.getUid());
        return true;
    }

    public void removeSet(String uid) {
        registred.remove(uid);
    }

    public void setRegistredHistory(String npcs) {
        this.registred =
                (npcs == null ||
                        npcs.trim().isEmpty()
                        ? new HashSet<>() : new HashSet<>(Arrays.asList(npcs.split(","))));
    }

    public int getPlayedMessage() {
        return playedMessage;
    }

    public void setPlayedMessage(int index) {
        this.playedMessage = index;
    }

    public String getObjective() {
        if (currentPlaying != null) {
            String name = NPCServerPlugin.getInstance().getHistoryManager().getNameNPCByUid(currentPlaying);
            if (name != null) {
                return (Lang.TALKING_TO.toString() + " " + name);
            }
        }
        return objective;
    }

    public void setObjective(String objective) {
        this.objective = objective;
    }

    public String getCurrentPlaying() {
        if (currentPlaying != null && currentPlaying.trim().isEmpty()) {
            return null;
        }
        return currentPlaying;
    }

    public void setCurrentPlaying(String currentPlaying) {
        this.currentPlaying = currentPlaying;
    }
}
