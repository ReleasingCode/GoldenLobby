package com.releasingcode.goldenlobby.loader;

import org.bukkit.plugin.Plugin;

public abstract class LobbyComponente {

    private boolean enabled = false;


    protected void onEnable() {
    }

    protected void onDisable() {
    }


    public void setEnabled(boolean enabled) {
        if (enabled == this.enabled)
            return;

        this.enabled = enabled;

        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }
    }

    public Plugin getPlugin() {
        return PluginBootstrap.getPlugin();
    }
}
