package com.releasingcode.goldenlobby.languages;

import com.releasingcode.goldenlobby.GoldenLobby;
import com.releasingcode.goldenlobby.configuracion.CustomConfiguration;

public class LanguageFile {
    private final GoldenLobby plugin;
    private final CustomConfiguration config;
    private final String name;

    public LanguageFile(GoldenLobby plugin, String name) {

        this.plugin = plugin;
        this.name = name;
        this.config = new CustomConfiguration(name, "/Lang/", plugin);

    }

    public GoldenLobby getPlugin() {
        return plugin;
    }

    public CustomConfiguration getConfig() {
        return config;
    }

    public String getName() {
        return name;
    }

    public void setup() {
        for (Lang i : Lang.values()) {
            config.addDefault(i.getPath(), i.getDef());
        }
    }

}
