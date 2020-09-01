package com.releasingcode.goldenlobby.managers.scoreboard;


import com.releasingcode.goldenlobby.LobbyMC;
import com.releasingcode.goldenlobby.managers.LobbyPlayer;
import org.bukkit.entity.Player;
import com.releasingcode.goldenlobby.managers.indexing.LobbyPlayerIndexing;
import com.releasingcode.goldenlobby.modulos.scoreboard.manager.ContentsAnimatable;
import com.releasingcode.goldenlobby.modulos.scoreboard.manager.LobbyScoreboard;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class LobbyPlayerScoreboard {
    private final LobbyPlayer lobbyPlayer;
    private LinkedHashMap<String, String> text;
    private LobbyPlayerIndexing indexing;
    private String minecubos;

    public LobbyPlayerScoreboard(LobbyPlayer lobbyPlayer) {
        this.lobbyPlayer = lobbyPlayer;
        indexing = new LobbyPlayerIndexing();
        text = new LinkedHashMap<>();
        Player player = lobbyPlayer.getPlayer();
        if (player != null && player.isOnline()) {
            minecubos = LobbyMC.getInstance().getVaultAPI().getEconPlayer(player) + "";
        }
    }

    public void reset() {
        text = new LinkedHashMap<>();
        resetIndexing();
    }

    public void resetIndexing() {
        indexing = new LobbyPlayerIndexing();
    }

    public List<String> hotEvaluate(ContentsAnimatable animatable, LobbyScoreboard scoreboard) {
        if (text.isEmpty()) {
            animatable.getTexts().forEach(content -> {
                int minecubosTick = indexing.getAndIncrementIndexAtExtra("sc_contents_minecubos" + content.getKey().toLowerCase(), 41);
                int tick = indexing.getAndIncrementIndexAtExtra("sc_contents_tick_" + content.getKey().toLowerCase(),
                        (content.getTick() + 1));
                int nextIndex = indexing.getAndIncrementIndexAtExtraCondition("sc_contents_text_" +
                        content.getKey().toLowerCase(), content.getText().size(), tick >= content.getTick());
                String evaluater = scoreboard.evaluateVariables(content.getText().get(nextIndex), lobbyPlayer);
                Player player = lobbyPlayer.getPlayer();
                if (player != null && player.isOnline()) {
                    if (minecubosTick >= 40) {
                        minecubos = LobbyMC.getInstance().getVaultAPI().getEconPlayer(player) + "";
                    }
                    evaluater = evaluater.replace("{minecubos}", minecubos);
                }
                text.put(content.getKey(), evaluater);
            });
        } else {
            animatable.getTexts().forEach(content -> {
                int minecubosTick = indexing.getAndIncrementIndexAtExtra("sc_contents_minecubos" + content.getKey().toLowerCase(), 41);
                int tick = indexing.getAndIncrementIndexAtExtra("sc_contents_tick_" + content.getKey().toLowerCase(),
                        (content.getTick() + 1));
                int nextIndex = indexing.getAndIncrementIndexAtExtraCondition("sc_contents_text_" +
                        content.getKey().toLowerCase(), content.getText().size(), tick >= content.getTick());
                if (content.getTick() > 0 && tick >= content.getTick()) {
                    try {
                        String evaluater = scoreboard.evaluateVariables(content.getText().get(nextIndex),
                                lobbyPlayer);
                        Player player = lobbyPlayer.getPlayer();
                        if (player != null && player.isOnline()) {
                            if (minecubosTick >= 40) {
                                minecubos = LobbyMC.getInstance().getVaultAPI().getEconPlayer(player) + "";
                            }
                            evaluater = evaluater.replace("{minecubos}", minecubos);
                        }
                        text.put(content.getKey(), evaluater);
                    } catch (Exception e) {
                    }
                }
            });
        }
        return new ArrayList<>(text.values());
    }

    public LobbyPlayerIndexing getIndexing() {
        return indexing;
    }
}
