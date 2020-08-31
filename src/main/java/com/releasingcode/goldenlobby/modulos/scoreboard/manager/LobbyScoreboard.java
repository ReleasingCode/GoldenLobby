package com.releasingcode.goldenlobby.modulos.scoreboard.manager;


import com.releasingcode.goldenlobby.LobbyMC;
import com.releasingcode.goldenlobby.Utils;
import com.releasingcode.goldenlobby.connections.ServerManager;
import com.releasingcode.goldenlobby.extendido.scoreboard.Sidebar;
import com.releasingcode.goldenlobby.managers.LobbyPlayer;
import com.releasingcode.goldenlobby.managers.LobbyPlayerMap;
import com.releasingcode.goldenlobby.managers.indexing.LobbyPlayerIndexing;
import com.releasingcode.goldenlobby.modulos.npcserver.NPCServerPlugin;
import com.releasingcode.goldenlobby.modulos.scoreboard.ScoreboardPlugin;
import com.releasingcode.goldenlobby.npc.api.state.NPCMode;
import com.releasingcode.goldenlobby.npc.internal.NPCManager;
import es.minecub.core.exceptions.CoreException;
import es.minecub.core.ranks.RanksCore;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LobbyScoreboard extends Sidebar {
    private final boolean title_animated, animatable_contents;
    private final int tick_title;
    private final int tick_contents;
    private final NPCServerPlugin npcServerPlugin;
    ScoreboardPlugin instance;
    FileConfiguration configuration;
    ArrayList<String> contents;
    ArrayList<String> title;
    private ContentsAnimatable contentsAnimatable;

    public LobbyScoreboard(ScoreboardPlugin instance) {
        configuration = instance.getScoreboardConfig().getConfig();
        npcServerPlugin = NPCServerPlugin.getInstance();
        this.instance = instance;
        this.setAsync(configuration.getBoolean("scoreboard-async"));
        tick_title = configuration.getInt("tick-updater.title", 10);
        tick_contents = configuration.getInt("tick-updater.contents", 15);
        contents = configuration.isSet("contents") ? new ArrayList<>(
                Arrays.asList(configuration.getString("contents").split("\\n"))) : new ArrayList<>();
        title = configuration.isSet("title") ? new ArrayList<>(
                Arrays.asList(configuration.getString("title").split("\\n"))) : new ArrayList<>();
        title_animated = configuration.getBoolean("animatable.title", false);
        animatable_contents = configuration.getBoolean("animatable.contents", false);
        if (animatable_contents) {
            contentsAnimatable = new ContentsAnimatable(configuration);
        }
    }

    /**
     * @param player objeto al que pertenece el scoreboard
     * @return Una lista que representa cada linea de la configuración
     */
    @Override
    public List<String> getContent(Player player) {
        LobbyPlayer lobbyPlayer = LobbyPlayerMap.getJugador(player);
        if (lobbyPlayer == null) {
            return null;
        }
        if (!animatable_contents) {
            LobbyPlayerIndexing indexing = lobbyPlayer.getScoreboard().getIndexing();
            int changeOn = indexing.getAndIncrementIndexAtExtra("scoreboard_contents_tick", (tick_contents + 1));
            if (changeOn == tick_contents) {
                return this.contents.stream().map(cadaLinea
                        -> evaluateVariables(cadaLinea, lobbyPlayer))
                        .collect(Collectors.toCollection(ArrayList::new));
            }
        } else {
            return lobbyPlayer.getScoreboard().hotEvaluate(contentsAnimatable, this);
        }
        return new ArrayList<>();
    }

    public String evaluateVariables(String text, LobbyPlayer lobbyPlayer) {
        try {
            if (text == null) {
                return "";
            }
            Player player = lobbyPlayer.getPlayer();
            if (player == null) {
                return "";
            }
            text = ChatColor.translateAlternateColorCodes('&', text);
            if (LobbyMC.getInstance().isPlaceHolderAPI()) {
                text = PlaceholderAPI.setPlaceholders(player, text);
            }
            String rank = Utils.chatColor(RanksCore.getPlayerRank(player).getPriority() < 1000 ? RanksCore.getPlayerRank(player).getChatPrefix() : ChatColor.GREEN + "Usuario");
            return ServerManager.translateVar(text.replace("{staff_found}",
                    (lobbyPlayer.getLobbyStaffFound() == null) ? "Cargando..." : lobbyPlayer.getLobbyStaffFound()
                            .found() + "")
                    .replace("{total_staff}", NPCManager.getAllNPCs(NPCMode.STAFF).size() + "")
                    .replace("{progress_history}", (npcServerPlugin != null ?
                            "" + lobbyPlayer.historyPercent(npcServerPlugin.getHistoryManager().totalHistory())
                            : "0"))
                    .replace("{found_history}", (npcServerPlugin != null ?
                            "" + lobbyPlayer.historyFound()
                            : "0"))
                    .replace("{total_history}",
                            (npcServerPlugin != null ? npcServerPlugin.getHistoryManager().totalHistory() + "" : ""))
                    .replace("{objective_history}", (npcServerPlugin != null ? lobbyPlayer.getHistory().getObjective()
                            != null ? lobbyPlayer.getHistory().getObjective() : "Ninguno" : "-"))
                    .replace("{colored_rank}", rank) //( solo version final )
                    .replace("{player}", lobbyPlayer.getName()), null);
        } catch (Exception e) {

        }
        return "";
    }


    @Override
    public String getTitle(Player player) {
        LobbyPlayer lobbyPlayer = null;
        try {
            lobbyPlayer = LobbyPlayerMap.getJugador(player);
        } catch (CoreException ignored) {
        }

        if (lobbyPlayer == null) return null;

        LobbyPlayerIndexing indexing = lobbyPlayer.getScoreboard().getIndexing();
        int changeOn = indexing.getAndIncrementIndexAtExtra("scoreboard_title_tick", (tick_title + 1));
        if (changeOn == tick_title) {
            int index = !title_animated ? 0 : indexing.getAndIncrementIndexAtExtra("scoreboard_title", title.size());
            String controlTextNull = !title_animated ? (title.size() > 0 ? title.get(0) : "") : title.get(index);
            if (controlTextNull == null) {
                controlTextNull = "";
            }
            return ChatColor.translateAlternateColorCodes('&', controlTextNull);
        }
        return null;
    }


}
