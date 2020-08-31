package com.releasingcode.goldenlobby.modulos.scoreboard.manager;


import com.releasingcode.goldenlobby.LobbyMC;
import com.releasingcode.goldenlobby.extendido.scoreboard.Sidebar;
import com.releasingcode.goldenlobby.managers.LobbyPlayer;
import com.releasingcode.goldenlobby.managers.LobbyPlayerMap;
import com.releasingcode.goldenlobby.modulos.scoreboard.ScoreboardPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import java.util.UUID;


public class ScoreboardManager {
    private final Scoreboard sb;
    private final Sidebar sidebar;

    public ScoreboardManager(ScoreboardPlugin instance) {
        this.sb = Bukkit.getServer().getScoreboardManager().getNewScoreboard();
        this.sidebar = new LobbyScoreboard(instance);
        for (Player player2 : Bukkit.getOnlinePlayers()) {
            this.sidebar.addRecipient(player2);
        }
        this.sidebar.runAutoRefresh(true);
    }

    public void clear() {
        for (UUID recipient : sidebar.getRecipients()) {
            Player player = Bukkit.getPlayer(recipient);
            LobbyPlayer lp = LobbyPlayerMap.getJugador(player);
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            lp.getScoreboard().reset();
        }
        sidebar.cancel();
        sidebar.getRecipients().forEach(sidebar::removeRecipient);
    }

    public void setScoreboardForPlayer(Player p) {
        Bukkit.getScheduler().runTask(LobbyMC.getInstance(), () -> {
            p.setScoreboard(sb);
            this.sidebar.addRecipient(p);
        });
    }

    public Scoreboard getScoreboard() {
        return this.sb;
    }

    public void removeScoreboard(Player p) {
        this.sidebar.removeRecipient(p);
    }
}
