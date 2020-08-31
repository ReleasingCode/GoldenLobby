package com.releasingcode.goldenlobby.modulos.gamemode;

import com.releasingcode.goldenlobby.Utils;
import com.releasingcode.goldenlobby.loader.LobbyComponente;
import com.releasingcode.goldenlobby.modulos.gamemode.comandos.GamemodeCommand;

public class GameModePlugin extends LobbyComponente {

    @Override
    protected void onEnable() {
        Utils.log(" - Cargando modulo de GameMode");
        new GamemodeCommand().register();
        //getPlugin().getServer().getPluginManager().registerEvents(new onJoin(), getPlugin());
        ///
        //mis configuracion
    }

    @Override
    protected void onDisable() {
        Utils.log(" - Desabilitando modulo de GameMode");

    }
}
