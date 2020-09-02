package com.releasingcode.goldenlobby.modulos.npcserver.history;

import com.releasingcode.goldenlobby.Utils;
import com.releasingcode.goldenlobby.configuracion.CustomConfiguration;
import com.releasingcode.goldenlobby.npc.api.NPC;
import com.releasingcode.goldenlobby.npc.api.state.NPCMode;
import com.releasingcode.goldenlobby.npc.internal.NPCManager;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class HistoryManager {
    private final Set<String> npcs = new HashSet<>();
    private final LinkedHashMap<String, ArrayList<NPCHistory>> historyLinkedHashMap;

    public HistoryManager() {
        this.historyLinkedHashMap = new LinkedHashMap<>();
        for (NPC npc : NPCManager.getAllNPCs()) {
            if (npc.getNPCMode().equals(NPCMode.HISTORY)) {
                npcs.add(npc.getName().toLowerCase());
            }
        }
        loadInitialHistory();
        Utils.log("Loaded stories: " + historyLinkedHashMap.size());
        Utils.log("NPCS Stories: " + npcs);
    }

    public void loadHistory(NPC npcHistory) {
        if (npcHistory != null) {
            CustomConfiguration configuration = npcHistory.getConfiguration();
            if (configuration != null) {
                String keyHistory = "history";
                FileConfiguration config = configuration.getConfig();
                String historyName = config.getString(keyHistory + ".name", null);
                if (historyName == null) {
                    // no se puede generar una historia sin un nombre
                    return;
                }
                List<String> required = Utils
                        .stringToArrayList(config.getString(keyHistory + ".required", null));
                String objetiveName = config.getString(keyHistory + ".objective.display", null);
                String target = config.getString(keyHistory + ".objective.target",
                        null); //si es nulo se considera fin de la historia del npc
                List<String> required_messages = Utils.stringToArrayList(
                        config.getString(keyHistory + ".required-message", null)
                );
                List<String> in_progress_message = Utils.stringToArrayList(
                        config.getString(keyHistory + ".in-progress-message", null)
                );
                List<String> dissapear = Utils.stringToArrayList(
                        config.getString(keyHistory + ".disappear-when-is-completed", null)
                );
                LinkedHashMap<Integer, NPCHistory.Dash> dashMap = new LinkedHashMap<>();
                for (String key : config.getConfigurationSection(keyHistory + ".dash").getKeys(false)) {
                    if (Utils.tryParseInt(key)) {
                        String dash = "dash.";
                        int secondsKeep = config.getInt("history." + dash + key + ".keep", 0);
                        List<String> messages = Utils
                                .stringToArrayList(config.getString("history." + dash + key + ".message", null));
                        List<String> effects = Utils
                                .stringToArrayList(config.getString("history." + dash + key + ".effects", null));
                        NPCHistory.Dash dashObject = new NPCHistory.Dash(secondsKeep, messages, effects);
                        dashMap.put(Integer.parseInt(key), dashObject);
                    }
                }
                NPCHistory historia = new NPCHistory(npcHistory
                        , historyName,
                        required,
                        objetiveName,
                        target,
                        required_messages,
                        in_progress_message,
                        dissapear, dashMap);
                npcHistory.setHistory(historia);
                if (historyLinkedHashMap.containsKey(historyName.toLowerCase())) {
                    ArrayList<NPCHistory> actual = historyLinkedHashMap.get(historyName.toLowerCase());
                    actual.add(historia);
                    historyLinkedHashMap.put(historyName.toLowerCase(), actual);
                    npcs.add(npcHistory.getName().toLowerCase());
                    return;
                }
                ArrayList<NPCHistory> histories = new ArrayList<>(Collections.singleton(historia));
                historyLinkedHashMap.put(historyName.toLowerCase(), histories);
                npcs.add(npcHistory.getName().toLowerCase());
            }
        }
    }

    public String getNameNPCByUid(String uidNpc) {
        for (String histories : historyLinkedHashMap.keySet()) {
            for (NPCHistory npcs : historyLinkedHashMap.get(histories)) {
                if (npcs.getTarget() != null) {
                    if (npcs.getNpc().getUid().trim().equals(uidNpc.trim())) {
                        return npcs.getNpc().getNameNPC();
                    }
                }
            }
        }
        return null;
    }

    public String getObjectiveByActualTarget(String target) {
        if (target.trim().isEmpty()) {
            target = null;
        }
        for (String histories : historyLinkedHashMap.keySet()) {
            for (NPCHistory npcs : historyLinkedHashMap.get(histories)) {
                if (npcs.getTarget() != null && target != null) {
                    if (npcs.getTarget().replace("-", "").toLowerCase()
                            .contains(target.replace("-", "").toLowerCase())) {
                        return npcs.getObjectiveDisplay();
                    }
                }
            }
        }
        return null;
    }

    public void loadInitialHistory() {
        Set<String> cloneNpcs = new HashSet<>(npcs);
        for (String npcName : cloneNpcs) {
            NPC npcHistory = NPCManager.getNPC(npcName);
            if (npcHistory != null) {
                loadHistory(npcHistory);
            }
        }
    }

    public int totalHistory() {
        int count = 0;
        for (String histories : historyLinkedHashMap.keySet()) {
            for (NPCHistory npcs : historyLinkedHashMap.get(histories)) {
                if (npcs.getHistoryName() != null) {
                    count++;
                }
            }
        }
        return count;
    }
}
