package com.releasingcode.goldenlobby.modulos.limbo;

import com.releasingcode.goldenlobby.Utils;
import com.releasingcode.goldenlobby.configuracion.CustomConfiguration;
import com.releasingcode.goldenlobby.loader.LobbyComponente;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LimboPlugin extends LobbyComponente {
    private final Map<String, Long> afkPlayers = new ConcurrentHashMap<>();
    private CustomConfiguration configuration;
    protected static int SECONDS_TO_LIMBO;

    @Override
    protected void onEnable() {
        configuration = new CustomConfiguration("limbo", getPlugin());
        if (!configuration.getConfig().getBoolean("enabled")) return;
        Utils.log(" - Cargando módulo de Limbo");
        SECONDS_TO_LIMBO = configuration.getConfig().getInt("seconds-afk-needed-to-send");
        getPlugin().getServer().getPluginManager().registerEvents(new AfkDetectorsEvent(this), getPlugin());
    }

    @Override
    protected void onDisable() {
        Utils.log(" - Inhabilitando módulo de Bloqueador de Comandos");
    }

    public Map<String, Long> getAfkPlayers() {
        return afkPlayers;
    }

    public CustomConfiguration getConfiguration() {
        return configuration;
    }

}

