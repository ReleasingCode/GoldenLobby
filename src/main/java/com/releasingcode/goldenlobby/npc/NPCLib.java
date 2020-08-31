package com.releasingcode.goldenlobby.npc;

import com.releasingcode.goldenlobby.LobbyMC;
import com.releasingcode.goldenlobby.Utils;
import es.minecub.core.apis.packetlistener.PacketListenerCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import com.releasingcode.goldenlobby.connections.ServerManager;
import com.releasingcode.goldenlobby.extendido.nms.ParticleEffect;
import com.releasingcode.goldenlobby.managers.DelayPlayer;
import com.releasingcode.goldenlobby.managers.LobbyPlayer;
import com.releasingcode.goldenlobby.managers.LobbyPlayerMap;
import com.releasingcode.goldenlobby.managers.history.LobbyPlayerHistory;
import com.releasingcode.goldenlobby.managers.indexing.LobbyPlayerIndexing;
import com.releasingcode.goldenlobby.modulos.npcserver.NPCServerPlugin;
import com.releasingcode.goldenlobby.modulos.npcserver.history.NPCHistory;
import com.releasingcode.goldenlobby.npc.api.NPC;
import com.releasingcode.goldenlobby.npc.internal.NPCManager;
import com.releasingcode.goldenlobby.npc.listener.PacketListeners;
import com.releasingcode.goldenlobby.npc.listener.PlayerListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class NPCLib {
    private static LobbyMC plugin;
    private final ScheduledExecutorService executorService;
    Runnable taskNPC = () -> {

        for (Player p : Bukkit.getOnlinePlayers()) {
            LobbyPlayer lp = LobbyPlayerMap.getJugador(p);
            if (lp == null) {
                continue;
            }
            if (DelayPlayer.containsDelay(p, "show_npc")) {
                continue;
            }
            for (NPC npc : NPCManager.getAllNPCs()) {
                //update hologram
                try {
                    if (!npc.isReady() || !p.getWorld().getName().equals(npc.getWorld().getName())) {
                        continue;
                    }
                    if (npc.isUpdateHologramAutomatic()) {
                        int hologramIndexUpdater = lp.getGlobalIndexing()
                                .getAndIncrementIndexAtExtra(npc.getId() + "_hologram_update", 5);
                        if (!npc.getText().isEmpty()) {
                            if (hologramIndexUpdater == 0 || hologramIndexUpdater >= 4) {
                                List<String> textUpdater = new ArrayList<>(npc.getText());
                                npc.updateText(ServerManager.translateVar(textUpdater, npc), p);
                            }
                        }
                    }
                    if (!p.isDead() && p.isValid() && p.isOnline()) {
                        if (npc.isShown(p)) {
                            if (npc.inRangeOf(p, 200)) { // en rango para moverse y mirar al jugador
                                npc.lookAt(p);
                            }
                            if (npc.getHistory() != null) {
                                NPCHistory npcHistory = npc.getHistory();
                                LobbyPlayerHistory history = lp.getHistory();
                                // -------------> Desaparece el NPC si ya ha cumplido su objetivo registrado
                                if (npc.getHistory().hasDissapearWhenIsCompleted(history)) {
                                    npc.hide(p);
                                    continue;
                                }// <---------------------
                                if (npc.inRangeOf(p, 100)) {
                                    LobbyPlayerIndexing indexing = history.getIndexing();
                                    if (history.isPlaying() && history.getCurrentPlaying() != null && npc.getUid()
                                            .equals(history.getCurrentPlaying())) {
                                        //reproducir la historia
                                        int index = indexing.getIndexAtExtra(npcHistory.getHistoryName()
                                                + "_dash", history.getPlayedMessage() + 1);
                                        if (index < npcHistory.getDashMap().size()) {
                                            if (!npcHistory.getDashMap().containsKey(index)) {
                                                indexing.pushIndexAtExtra(npcHistory.getHistoryName()
                                                        + "_dash", 1);
                                                continue;
                                            }
                                            NPCHistory.Dash dash = npcHistory.getDashMap().get(index);
                                            int next = indexing
                                                    .getAndIncrementIndexAtExtra(npcHistory.getHistoryName()
                                                                    + "_dashkeep",
                                                            (dash.getKeepTick() + 1));
                                            if (history.getPlayedMessage() != index) {
                                                history.setPlayedMessage(index);
                                                history.setCurrentPlaying(npc.getUid());
                                                NPCServerPlugin.getInstance().getHistoryDB().savePlayer(lp,
                                                        () -> {
                                                            lp.sendMessage(dash.getMessages());
                                                            dash.sendEffects(p, npc);
                                                        }, false);
                                            }
                                            if (next >= dash.getKeepTick()) {
                                                //continuar con el siguiente index
                                                if ((index + 1) < npcHistory.getDashMap().size()) {
                                                    indexing.pushIndexAtExtra(npcHistory.getHistoryName()
                                                            + "_dash", 1);
                                                } else {
                                                    //no hay mas indice
                                                    //aquí acaba todo
                                                    history.setPlaying(false);
                                                    history.setPreviusTarget(npc.getUid());
                                                    history.setActualTarget(npcHistory.getTarget());
                                                    history.setCurrentPlaying(null);
                                                    if (npcHistory.getObjectiveDisplay() != null) {
                                                        history.setObjective(ChatColor
                                                                .translateAlternateColorCodes('&',
                                                                        npcHistory.getObjectiveDisplay()));
                                                    } else {
                                                        history.setObjective(null);
                                                    }
                                                    NPCServerPlugin.getInstance().getHistoryDB().savePlayer(lp,
                                                            () -> {

                                                            }, false);
                                                    history.setPlayedMessage(-1);
                                                    indexing.remove(npcHistory.getHistoryName()
                                                                    + "_dashkeep",
                                                            npcHistory.getHistoryName()
                                                                    + "_dash");
                                                }
                                            }

                                        }
                                    } else {
                                        // verificar cual es el objetivo del jugador
                                        // si no tiene ningun npc registrado del historia significa
                                        // que no
                                        // p.sendMessage("historia spawn particle");
                                        if (npc.getUid().equals(history.getActualTarget())
                                                || npc.getUid().equals(history.getCurrentPlaying())) {
                                            int count = indexing
                                                    .getAndIncrementIndexAtExtra("particle_send", (10 + 1));
                                            if (count >= 10) {
                                                ParticleEffect.VILLAGER_HAPPY
                                                        .display(0.3f, 1.0f, 0.3f, 0.0f, 15, npc.getLocation(),
                                                                p);
                                            }
                                            continue;
                                        }
                                        if (history.getActualTarget() == null && npcHistory.getRequired()
                                                .isEmpty()) {
                                            int count = indexing
                                                    .getAndIncrementIndexAtExtra("particle_send", (10 + 1));
                                            if (count >= 10) {
                                                ParticleEffect.VILLAGER_HAPPY
                                                        .display(0.3f, 1.0f, 0.3f, 0.0f, 15, npc.getLocation(),
                                                                p);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (npc.inRangeOf(p)) {
                            if (!npc.isShown(p)) {
                                if (npc.getHistory() != null
                                        && npc.getHistory().hasDissapearWhenIsCompleted(lp.getHistory())) {
                                    continue;
                                }
                                npc.show(p);
                            }
                        } else {
                            if (npc.isShown(p)) {
                                npc.hide(p);
                            }
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };
    private Class<?> npcClass;
    private ScheduledFuture<?> thread;

    public NPCLib(LobbyMC plugin) {
        executorService = Executors.newScheduledThreadPool(2);
        NPCLib.plugin = plugin;
        try {
            npcClass = Class.forName(
                    "us.minecub.lobbymc.extendido.nms." + LobbyMC.getVersion() + ".NPC_" + LobbyMC.getVersion());
        } catch (Exception ignored) {
        }
        if (npcClass == null) {
            Utils.log("&cNo se puede cargar el componente para NPC's");
            Utils.log(" &c- Versión incompatible: " + LobbyMC.getVersion());
            return;
        }
        plugin.getServer().getPluginManager().registerEvents(new PlayerListener(), plugin);
        Utils.log("Cargando componente para NPC'S v: " + LobbyMC.getVersion());
        PacketListenerCore.registerPackets(new PacketListeners(), LobbyMC.getInstance());

    }

    public static LobbyMC getPlugin() {
        return plugin;
    }

    public void startTaskNPC() {
        thread = executorService.scheduleWithFixedDelay(taskNPC, 0, 250, TimeUnit.MILLISECONDS);
    }

    public void cancel() {
        if (executorService != null) {
            if (thread != null && !thread.isCancelled()) {
                thread.cancel(true);
                thread = null;
            }
        }
    }

    /**
     * Create a new non-player character (NPC).
     *
     * @param text The text you want to sendShowPackets above the NPC (null = no text).
     * @return The NPC object you may use to sendShowPackets it to players.
     */
    public NPC createNPC(String name, List<String> text, UUID uuid, String nameNPC) {
        try {
            return (NPC) npcClass.getConstructors()[0].newInstance(this, name, text, uuid, nameNPC);
        } catch (Exception exception) {
            Utils.log("&cError al crear el NPC, reporta este mensaje de error: " + exception.getMessage());
        }

        return null;
    }

    /**
     * Create a new non-player character (NPC).
     *
     * @return The NPC object you may use to sendShowPackets it to players.
     */
    public NPC createNPC(String name, UUID uuid, String nameNPC) {
        return createNPC(name, null, uuid, nameNPC);
    }
}
