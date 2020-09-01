package com.releasingcode.goldenlobby.modulos.npcserver.history;

import com.releasingcode.goldenlobby.GoldenLobby;
import com.releasingcode.goldenlobby.Utils;
import com.releasingcode.goldenlobby.extendido.nms.TitleAPI;
import com.releasingcode.goldenlobby.managers.ItemStackBuilder;
import com.releasingcode.goldenlobby.managers.LobbyPlayer;
import com.releasingcode.goldenlobby.managers.history.LobbyPlayerHistory;
import com.releasingcode.goldenlobby.npc.api.NPC;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NPCHistory {
    private final NPC npc;
    private final String historyName;
    private final List<String> required;
    private final String objectiveDisplay;
    private final String target;
    private final List<String> required_messages;
    private final List<String> in_progress_message;
    private final List<String> dissapear_when_is_completed;
    private final LinkedHashMap<Integer, Dash> dashMap;

    public NPCHistory(NPC npc, String historyName,
                      List<String> required,
                      String objectiveDisplay, String target,
                      List<String> required_messages,
                      List<String> in_progress_message,
                      List<String> dissapear_when_is_completed,
                      LinkedHashMap<Integer, Dash> dashMap) {
        this.npc = npc;
        this.historyName = historyName;
        this.required = required;
        this.objectiveDisplay = objectiveDisplay;
        this.target = target;
        this.required_messages = required_messages;
        this.in_progress_message = in_progress_message;
        this.dissapear_when_is_completed = dissapear_when_is_completed;
        this.dashMap = dashMap;
    }

    public List<String> getRequiredMessages() {
        return required_messages;
    }

    public NPC getNpc() {
        return npc;
    }

    public List<String> getDissapear_when_is_completed() {
        return dissapear_when_is_completed;
    }

    public boolean hasDissapearWhenIsCompleted(LobbyPlayerHistory historyPlayer) {
        if (!getDissapear_when_is_completed().isEmpty()) {
            for (String npc : getDissapear_when_is_completed()) {
                return historyPlayer.containsRegistred(npc.replace("-", ""));
            }
        }
        return false;
    }

    public String getTarget() {
        return target;
    }

    public String getObjectiveDisplay() {
        return objectiveDisplay;
    }

    public String getRandomRequiredMessages() {
        try {
            int random = ThreadLocalRandom.current().nextInt(0, required_messages.size());
            return required_messages.get(random);
        } catch (Exception e) {

        }
        return "";
    }

    public String getRandomInProgressMessage() {
        try {
            int random = ThreadLocalRandom.current().nextInt(0, in_progress_message.size());
            return in_progress_message.get(random);
        } catch (Exception e) {

        }
        return "";
    }

    public LinkedHashMap<Integer, Dash> getDashMap() {
        return dashMap;
    }

    public String getHistoryName() {
        return historyName;
    }

    public List<String> getRequired() {
        return required;
    }

    public void play(LobbyPlayer lobbyPlayer) {
        LobbyPlayerHistory pHistory = lobbyPlayer.getHistory();
        pHistory.setPlaying(true);

    }

    public interface DashEffect {
        default void send(Player p) {

        }

        default void send(Player p, NPC npc) {

        }
    }

    public static class DashEffectCommand implements DashEffect {
        private final String command;

        public DashEffectCommand(String command) {
            this.command = command;
        }

        @Override
        public void send(Player p) {
            Utils.evaluateCommand(command, p);
        }
    }

    public static class DashEffectSound implements DashEffect {
        private final float volume;
        private final float pitch;
        private Sound sound;

        public DashEffectSound(String sound, float volume, float pitch) {
            try {
                this.sound = Sound.valueOf(sound.toUpperCase());
            } catch (Exception ignored) {
            }
            this.volume = volume;
            this.pitch = pitch;
        }

        @Override
        public void send(Player p) {
            if (sound != null) {
                p.playSound(p.getLocation(), sound, volume, pitch);
            }
        }
    }

    public static class DashEffectDrop implements DashEffect {
        private final String itemStack;

        public DashEffectDrop(String itemStack) {
            this.itemStack = itemStack;
        }

        @Override
        public void send(Player p, NPC npc) {
            if (npc == null) {
                return;
            }
            ItemStack stack = new ItemStackBuilder(itemStack).build();
            Bukkit.getScheduler().runTask(GoldenLobby.getInstance(), () -> {
                Location location = npc.getLocation();
                Location vectorLocation = location.setDirection(p.getLocation().subtract(location).toVector());
                Item item = vectorLocation.getWorld().dropItemNaturally(vectorLocation, stack);
                item.setPickupDelay(Integer.MAX_VALUE);
                item.setFireTicks(0);
                Bukkit.getScheduler().runTaskLater(GoldenLobby.getInstance(), () -> {
                    try {
                        item.remove();
                    } catch (Exception ignored) {

                    }
                }, 3 * 20);
            });

        }
    }

    public static class DashEffectTitle implements DashEffect {

        String title;
        String subtitle;
        int fadeIn;
        int fadeOut;
        int showTime;

        public DashEffectTitle(String title, String subtitle, int fadeIn, int showTime, int fadeOut) {
            this.title = title;
            this.subtitle = subtitle;
            this.fadeIn = fadeIn;
            this.fadeOut = fadeOut;
            this.showTime = showTime;
            if (subtitle == null) {
                this.subtitle = "";
            }
            if (title == null) {
                this.title = "";
            }
        }

        @Override
        public void send(Player p) {
            TitleAPI.sendTitle(p, this.fadeIn, this.showTime, this.fadeOut, title, subtitle);
        }
    }

    public static class Dash {
        private final int keep;
        private final List<String> messages;
        private final int keepTick;
        private final List<String> effects;
        private final List<DashEffect> dashEffects;

        public Dash(int keep, List<String> messages, List<String> effects) {
            this.keep = keep;
            this.keepTick = ((keep * 1000)) / 50;
            this.messages = messages;
            this.effects = effects;
            dashEffects = new ArrayList<>();
            process();
        }

        public void process() {
            for (String efectos : effects) {
                Pattern pattern;
                if (efectos.startsWith("drop")) {
                    pattern = Pattern.compile("drop\\[(.*)]");
                    Matcher matcher = pattern.matcher(efectos);
                    if (matcher.find()) {
                        String stack = matcher.group(1);
                        dashEffects.add(new DashEffectDrop(stack));
                    }
                    continue;
                }
                if (efectos.startsWith("command")) {
                    pattern = Pattern.compile(
                            "command\\[(.*)]"); // command[server:lobby {player}] | consola:minecubos dar {player} 10 | msg: bla bla bla
                    Matcher matcher = pattern.matcher(efectos);
                    if (matcher.find()) {
                        String stack = matcher.group(1);
                        dashEffects.add(new DashEffectCommand(stack));
                    }
                    continue;
                }
                if (efectos.startsWith("title")) {
                    pattern = Pattern
                            .compile("title\\[(.*)] subtitle\\[(.*)] fadeIn\\[(.*)] showTime\\[(.*)] fadeOut\\[(.*)]");
                    Matcher matcher = pattern.matcher(efectos);
                    if (matcher.find()) {
                        String title = matcher.group(1);
                        String subtitle = matcher.group(2);
                        int fadeIn = Utils.tryParseInt(matcher.group(3), 20);
                        int showTime = Utils.tryParseInt(matcher.group(4), 20);
                        int fadeOut = Utils.tryParseInt(matcher.group(5), 20);
                        dashEffects.add(new DashEffectTitle(title, subtitle, fadeIn, showTime, fadeOut));
                    }
                    continue;
                }
                if (efectos.startsWith("play")) {
                    pattern = Pattern.compile("play\\[(.*)] volume\\[(.*)] pitch\\[(.*)]");
                    Matcher matcher = pattern.matcher(efectos);
                    if (matcher.find()) {
                        String sound = matcher.group(1);
                        float volume = Utils.tryParseFloat(matcher.group(2), 2);
                        float pitch = Utils.tryParseFloat(matcher.group(3), 1);
                        dashEffects.add(new DashEffectSound(sound, volume, pitch));
                    }
                }
            }
        }

        public void sendEffects(Player player, NPC npc) {
            for (DashEffect effect : dashEffects) {
                effect.send(player, npc);
                effect.send(player);
            }
        }

        public List<String> getEffects() {
            return effects;
        }

        public int getKeep() {
            return keep;
        }

        public int getKeepTick() {
            return keepTick;
        }

        public List<String> getMessages() {
            return messages;
        }
    }
}
