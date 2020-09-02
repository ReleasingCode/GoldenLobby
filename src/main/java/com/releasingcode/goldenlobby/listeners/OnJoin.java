package com.releasingcode.goldenlobby.listeners;

import com.releasingcode.goldenlobby.GoldenLobby;
import com.releasingcode.goldenlobby.listeners.custom.SecurePlayerJoinEvent;
import com.releasingcode.goldenlobby.managers.DelayPlayer;
import com.releasingcode.goldenlobby.managers.LobbyPlayer;
import com.releasingcode.goldenlobby.managers.LobbyPlayerMap;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class OnJoin implements Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent evento) {
        evento.setJoinMessage(null);
        Player player = evento.getPlayer();
        DelayPlayer.addDelay(player, "show_npc", 1010);
        LobbyPlayerMap.addJugador(player);
        LobbyPlayer lobbyPlayer = LobbyPlayerMap.getJugador(player);
        player.setGameMode(GameMode.SURVIVAL);
        player.setAllowFlight(true);
        if (player.hasPermission("goldenlobby.benefits.fly")) {
            player.setFlying(true);
            lobbyPlayer.setFlyer(true);
            player.setMetadata("userFlying", new FixedMetadataValue(GoldenLobby.getInstance(), null));
        }
        if (!evento.getPlayer().hasPermission("goldenlobby.gamemode")) {
            lobbyPlayer.setGameMode(null);
        }
        SecurePlayerJoinEvent event = new SecurePlayerJoinEvent(lobbyPlayer);
        Bukkit.getServer().getPluginManager().callEvent(event);
    }
}
