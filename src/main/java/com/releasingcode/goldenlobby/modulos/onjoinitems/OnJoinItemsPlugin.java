package com.releasingcode.goldenlobby.modulos.onjoinitems;

import com.releasingcode.goldenlobby.configuracion.CustomConfiguration;
import com.releasingcode.goldenlobby.loader.LobbyComponente;

public class OnJoinItemsPlugin extends LobbyComponente {

    private ItemJoin itemJoin;

    @Override
    protected void onEnable() {
        CustomConfiguration onjoinitems = new CustomConfiguration("onjoinitems", getPlugin());
        itemJoin = new ItemJoin(onjoinitems);
        itemJoin.loadItemJoin();
        new OnJoinListener(this);
    }

    public ItemJoin getItemJoin() {
        return itemJoin;
    }

    @Override
    protected void onDisable() {

    }
}
