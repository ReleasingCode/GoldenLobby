package com.releasingcode.goldenlobby.loader;

import com.releasingcode.goldenlobby.Utils;
import org.bukkit.plugin.java.JavaPlugin;

public class LobbyMCPlugin extends JavaPlugin {
    @Override
    public void onLoad() {
        PluginBootstrap.Inicializador(this);
    }

    public <T extends LobbyComponente> T cargarComponente(Class<T> componentClass) {
        try {
            return PluginBootstrap.cargarComponente(componentClass);
        } catch (Exception e) {
            Utils.log("Class could not be loaded: " + componentClass.getName());
            e.printStackTrace();
        }
        return null;
    }

}
