package com.releasingcode.goldenlobby.modulos.spawn;

import com.releasingcode.goldenlobby.Utils;
import com.releasingcode.goldenlobby.loader.LobbyComponente;

public class SpawnPlugin extends LobbyComponente {

    @Override
    protected void onEnable() {
        Utils.log(" - Cargando módulo Spawn");
        new SpawnCommand().register();
    }

    @Override
    protected void onDisable() {
        Utils.log(" - Inhabilitando módulo Spawn");
    }

}
