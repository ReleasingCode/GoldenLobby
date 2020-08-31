package com.releasingcode.goldenlobby.modulos.inventarios.manager;

import com.releasingcode.goldenlobby.connections.ServerInfo;
import com.releasingcode.goldenlobby.connections.ServerManager;
import com.releasingcode.goldenlobby.modulos.inventarios.bungee.GlobalVariable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TranslateServer {

    private final String text;

    public TranslateServer(String text) {
        if (text == null) {
            this.text = "";
            return;
        }
        Pattern players = Pattern.compile("\\{(.*)_players}");
        Matcher mplayers = players.matcher(text);
        if (mplayers.find()) {
            text = text.replace(mplayers.group(0), ServerManager.getOnlinePlayers(mplayers.group(1)));
        }
        Pattern maxplayers = Pattern.compile("\\{(.*)_maxplayers}");
        Matcher match_maxplayer = maxplayers.matcher(text);
        if (match_maxplayer.find()) {
            text = text.replace(match_maxplayer.group(0), ServerManager.getMaxPlayers(match_maxplayer.group(1)));
        }
        Pattern motd = Pattern.compile("\\{(.*)_motd}");
        Matcher match_motd = motd.matcher(text);
        if (match_motd.find()) {
            text = text.replace(match_motd.group(0), ServerManager.getMotd(match_motd.group(1)));
        }

        this.text = text.replace("{bungee_online}", "" + GlobalVariable.getExtraVariable("bungee_players"));
    }

    public static String TranslateServer(String server, String text) {
        if (text == null) {
            return "";
        }
        text = text.replace("{players}", ServerManager.getOnlinePlayers(server));
        text = text.replace("{maxplayers}", ServerManager.getMaxPlayers(server));
        text = text.replace("{motd}", ServerManager.getMotd(server));
        return text;
    }

    public static ServerInfo.Estados getStatus(String server) {
        return ServerManager.getAvailable(server);
    }


    public String text() {
        return text;
    }

}
