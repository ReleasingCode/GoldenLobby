package com.releasingcode.goldenlobby.modulos.scoreboard.manager;


import com.releasingcode.goldenlobby.extendido.scoreboard.Sidebar;
import com.releasingcode.goldenlobby.loader.LobbyComponente;

public class SidebarScoreboard extends LobbyComponente {
    @Override
    protected void onEnable() {
        Sidebar.init();
    }

    @Override
    protected void onDisable() {
        Sidebar.exit();
    }
}
