package com.releasingcode.goldenlobby.loader;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class ZLib {
    static private final ArrayList<Class<? extends LobbyComponente>> componentesAlCargar = new ArrayList<>();
    static JavaPlugin plugin;
    static private Set<LobbyComponente> componentes;

    static private void checkInitialized() throws IllegalStateException {
        if (plugin == null)
            throw new IllegalStateException(
                    "Inicializacion fallida: No se ha instanciado correctamente el complemento a LobbyMCPlugin");
    }

    public static Plugin getPlugin() {
        checkInitialized();
        return plugin;
    }

    public static void Inicializador(LobbyMCPlugin lobbyMCPlugin) {
        plugin = lobbyMCPlugin;
        componentes = new CopyOnWriteArraySet<>();
        for (Class<? extends LobbyComponente> component : componentesAlCargar) {
            cargarComponente(component);
        }
    }

    static public boolean isInicializado() {
        return plugin != null;
    }

    static <T extends LobbyComponente> T cargarComponente(T component) throws IllegalStateException {
        checkInitialized();
        if (componentes.add(component)) {
            component.setEnabled(true);
        }
        return component;
    }


    static public <T extends LobbyComponente> T cargarComponente(Class<T> componentClass) {
        if (!isInicializado()) {
            componentesAlCargar.add(componentClass);
            return null;
        }
        try {
            return cargarComponente(componentClass.newInstance());
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        } catch (NoClassDefFoundError e) {
            return null;
        }
    }

    static public <T extends Listener> T registerEvents(T listener) {
        Bukkit.getServer().getPluginManager().registerEvents(listener, plugin);
        return listener;
    }
}
