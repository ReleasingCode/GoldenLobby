package com.releasingcode.goldenlobby.loader;

import com.releasingcode.goldenlobby.Utils;
import org.bukkit.plugin.java.JavaPlugin;

public class LobbyMCPlugin extends JavaPlugin {
    @Override
    public void onLoad() {
        ZLib.Inicializador(this);
    }

    public <T extends LobbyComponente> T cargarComponente(Class<T> componentClass) {
        try {
            return ZLib.cargarComponente(componentClass);
        } catch (Exception e) {
            Utils.log("No se ha podido cargar la clase: " + componentClass.getName());
            e.printStackTrace();
        }
        return null;
    }

}
