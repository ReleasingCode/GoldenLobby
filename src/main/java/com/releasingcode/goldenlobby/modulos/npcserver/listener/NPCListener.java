package com.releasingcode.goldenlobby.modulos.npcserver.listener;

import com.releasingcode.goldenlobby.Utils;
import com.releasingcode.goldenlobby.call.CallBack;
import com.releasingcode.goldenlobby.languages.Lang;
import com.releasingcode.goldenlobby.listeners.custom.SecurePlayerJoinEvent;
import com.releasingcode.goldenlobby.managers.LobbyPlayer;
import com.releasingcode.goldenlobby.managers.LobbyPlayerMap;
import com.releasingcode.goldenlobby.managers.history.LobbyPlayerHistory;
import com.releasingcode.goldenlobby.modulos.cooldown.object.CooldownSystem;
import com.releasingcode.goldenlobby.modulos.npcserver.NPCServerPlugin;
import com.releasingcode.goldenlobby.modulos.npcserver.db.history.NPCHistoryPlayerNetwork;
import com.releasingcode.goldenlobby.modulos.npcserver.history.NPCHistory;
import com.releasingcode.goldenlobby.modulos.npcserver.object.LobbyPlayerBuilder;
import com.releasingcode.goldenlobby.modulos.npcserver.object.LobbyStaffFound;
import com.releasingcode.goldenlobby.npc.api.NPC;
import com.releasingcode.goldenlobby.npc.api.events.NPCInteractEvent;
import com.releasingcode.goldenlobby.npc.api.state.NPCSlot;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class NPCListener implements Listener {
    NPCServerPlugin npcServerPlugin;

    public NPCListener(NPCServerPlugin plugin) {
        npcServerPlugin = plugin;
        plugin.getPlugin().getServer().getPluginManager().registerEvents(this, plugin.getPlugin());
    }

    public void commonCMD(NPC npc, Player player) {
        npc.getCommand().forEach(comando -> {
            if (!comando.trim().isEmpty()) {
                Utils.evaluateCommand(comando, player);
            }
        });
    }

    public void rewardCMD(NPC npc, Player player) {
        npc.getRewardCommands().forEach(comandoRecompensa -> {
            if (!comandoRecompensa.trim().isEmpty()) {
                Utils.evaluateCommand(comandoRecompensa, player);
            }
        });
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onJoin(SecurePlayerJoinEvent event) {
        LobbyPlayer lobbyPlayer = event.getPlayer();
        npcServerPlugin.getHistoryDB().loadPlayer(lobbyPlayer.toStringUUID(),
                new CallBack.ReturnCallBack<NPCHistoryPlayerNetwork>() {
                    @Override
                    public void onSuccess(NPCHistoryPlayerNetwork callback) {
                        if (callback == null) { // sin datos, el jugador es nuevo
                            lobbyPlayer.setLobbyStaffFound(new LobbyStaffFound(""));
                            return;
                        }
                        lobbyPlayer.setLobbyStaffFound(new LobbyStaffFound(callback.staff_found));
                        if (callback.previus_target == null || callback.previus_target.trim().isEmpty()) {
                            callback.current_target = "";
                        }
                        if (callback.history_current_playing != null && callback.history_current_playing.trim()
                                .isEmpty()) {
                            callback.history_current_playing = null;
                        }
                        LobbyPlayerHistory update = new LobbyPlayerHistory(callback.history,
                                callback.current_target,
                                callback.previus_target);
                        update.setPlaying(false);
                        update.setCurrentPlaying(callback.history_current_playing);
                        update.setPlayedMessage(callback.history_dash);
                        lobbyPlayer.setHistory(update);
                        String actualObjective = npcServerPlugin.getHistoryManager()
                                .getObjectiveByActualTarget(callback.current_target);
                        lobbyPlayer.getHistory().setObjective(actualObjective);
                        npcServerPlugin.getHistoryDB().setPlayerRedis(lobbyPlayer);
                    }

                    @Override
                    public void onError(NPCHistoryPlayerNetwork callback) {
                        lobbyPlayer.sendMessage(
                                Lang.NO_LOAD_Statistics.toString());
                    }
                });
 /*       npcServerPlugin.getStaffStatsDB().loadPlayer(lobbyPlayer.toStringUUID(), player.getName(), new CallBack.ReturnCallBack<String>() {
            @Override
            public void onSuccess(String callback) {
                LobbyStaffFound stats = new LobbyStaffFound(callback);
                lobbyPlayer.setLobbyStaffFound(stats);
                npcServerPlugin.getStaffStatsDB().setPlayerRedis(lobbyPlayer.toStringUUID(), stats.allNPCs(), player.getName());
            }

            @Override
            public void onError(String callback) {
                lobbyPlayer.sendMessage("&cNo se ha podido cargar tus estadisticas, comunicalo a un administrador");
            }
        });*/
    }

    @EventHandler
    public void onInteract(NPCInteractEvent event) {
        Player player = event.getWhoClicked();
        LobbyPlayer lobbyPlayer = LobbyPlayerMap.getJugador(player);
        NPC npc = event.getNPC();
        if (lobbyPlayer.getNpcBuilder().isEditing()) {
            if (lobbyPlayer.getNpcBuilder().getNpc().equals(npc)) {
                ItemStack stack = player.getItemInHand();
                if (stack != null) {
                    if (!player.isSneaking()) {
                        NPCSlot slot = NPCSlot.NPCSlotByItemStack(stack);
                        ItemStack stackHash = npc.getItem(slot);
                        if (stackHash != null && !stackHash.getType().equals(Material.AIR)) {
                            player.getLocation().getWorld().dropItem(player.getLocation(), stackHash);
                        }
                        npc.setItem(slot, stack);
                        lobbyPlayer.sendMessage(
                                "&aYou have established:&2 " + slot.name() + " &a-> &e" + stack.getType().name());
                        return;
                    }
                    NPCSlot slot = NPCSlot.NPCSlotByItemStack(stack);
                    ItemStack stackHash = npc.getItem(slot);
                    if (stackHash != null && !stackHash.getType().equals(Material.AIR)) {
                        player.getLocation().getWorld().dropItem(player.getLocation(), stackHash);
                    }
                    npc.setItem(slot, new ItemStack(Material.AIR));
                }
                return;
            }
            lobbyPlayer.sendMessage(Lang.COMMAND_ENABLE.toString());
            return;
        }
        if (!npc.isReady() || npc.isEditing()) {
            lobbyPlayer.sendMessage(Lang.NPC_NOT_READY_CLICKED.toString());
            return;
        }

        LobbyPlayerBuilder builderNPC = lobbyPlayer.getNpcBuilder();
        if (event.getClickType().equals(NPCInteractEvent.ClickType.LEFT_CLICK)) {
            if (builderNPC.isSelecting()) {
                builderNPC.setEditing(event.getNPC().getName());
                builderNPC.setSelectingNpc(false);
                builderNPC.getNpc().setEditing(true);
                builderNPC.setCommands(builderNPC.getNpc().getCommand());
                builderNPC.setRewardCommands(builderNPC.getNpc().getRewardCommands());
                builderNPC.setHologram(builderNPC.getNpc().getText());
                lobbyPlayer.sendMessage("&aYou have selected the NPC: &e" + event.getNPC().getName());
                return;
            }
        }
        if (lobbyPlayer.getLobbyStaffFound() == null) {
            lobbyPlayer.sendMessage(
                    Lang.NO_LOAD_Statistics.toString());
            return;
        }
        switch (npc.getNPCMode()) {
            case HISTORY: {
                LobbyPlayerHistory history = lobbyPlayer.getHistory();
                if (!history.isPlaying()) {
                    if (npc.getHistory() != null) {
                        NPCHistory npcHistory = npc.getHistory();
                        if (npc.getUid().equals(history.getPreviusTarget())) {
                            String msg = npc.getHistory().getRandomInProgressMessage();
                            lobbyPlayer.sendMessage(msg);
                            return;
                        }
                        boolean contieneUnaConversacionNoTerminada = history.getCurrentPlaying() != null && npc.getUid()
                                .equals(history.getCurrentPlaying());

                        if ((history.isNullActualTarget() && npcHistory
                                .getRequired()
                                .isEmpty()) || npc.getUid().equals(history.getActualTarget()) || npcHistory
                                .getRequired()
                                .isEmpty() || contieneUnaConversacionNoTerminada) {
                            if (history.addNextHistory(npc) || contieneUnaConversacionNoTerminada) {
                                history.setCurrentPlaying(npc.getUid());
                                npcServerPlugin.getHistoryDB().savePlayer(lobbyPlayer, new CallBack.SingleCallBack() {
                                    @Override
                                    public void onSuccess() {
                                        npc.getHistory().play(lobbyPlayer);
                                    }

                                    @Override
                                    public void onError() {
                                        history.setCurrentPlaying(null);
                                        history.removeSet(npc.getUid());
                                    }
                                }, false);
                            } else {
                                String msg = npc.getHistory().getRandomRequiredMessages();
                                lobbyPlayer.sendMessage(msg);
                            }
                            return;
                        }
                        //entonces requiere de un antecedente
                        String msg = npc.getHistory().getRandomRequiredMessages();
                        lobbyPlayer.sendMessage(msg);
                    }
                }
                break;
            }
            case COMMAND: {
                if (npc.getCooldownValidator() != null
                        && !npc.getCooldownValidator().trim().isEmpty()) {
                    CooldownSystem system = CooldownSystem.getCooldown(npc.getCooldownValidator());
                    if (system != null
                            && !system.isFinished()) {
                        return;
                    }
                }
                commonCMD(npc, player);
                break;
            }
            case STAFF: {


                /*
                 *   Evaluar cuando el jugador ya no ha encontrado el npc antes
                 *   y ejecutar comandos de recompensa
                 */
                if (npc.getRewardCommands().isEmpty()) {
                    lobbyPlayer.sendMessage(
                            Lang.NPC_NOT_READYSTAFF.toString());
                    return;
                }
                if (lobbyPlayer.getLobbyStaffFound().addNPC(npc)) {
                    npcServerPlugin.getHistoryDB().savePlayer(lobbyPlayer, new CallBack.SingleCallBack() {
                        @Override
                        public void onSuccess() {
                            rewardCMD(npc, player);
                            commonCMD(npc, player);
                        }

                        @Override
                        public void onError() {
                            lobbyPlayer.getLobbyStaffFound().removeSet(npc.getUid());
                            lobbyPlayer.sendMessage(
                                    Lang.ERROR_INTERACTION_REGISTER.toString());
                        }
                    }, false);
                    return;
                }
                /*
                 * Ejecutar comandos comunes
                 *
                 */
                commonCMD(npc, player);
                break;
            }
        }
    }
}
