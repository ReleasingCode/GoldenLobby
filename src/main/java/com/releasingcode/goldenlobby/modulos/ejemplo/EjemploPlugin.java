package com.releasingcode.goldenlobby.modulos.ejemplo;

import com.releasingcode.goldenlobby.Utils;
import com.releasingcode.goldenlobby.loader.LobbyComponente;

public class EjemploPlugin extends LobbyComponente {
    @Override
    protected void onEnable() {

        Utils.log("EjemploPlugin", "se ha inicializado");
    }

    @Override
    protected void onDisable() {


    }
}
