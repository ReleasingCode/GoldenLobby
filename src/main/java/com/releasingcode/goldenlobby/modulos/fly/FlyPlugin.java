package com.releasingcode.goldenlobby.modulos.fly;

import com.releasingcode.goldenlobby.Utils;
import com.releasingcode.goldenlobby.loader.LobbyComponente;
import com.releasingcode.goldenlobby.modulos.fly.comandos.FlyCommands;

public class FlyPlugin extends LobbyComponente {

    @Override
    protected void onEnable() {
        Utils.log(" - Loading module Fly");
        new FlyCommands().register();
    }

    @Override
    protected void onDisable() {
        Utils.log(" - Disabling module Fly");
    }

}
