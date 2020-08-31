package com.releasingcode.goldenlobby.managers;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LobbyPlayerMap {
    private static final Map<String, LobbyPlayer> jugadores = new ConcurrentHashMap<>();

    /*
     1.- Añadir al jugador a la memoria del HashMap - Concurrente
     2.- Obtenerlo por UUID el Objeto LobbyPlayer
     */
    public static LobbyPlayer addJugador(Player player, int playerId) {
        return jugadores.putIfAbsent(player.getName(), new LobbyPlayer(player, playerId));
    }

    public static LobbyPlayer getJugador(Player player) {
        return jugadores.getOrDefault(player.getName(), null);
    }

    public static void removeJugador(Player player) {
        jugadores.remove(player.getName());
    }

    public static List<LobbyPlayer> getPlayers() {
        return new ArrayList<>(jugadores.values());
    }
}
