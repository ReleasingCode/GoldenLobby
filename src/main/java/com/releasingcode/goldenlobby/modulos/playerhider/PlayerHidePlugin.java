package com.releasingcode.goldenlobby.modulos.playerhider;

import com.releasingcode.goldenlobby.Utils;
import com.releasingcode.goldenlobby.configuracion.CustomConfiguration;
import com.releasingcode.goldenlobby.loader.LobbyComponente;

public class PlayerHidePlugin extends LobbyComponente {
    protected static int SLOT;

    @Override
    protected void onEnable() {
        Utils.log(" - Loading module PlayerHider");
        getPlugin().getServer().getPluginManager().registerEvents(new PlayerHiderListeners(), getPlugin());
        SLOT = new CustomConfiguration("player_hider", getPlugin()).getConfig().getInt("slot");
    }

    @Override
    protected void onDisable() {
        Utils.log(" - Enabling module PlayerHider");
    }

}
