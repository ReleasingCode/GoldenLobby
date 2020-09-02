package com.releasingcode.goldenlobby.modulos.spawn;

import com.releasingcode.goldenlobby.Utils;
import com.releasingcode.goldenlobby.loader.LobbyComponente;

public class SpawnPlugin extends LobbyComponente {

    @Override
    protected void onEnable() {
        Utils.log(" - Loading module Spawn");
        new SpawnCommand().register();
    }

    @Override
    protected void onDisable() {
        Utils.log(" - Disabling módule Spawn");
    }

}
