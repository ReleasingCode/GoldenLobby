package com.releasingcode.goldenlobby.extendido.scoreboard;


import com.releasingcode.goldenlobby.extendido.nms.ObjectiveSender;
import com.releasingcode.goldenlobby.extendido.nms.SidebarObjective;
import com.releasingcode.goldenlobby.loader.PluginBootstrap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.*;


public abstract class Sidebar {


    // Asynchronously available list of the logged in players.


    private final Set<UUID> recipients = new CopyOnWriteArraySet<>();
    // Other cases (caso SidebarObjective por jugador)
    private final Map<UUID, SidebarObjective> objectives = new ConcurrentHashMap<>();
    private final ScheduledExecutorService thread = Executors.newScheduledThreadPool(1);
    private int lastLineScore = 1;
    private boolean automaticDeduplication = true;
    private boolean async = false;
    private ScheduledFuture<?> scheduledFuture;
    private long autoRefreshDelay = 0;

    // Only used if both titleMode and contentMode are global.
    private BukkitTask refreshTask = null;

    public static void init() {

        PluginBootstrap.registerEvents(new OnlinePlayersListener());
    }

    public static void exit() {

        ObjectiveSender.clearForAll();

    }

    public abstract List<String> getContent(final Player player);

    public abstract String getTitle(final Player player);

    public void preRender() {
    }

    public void postRender() {
    }

    public void setAsync(final boolean async) {
        this.async = async;
    }

    public void setAutoRefreshDelay(final long autoRefreshDelay) {
        this.autoRefreshDelay = autoRefreshDelay;
    }

    public void setLastLineScore(final int lastLineScore) {
        this.lastLineScore = Math.max(lastLineScore, 1);
    }

    public void setAutomaticDeduplication(boolean automaticDeduplication) {
        this.automaticDeduplication = automaticDeduplication;
    }

    public void addRecipient(final UUID id) {
        recipients.add(id);
    }

    public Set<UUID> getRecipients() {
        return recipients;
    }

    public void addRecipient(final Player player) {
        addRecipient(player.getUniqueId());
    }

    public void removeRecipient(final UUID id) {
        recipients.remove(id);
        objectives.remove(id);
        ObjectiveSender.clear(id);
    }

    public void removeRecipient(final Player player) {
        removeRecipient(player.getUniqueId());
    }

    public void refresh() {
        try {
            preRender();
            for (UUID id : recipients) {
                Player recipient = Bukkit.getPlayer(id);
                if (recipient != null && recipient.isOnline()) {
                    refresh(recipient);
                }
            }
            postRender();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void cancel() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
            scheduledFuture = null;
        }
    }

    public void runAutoRefresh(final boolean run) {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
            scheduledFuture = null;
        }
        if (run) {
            Runnable refreshRunnable = this::refresh;
            if (async) {
                scheduledFuture =
                        thread.scheduleAtFixedRate(refreshRunnable, 0, 35, TimeUnit.MILLISECONDS);
            } else {
                refreshTask = Bukkit.getScheduler().runTaskTimer(
                        PluginBootstrap.getPlugin(), refreshRunnable, 1, 0
                );
            }
        }
    }

    public void updateLine(Player player, String oldLine, String newLine) {
        // Necesitamos obtener el antiguo scoreboard.
        SidebarObjective objective = objectives.get(player.getUniqueId());
        Integer score = objective.getScores().get(oldLine);
        if (score == null)
            return;  // Nada que actualizar.

        // Actualizaremos la linea
        updateLine(objective, oldLine, newLine, score);
    }

    public void updateLine(Player player, int lineIndex, String newLine) {

        // We need the old score.
        SidebarObjective objective = objectives.get(player.getUniqueId());
        Integer biggestScore = -1;

        for (Integer score : objective.getScores().values())
            if (score > biggestScore)
                biggestScore = score;

        if (lineIndex >= biggestScore)
            return;

        Integer score = biggestScore - lineIndex;

        // Then we need the old line at this score.
        String oldLine = null;
        for (Map.Entry<String, Integer> scoreEntry : new HashMap<>(objective.getScores()).entrySet()) {
            if (scoreEntry.getValue().equals(score)) {
                oldLine = scoreEntry.getKey();
                break;
            }
        }

        if (oldLine == null)
            return;

        // Then we updates the line.
        updateLine(objective, oldLine, newLine, score);
    }

    private void refresh(final Player player) {
        String title = getTitle(player);
        List<String> content = getContent(player);
        final UUID playerID = player.getUniqueId();
        final boolean objectiveAlreadyExists = objectives.containsKey(playerID);
        if (title != null || content != null) {
            if (content != null || !objectiveAlreadyExists) {
                final SidebarObjective objective = constructObjective(title, content, Collections.singleton(playerID));
                if (objective != null) {
                    objectives.put(playerID, objective);
                    ObjectiveSender.send(objective);
                }
            } else {
                final SidebarObjective objective = objectives.get(playerID);
                objective.setDisplayName(title);
                ObjectiveSender.updateDisplayName(objective);
            }
        }
    }

    private SidebarObjective constructObjective(final String title,
                                                final List<String> content,
                                                Set<UUID> receivers) {
        if (title == null || content == null) {
            return null;
        }
        SidebarObjective objective = new SidebarObjective(title);
        // The score of the first line
        int score = lastLineScore + content.size() - 1;
        // The deduplication stuff
        Set<String> usedLines = new HashSet<>();

        // The current number of spaces used to create blank lines
        int spacesInBlankLines = 0;

        for (String line : content) {
            // The blank lines are always deduplicated
            if (line.isEmpty()) {
                StringBuilder lineBuilder = new StringBuilder(line);
                for (int i = 0; i < spacesInBlankLines; i++)
                    lineBuilder.append(" ");
                line = lineBuilder.toString();

                spacesInBlankLines++;
            }

            // If the deduplication is enabled, we add spaces until the line is unique.
            else if (automaticDeduplication) {
                String rawLine = line;

                // The deduplication string used.
                // We try to use the Minecraft formatting codes. If we can't find an unique
                // string with the first one, we try with the next, and so one.
                // This is not used for empty line because a sidebar with 40 empty lines is
                // not a well-designed sidebar, as the sidebar can't display this amount of
                // lines. If this is a problem for you, fill a bug report.
                Character deduplicationChar = '0';

                while (usedLines.contains(line)) {
                    if (line.length() + 2 > SidebarObjective.MAX_LENGTH_SCORE_NAME && deduplicationChar != null) {
                        deduplicationChar++;

                        if (deduplicationChar > 'f')
                            deduplicationChar = null;

                        else if (deduplicationChar > '9')
                            deduplicationChar = 'a';

                        line = rawLine;
                    }

                    line += deduplicationChar != null ? ChatColor.COLOR_CHAR + "" + deduplicationChar : " ";
                }

                usedLines.add(line);
            }

            objective.setScore(line, score);
            score--;
        }

        for (UUID receiver : receivers) {
            objective.addReceiver(receiver);
        }

        return objective;
    }

    private void updateLine(SidebarObjective objective, String oldLine, String newLine, int score) {
        // First: we check if there is something to do.
        // This may seems strange, but without this check, if a line is replaced by itself (no change),
        // the line will disappears (because the new line is sent before the destroy packet of the
        // old one), and even without that, this avoids useless packets to be sent.
        if (oldLine.equals(newLine))
            return;

        // We send the line change packets
        ObjectiveSender.updateLine(objective, oldLine, newLine, score);

        // ...and we update the objective.
        objective.removeScore(oldLine);
        objective.setScore(newLine, score);
    }
}
