package com.releasingcode.goldenlobby;

import com.releasingcode.goldenlobby.managers.LobbyPlayer;
import com.releasingcode.goldenlobby.managers.LobbyPlayerMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class Utils {
    public static void log(String text) {
        if (text != null) {
            Bukkit.getConsoleSender().sendMessage(chatColor("&f[GoldenLobby] &a" + text));
        }
    }

    public static void launchPlayers(Location loc, int radius) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getWorld().equals(loc.getWorld()) && player.getLocation().distanceSquared(loc) < radius * radius) {
                Location playerLoc = player.getLocation();

                double d = playerLoc.distance(loc);
                double t = 5 - d;

                double difx = (loc.getX() - playerLoc.getX()) / 10;
                double difz = (loc.getZ() - playerLoc.getZ()) / 10;

                if (difx < 0) {
                    difx = difx - 2;
                }

                if (difz < 0) {
                    difz = difz - 2;
                }

                difx++;
                difz++;

                double v_x = difx;
                double v_y = (1 + t / 20);
                double v_z = difz;

                Vector v = player.getVelocity();

                v.setX(v_x);
                v.setY(v_y);
                v.setZ(v_z);

                player.setVelocity(v);
            }
        }
    }

    public static void broadcast(String text) {
        if (text != null) {
            Bukkit.getOnlinePlayers().forEach(player -> {
                player.sendMessage(Utils.chatColor(text));
            });
        }
    }

    public static void log(String plugin, String text) {
        if (text != null) {
            Bukkit.getConsoleSender().sendMessage(chatColor("&f[LobbyMC][&b" + plugin + "&f] &a" + text));
        }
    }

    public static UUID fromUUIDString(String uuid) {
        return UUID.fromString(uuid.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
    }

    public static String[] colorizeArray(String[] array) {
        return Arrays.stream(array).map(Utils::chatColor).toArray(String[]::new);
    }

    public static double square(double val) {
        return val * val;
    }

    public static String chatColor(String text) {
        if (text == null) {
            text = "";
        }
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static String getNMSVersion() {
        String v = Bukkit.getServer().getClass().getPackage().getName();
        return v.substring(v.lastIndexOf('.') + 1);
    }

    public static void writeVarInt(ByteArrayOutputStream output, int i) {
        while ((i & 0xFFFFFF80) != 0) {
            output.write(i & 0x7F | 0x80);
            i >>>= 7;
        }

        output.write(i);
    }

    public static int varIntLength(int i) {
        for (int j = 1; j < 5; ++j) {
            if ((i & (0xFFFFFFFF << (j * 7))) == 0) {
                return j;
            }
        }
        return 5;
    }

    public static int readVarInt(InputStream stream) throws IOException {
        int value = 0, offset = 0, read;

        do {
            if ((read = stream.read()) == -1) {
                throw new EOFException();
            }
            value |= (read & 0x7F) << offset++ * 7;
            if (offset > 5) {
                throw new RuntimeException("VarInt too big");
            }
        } while ((read & 0x80) == 0x80);

        return value;
    }

    public static boolean tryParseShort(String value) {
        try {
            Short.parseShort(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static float tryParseFloat(String value, float defaultValue) {
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException ignored) {
        }
        return defaultValue;
    }

    public static int tryParseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
        }
        return defaultValue;
    }

    public static boolean tryParseInt(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static ArrayList<String> coloredList(List<String> list) {
        ArrayList<String> colored = new ArrayList<>();
        for (String text :
                list) {
            colored.add(chatColor(text));
        }
        return colored;
    }

    public static List<String> stringToArrayList(String text) {
        if (text == null || text.trim().isEmpty() || text.trim().equals("[]")) {
            return new ArrayList<>();
        }
        return Arrays.stream(text.split("\\n")).map(String::trim).collect(Collectors.toCollection(ArrayList::new));
    }

    public static boolean BitBoolean(int bit) {
        return bit == 1;
    }

    public static int BooleanBit(boolean as) {
        return as ? 1 : 0;
    }

    public static String toStringList(List<String> text) {
        String from = "";
        for (String linea :
                text) {
            linea = linea.replace("§", "&");
            from = from.concat(linea) + "\n";
        }
        return from;
    }


    public static void sendToServer(Player p, String server) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bytes);
        try {
            dos.writeUTF("Connect");
            dos.writeUTF(server);
        } catch (Exception ignored) {
        }
        p.sendPluginMessage(GoldenLobby.getInstance(), "BungeeCord", bytes.toByteArray());
    }

    public static String concatArgs(String[] args, int start) {
        String append = "";
        for (int i = start; i < args.length; i++) {
            append = append.concat(args[i]) + " ";
        }
        return append.trim();
    }

    public static void evaluateCommand(String comandos, Player player) {
        comandos = comandos.trim().replaceAll("\\s{2,}", " ");
        comandos = comandos.replace("{player}", player.getName());
        if (comandos.startsWith("msg:")) {
            comandos = comandos.replace("msg:", "").trim();
            LobbyPlayer lplayer = LobbyPlayerMap.getJugador(player);
            if (lplayer != null) {
                lplayer.sendMessage(comandos);
            }
            return;
        }
        if (comandos.startsWith("player:")) {
            comandos = comandos.replace("player:", "").trim();
            Bukkit.dispatchCommand(player, comandos);
            player.updateInventory();
            return;
        }
        if (comandos.startsWith("server:")) {
            comandos = comandos.replace("server:", "").trim();
            sendToServer(player, comandos);
            return;
        }
        if (comandos.startsWith("consola:") || comandos.startsWith("console:")) {
            comandos = comandos.replace("consola:", "").replace("console:", "").trim();
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), comandos);
            return;
        }
        if (comandos.startsWith("sound:")) {
            comandos = comandos.replace("consola:", "").trim();
        }
    }

    public static void deleteIfExist(File file) {
        if (file.isFile() && file.exists()) {
            file.delete();
        }
    }

}
