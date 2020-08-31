package com.releasingcode.goldenlobby.listeners;

import com.releasingcode.goldenlobby.LobbyMC;
import com.releasingcode.goldenlobby.managers.DelayPlayer;
import com.releasingcode.goldenlobby.managers.LobbyPlayer;
import com.releasingcode.goldenlobby.managers.LobbyPlayerMap;
import es.minecub.core.callbacks.Callback;
import es.minecub.core.utils.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.metadata.FixedMetadataValue;
import com.releasingcode.goldenlobby.listeners.custom.SecurePlayerJoinEvent;

public class OnJoin implements Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent evento) {
        evento.setJoinMessage(null);
        Player player = evento.getPlayer();
        DelayPlayer.addDelay(player, "show_npc", 1010);
        PlayerUtils.getAccountId(player, new Callback.ReturnCall<Integer>() {
            @Override
            public void success(Integer integer) {
                LobbyPlayerMap.addJugador(player, integer);
                LobbyPlayer lobbyPlayer = LobbyPlayerMap.getJugador(player);
                player.setGameMode(GameMode.SURVIVAL);
                player.setAllowFlight(true);
                if (player.hasPermission("lobbymc.benefits.fly")) {
                    player.setFlying(true);
                    lobbyPlayer.setFlyer(true);
                    player.setMetadata("userFlying", new FixedMetadataValue(LobbyMC.getInstance(), null));
                }
                if (!evento.getPlayer().hasPermission("lobbymc.gamemode")) {
                    lobbyPlayer.setGameMode(null);
                }
                SecurePlayerJoinEvent event = new SecurePlayerJoinEvent(lobbyPlayer);
                Bukkit.getServer().getPluginManager().callEvent(event);
            }

            @Override
            public void error(Exception e) {
                Bukkit.getScheduler().runTask(LobbyMC.getInstance(), () -> {
                    evento.getPlayer().kickPlayer("No se ha podido obtener tus datos comunicate con un administrador");
                });
            }
        });
    }
}
