package com.releasingcode.goldenlobby.modulos.welcomemessage;

import com.releasingcode.goldenlobby.Utils;
import com.releasingcode.goldenlobby.configuracion.CustomConfiguration;
import com.releasingcode.goldenlobby.loader.LobbyComponente;
import com.releasingcode.goldenlobby.modulos.welcomemessage.command.WelcomeMessageCmd;
import com.releasingcode.goldenlobby.modulos.welcomemessage.listener.onJoin;

import java.util.List;

public class WelcomeMessage extends LobbyComponente {
    private CustomConfiguration welcomeMessage;
    private List<String> messages;

    @Override
    protected void onEnable() {
        Utils.log("Starting WelcomeMessage");
        welcomeMessage = new CustomConfiguration("welcomeMessage", getPlugin());
        messages = Utils.stringToArrayList(welcomeMessage.getConfig().getString("welcomeMessage"));
        new WelcomeMessageCmd(this, "welcomemessage").register();
        new onJoin(this);
    }

    public void reload() {
        welcomeMessage.reloadConfig();
        messages = Utils.stringToArrayList(welcomeMessage.getConfig().getString("welcomeMessage"));
    }

    @Override
    protected void onDisable() {

    }

    public List<String> getMessages() {
        return messages;
    }

    public CustomConfiguration getWelcomeMessage() {
        return welcomeMessage;
    }
}
