package com.releasingcode.goldenlobby.modulos.npcserver.object;

import com.releasingcode.goldenlobby.npc.api.NPC;
import com.releasingcode.goldenlobby.npc.internal.NPCManager;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class LobbyPlayerBuilder {
    private String editing;
    private List<String> hologram;
    private List<String> commands;
    private List<String> rewardCommands;
    private boolean selectingNpc;

    public LobbyPlayerBuilder() {
        editing = null;
        hologram = new ArrayList<>();
        commands = new ArrayList<>();
        rewardCommands = new ArrayList<>();
        selectingNpc = false;
    }

    public List<String> getRewardCommands() {
        return rewardCommands;
    }

    public void setRewardCommands(List<String> rewardCommands) {
        this.rewardCommands = new ArrayList<>(rewardCommands);
    }

    public boolean isSelecting() {
        if (editing != null) {
            selectingNpc = false;
        }
        return selectingNpc;
    }

    public void setSelectingNpc(boolean selectingNpc) {
        this.selectingNpc = selectingNpc;
    }

    public List<String> getHologram() {
        return hologram;
    }

    public void setHologram(List<String> hologram) {
        this.hologram = new ArrayList<>(hologram);
    }

    public boolean removeCommand(String linea) {
        try {
            int x = Integer.parseInt(linea);
            x--;
            if (x < 0) {
                return false;
            }
            commands.remove(x);
            return true;
        } catch (Exception ignored) {

        }
        return false;
    }

    public boolean removeRewardCommands(String linea) {
        try {
            int x = Integer.parseInt(linea);
            x--;
            if (x < 0) {
                return false;
            }
            rewardCommands.remove(x);
            return true;
        } catch (Exception ignored) {

        }
        return false;
    }

    public void addRewardCommands(String text) {
        if (text == null) {
            return;
        }
        rewardCommands.add(text);
    }

    public void addCommand(String text) {
        if (text == null) {
            return;
        }
        commands.add(text);
    }

    public void addLine(String text) {
        if (text == null) {
            return;
        }
        hologram.add(ChatColor.translateAlternateColorCodes('&', text));
    }

    public boolean isEditing() {
        return editing != null && NPCManager.getNPC(editing) != null;
    }

    public void setEditing(String npc) {
        if (npc == null) {
            hologram.clear();
            commands.clear();
        }
        this.editing = npc;
    }

    public NPC getNpc() {
        return NPCManager.getNPC(editing);
    }

    public boolean removeLine(String linea) {
        try {
            int x = Integer.parseInt(linea);
            x--;
            if (x < 0) {
                return false;
            }
            hologram.remove(x);
            return true;
        } catch (Exception ignored) {

        }
        return false;
    }

    public List<String> getCommands() {
        return commands;
    }

    public void setCommands(List<String> commands) {
        this.commands = new ArrayList<>(commands);
    }
}
