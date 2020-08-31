package com.releasingcode.goldenlobby.connections;

import com.google.common.base.Charsets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.releasingcode.goldenlobby.Utils;
import com.releasingcode.goldenlobby.modulos.cooldown.object.CooldownSystem;
import com.releasingcode.goldenlobby.npc.api.NPC;

import java.io.EOFException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerManager {
    public static final ConcurrentHashMap<String, ServerManagerVars> serverVars = new ConcurrentHashMap<>();
    // guardar por host
    static final ConcurrentHashMap<String, ServerInfo> servidores = new ConcurrentHashMap<>();
    private static final byte[] PING = new byte[]{9, 1, 0, 0, 0, 0, 0, 0, 0, 0};
    long syncDelay;
    boolean executing;
    private final ScheduledExecutorService scheduledExecutorService;
    private ScheduledFuture<?> threadService;

    public ServerManager(long syncDelay) {
        this.syncDelay = syncDelay;
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    }

    public static void addServer(String name, ServerInfo serverInfo) {
        servidores.computeIfAbsent(name.toLowerCase(), (key) -> serverInfo);
    }

    public static void clearVar() {
        serverVars.clear();
    }

    public static void addVarForServer(String name, ServerManagerVars serverManagerVars) {
        serverVars.computeIfAbsent(name.toLowerCase(), (key) -> serverManagerVars);
    }

    public static List<String> translateVar(List<String> textos, NPC npc) {
        List<String> elTexto = new ArrayList<>();
        for (String text : textos) {
            elTexto.add(translateVar(text, npc));
        }
        return elTexto;
    }

    public static ServerInfo getServerManager(String var) {
        for (ServerInfo sv : servidores.values()) {
            if (sv.getName().toLowerCase().contains(var.toLowerCase())) {
                return sv;
            }
        }
        return null;
    }

    public static String translateVar(String linea, NPC npc) {
        //{lobby} {den1}
        String result = linea;
        Pattern patternCompile = Pattern.compile("\\{(.*)}");
        Matcher matcher = patternCompile.matcher(linea);
        if (matcher.find()) {
            if (npc != null && npc.getCooldownValidator() != null) {
                if (matcher.group(0).trim().equals("{cooldown}")) {
                    CooldownSystem cooldown = CooldownSystem.getCooldown(npc.getCooldownValidator());
                    if (cooldown != null) {
                        result = result.replace(matcher.group(0), cooldown.getRemaing());
                        return result;
                    }
                }
            }
            String extraerSv = matcher.group(1);
            for (ServerManagerVars sv :
                    serverVars.values()) {
                String patternServer = "{" + sv.var + "}";
                if (!patternServer.contains(extraerSv)) {
                    continue;
                }
                int counter = sv.getOnlinePlayersFormula();
                if (counter == 1) {
                    result = linea.replace(matcher.group(0), sv.getSingular());
                    continue;
                }
                result = linea.replace(matcher.group(0), sv.getPlural());
            }
        }
        return result;
    }

    public static void removeServerVar(String name) {
        serverVars.computeIfPresent(name, (key, servervar) -> servervar);
    }

    public static void removeServer(String address) {
        servidores.computeIfPresent(address, (key, server) -> server);
    }

    public static String getMotd(String server) {
        if (servidores.containsKey(server.toLowerCase())) {
            return servidores.get(server.toLowerCase()).getMotd() + "";
        }
        return "&c¡Servidor no encontrado!";
    }

    public static ServerInfo.Estados getAvailable(String server) {
        if (servidores.containsKey(server.toLowerCase())) {
            return servidores.get(server.toLowerCase()).isAvailable() ? ServerInfo.Estados.ON : ServerInfo.Estados.OFF;
        }
        return ServerInfo.Estados.NOT_FOUND;
    }

    public static String getMaxPlayers(String server) {
        if (servidores.containsKey(server.toLowerCase())) {
            return servidores.get(server.toLowerCase()).getMaxplayers() + "";
        }
        return "0";
    }


    public static String getOnlinePlayers(String server) {
        if (servidores.containsKey(server.toLowerCase())) {
            return servidores.get(server.toLowerCase()).getOnline() + "";
        }
        return "0";
    }

    public void stop() {
        executing = false;
        if (threadService != null) {
            threadService.cancel(true);
        }
    }

    public void async() {
        executing = true;
        if (threadService != null) {
            threadService.cancel(true);
        }
        threadService = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            servidores.forEach((key, sinfo) -> {
                try (Socket socket = new Socket(sinfo.getSocketAddress().getAddress(),
                        sinfo.getSocketAddress().getPort())) {
                    OutputStream output = socket.getOutputStream();
                    InputStream input = socket.getInputStream();
                    output.write(sinfo.getHandshake());
                    Utils.readVarInt(input);
                    int read = input.read();
                    if (read != 0) {
                        return;
                    }
                    int length = Utils.readVarInt(input), pos = 0;
                    byte[] jsonBytes = new byte[length];
                    while (pos < length) {
                        if (pos > (pos += input.read(jsonBytes, pos, length - pos))) {
                            throw new EOFException();
                        }
                    }
                    JsonElement response = new JsonParser().parse(new String(jsonBytes, Charsets.UTF_8));
                    String motd;
                    int online = 0, max = 0;
                    if (response.isJsonObject()) {
                        JsonObject obj = response.getAsJsonObject();
                        JsonElement desc = obj.get("description");
                        if (desc.isJsonObject())
                            motd = desc.getAsJsonObject().get("text").getAsString();
                        else motd = desc.getAsString();
                        JsonElement players = obj.get("players");
                        if (players.isJsonObject()) {
                            JsonObject playersObj = players.getAsJsonObject();
                            online = playersObj.get("online").getAsInt();
                            max = playersObj.get("max").getAsInt();
                        }
                    } else motd = response.getAsString();
                    sinfo.setAvailable(true);
                    sinfo.setMotd(motd);
                    sinfo.setOnline(online);
                    sinfo.setMaxplayers(max);
                    output.write(PING);
                } catch (Exception ex) {
                    sinfo.setAvailable(false);
                }
            });
        }, 0, syncDelay, TimeUnit.MILLISECONDS);

    }

    public void clear() {
        stop();
        servidores.clear();
    }


    public static class ServerManagerVars {
        private final String var;
        private String formula;
        private String singular;
        private String plural;

        public ServerManagerVars(String var) {
            this.var = var;
        }

        public void setFormula(String formula) {
            this.formula = formula;
        }

        public String getPlural() {
            return plural.replace("{0}", getOnlinePlayersFormula() + "");
        }

        public void setPlural(String plural) {
            this.plural = plural;
        }

        public String getSingular() {
            return singular.replace("{0}", getOnlinePlayersFormula() + "");
        }

        public void setSingular(String singular) {
            this.singular = singular;
        }

        public String getVar() {
            return var;
        }

        public int getOnlinePlayersFormula() {
            List<String> serverList = new ArrayList<>(Arrays.asList(formula.split("\\+")));
            int contar = 0;
            for (String servidor : serverList) {
                if (!servidores.containsKey(servidor.toLowerCase())) {
                    continue;
                }
                int serverOnline = Integer.parseInt(getOnlinePlayers(servidor));
                contar += serverOnline;
            }
            return contar;
        }
    }
}
