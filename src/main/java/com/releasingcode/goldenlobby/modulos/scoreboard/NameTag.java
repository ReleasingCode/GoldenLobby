package com.releasingcode.goldenlobby.modulos.scoreboard;

import com.releasingcode.goldenlobby.Utils;
import es.minecub.core.ranks.RanksCore;
import es.minecub.core.utils.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class NameTag {

    private final ScoreboardPlugin plugin;
    ScheduledExecutorService service;
    ScheduledFuture<?> thread;

    public NameTag(ScoreboardPlugin plugin) {
        this.plugin = plugin;
        service = Executors.newScheduledThreadPool(1);
    }

    public void cancel() {
        if (thread != null) {
            thread.cancel(true);
        }
    }

    public void start() {
        Runnable runnable = () -> {
            Scoreboard board = plugin.getScoreboardManager().getScoreboard();
            if (board == null) {
                return;
            }
            for (Player player : Bukkit.getOnlinePlayers()) {
                try {
                    boolean hasDisguise = PlayerUtils.containsMapKeyObject(player.getName(), "disguiseName");
                    String disguiseName = (String) PlayerUtils.getObjectMapKey(player.getName(), "disguiseName");
                    if (hasDisguise) {
                        Team teamBoard = getTeam(board, disguiseName);
                        setPrefixTeam(player, teamBoard, true);
                    } else {
                        Team teamBoard = getTeam(board, player.getName());
                        setPrefixTeam(player, teamBoard, false);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        };
        thread = service.scheduleAtFixedRate(runnable, 0, 500, TimeUnit.MILLISECONDS);
    }

    public Team getTeam(Scoreboard board, String name) {
        Team team;
        if (board.getTeam(name) != null) {
            team = board.getTeam(name);
            if (!containEntry(team.getEntries(), name)) {
                team.addEntry(name);
            }
        } else {
            team = board.registerNewTeam(name);
            team.addEntry(name);
            //team.addEntry(p.getName());
        }
        return team;
    }

    public boolean containEntry(Set<String> entries, String name) {
        for (String key : entries) {
            if (key.equals(name)) {
                return true;
            }
        }
        return false;
    }

    public void setPrefixTeam(Player p, Team team, boolean hasDisguise) {
        if (p.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
            if ((team.getNameTagVisibility() != NameTagVisibility.NEVER)) {
                team.setNameTagVisibility(NameTagVisibility.NEVER);
            }
            return;
        }
        if ((team.getNameTagVisibility() != NameTagVisibility.ALWAYS)) {
            team.setNameTagVisibility(NameTagVisibility.ALWAYS);
        }
        String chatPrefix = hasDisguise ? Utils.chatColor("&a&lVIP.E &r&a") : Utils.chatColor(RanksCore.getPlayerRank(p)
                .getChatPrefix());
        String prefix = chatPrefix.indexOf(" ") > 0
                ? chatPrefix.substring(0, chatPrefix.indexOf(" ")).trim() + " " : "";
        if (team.getNameTagVisibility() != NameTagVisibility.ALWAYS) {
            team.setNameTagVisibility(NameTagVisibility.ALWAYS);
            team.setCanSeeFriendlyInvisibles(false);
        }
        if (prefix.length() > 15) {
            prefix = prefix.substring(0, 14);
        }
        try {
            team.setPrefix(prefix + ChatColor.RESET);
        } catch (Exception e) {
            prefix = prefix.substring(0, 14);
            team.setPrefix(prefix + ChatColor.RESET);
        }
    }
}
