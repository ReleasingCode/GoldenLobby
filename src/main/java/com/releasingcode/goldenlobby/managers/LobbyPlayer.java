package com.releasingcode.goldenlobby.managers;

import com.releasingcode.goldenlobby.Utils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import com.releasingcode.goldenlobby.managers.cooldown.LobbyCooldown;
import com.releasingcode.goldenlobby.managers.history.LobbyPlayerHistory;
import com.releasingcode.goldenlobby.managers.indexing.LobbyPlayerIndexing;
import com.releasingcode.goldenlobby.managers.inventarios.LobbyPlayerInventory;
import com.releasingcode.goldenlobby.managers.scoreboard.LobbyPlayerScoreboard;
import com.releasingcode.goldenlobby.modulos.npcserver.object.LobbyPlayerBuilder;
import com.releasingcode.goldenlobby.modulos.npcserver.object.LobbyStaffFound;
import com.releasingcode.goldenlobby.modulos.regions.cuboid.StageOfCreation;

import java.util.List;
import java.util.UUID;

public class LobbyPlayer {

    private final UUID uuid;
    private final int id;
    private final LobbyPlayerInventory inventory;
    private final LobbyPlayerBuilder npcBuilder;
    private final LobbyPlayerScoreboard lobbyplayerscoreboard;
    private final LobbyPlayerIndexing globalIndexing;
    private int kills;
    private String name;
    private LobbyStaffFound lobbyStaffFound;
    private LobbyPlayerHistory history;
    private boolean flyer;
    private GameMode gameMode;
    private StageOfCreation.Regions actualRegion = StageOfCreation.Regions.LOBBY;
    private LobbyCooldown lobbyCooldown;

    LobbyPlayer(Player player, int id) {
        this.name = player.getName();
        this.uuid = player.getUniqueId();
        this.id = id;
        kills = 0;
        inventory = new LobbyPlayerInventory();
        npcBuilder = new LobbyPlayerBuilder();
        lobbyCooldown = new LobbyCooldown(null);
        history = new LobbyPlayerHistory(null, null, null);
        globalIndexing = new LobbyPlayerIndexing();
        flyer = getFlyer();
        gameMode = null;
        lobbyplayerscoreboard = new LobbyPlayerScoreboard(this);
    }

    public LobbyCooldown getLobbyCooldown() {
        return lobbyCooldown;
    }

    public LobbyPlayerHistory getHistory() {
        return history;
    }

    public void setHistory(LobbyPlayerHistory history) {
        this.history = history;
    }

    public LobbyPlayerIndexing getGlobalIndexing() {
        return globalIndexing;
    }

    public LobbyPlayerInventory getInventoryManager() {
        return inventory;
    }

    public LobbyStaffFound getLobbyStaffFound() {
        return lobbyStaffFound;
    }

    public void setLobbyStaffFound(LobbyStaffFound lobbyStaffFound) {
        this.lobbyStaffFound = lobbyStaffFound;
    }

    public LobbyPlayerBuilder getNpcBuilder() {
        return npcBuilder;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(name);
    }

    public void incrementKills() {
        kills++;
    }

    public int getKills() {
        return kills;
    }

    public void sendMessage(List<String> sms) {
        for (String mensaje : sms) {
            sendMessage(mensaje);
        }
    }

    public void sendMessage(String... sms) {
        for (String mensaje : sms) {
            sendMessage(mensaje);
        }
    }

    public void sendMessageWithSuggest(MessageSuggest... messageSuggests) {
        for (MessageSuggest suggest : messageSuggests) {
            BaseComponent[] component
                    = TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', suggest.text));
            TextComponent tx = new TextComponent(component);
            tx.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, suggest.suggestcmd));
            tx.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', suggest.hoverText))));
            getPlayer().spigot().sendMessage(tx);
        }
    }

    public void sendMessage(String text) {
        Player p = getPlayer();
        if (p == null) {
            return;
        }
        p.sendMessage(Utils.chatColor(text).replace("{player}", getName()));
    }

    public UUID getUUID() {
        return uuid;
    }

    public String toStringUUID() {
        return id + "";
    }

    public boolean getFlyer() {
        return flyer;
    }

    public void setFlyer(boolean f) {
        this.flyer = f;
    }

    public void setGameMode(GameMode gameMode) {
        Player p = getPlayer();
        if (gameMode != null) {
            if (p != null) {
                this.gameMode = gameMode;
                p.setGameMode(gameMode);
            }
        } else {
            if (this.gameMode != null) {
                p.setGameMode(this.gameMode);
            }
        }
    }

    public LobbyPlayerScoreboard getScoreboard() {
        return lobbyplayerscoreboard;
    }

    public String historyFound() {
        return history.getRegistred().size() + "";
    }

    public String historyPercent(int total) {
        LobbyPlayerHistory history = getHistory();
        if (history != null) {
            if (total > 0) {
                return "" + (history.getRegistred().size() * 100) / total;
            }
        }
        return "" + 0;
    }

    public int getId() {
        return this.id;
    }

    public StageOfCreation.Regions getActualRegion() {
        return actualRegion;
    }

    public void setActualRegion(StageOfCreation.Regions actualRegion) {
        this.actualRegion = actualRegion;
    }

    public void setEditingCooldown(String toLowerCase) {
        lobbyCooldown = new LobbyCooldown(toLowerCase);
    }
}
