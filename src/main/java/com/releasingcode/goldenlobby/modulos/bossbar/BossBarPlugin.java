package com.releasingcode.goldenlobby.modulos.bossbar;

import com.releasingcode.goldenlobby.Utils;
import com.releasingcode.goldenlobby.configuracion.CustomConfiguration;
import com.releasingcode.goldenlobby.loader.LobbyComponente;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.List;

public class BossBarPlugin extends LobbyComponente implements Listener {

    private final String pluginName = "BossBarPlugin";
    private CustomConfiguration bossBarConfig;
    private BossBar bar;
    private int taskId = -1;

    @Override
    protected void onEnable() {
        Utils.log(pluginName, " - Enabling module");

        try {
            Class.forName("org.bukkit.boss.BossBar");
        } catch (ClassNotFoundException e) {
            Utils.log(pluginName, ChatColor.RED + "Incompatible Minecraft version!");
            setEnabled(false);
            return;
        }

        new BossBarReload(this).register();
        bossBarConfig = new CustomConfiguration("bossbar", getPlugin());
        getPlugin().getServer().getPluginManager().registerEvents(this, getPlugin());

        reload();
    }

    public void reload() {
        cancelAnim();

        bossBarConfig.reloadConfig();
        FileConfiguration config = bossBarConfig.getConfig();

        try {
            if (bar != null)
                bar.removeAll();

            bar = getPlugin().getServer().createBossBar(
                    config.getString("title"),
                    BarColor.valueOf(config.getString("color").toUpperCase()),
                    BarStyle.valueOf(config.getString("style").toUpperCase()));

            bar.setProgress(clamp(config.getDouble("progress") / 100, 0, 100));
        } catch (IllegalArgumentException e) {
            Utils.log(ChatColor.RED + "Invalid boss bar option on main options!");
            return;
        }

        if (config.getBoolean("animation.enabled", false)) {
            taskId = -1;
            nextFrame(0, true);
        }

        for (Player player : getPlugin().getServer().getOnlinePlayers())
            bar.addPlayer(player);
    }

    private void nextFrame(int currentFrame, boolean add) {
        if (bar == null || !bossBarConfig.getConfig().isConfigurationSection("animation.frames"))
            return;

        List<String> keys = new ArrayList<>(bossBarConfig.getConfig()
                .getConfigurationSection("animation.frames").getKeys(false));

        // Out of bounds - Under
        if (currentFrame <= 0) {
            add = true;
            currentFrame = 0;
        }

        // Out of bounds - Over
        if (currentFrame >= keys.size()) {
            if (!bossBarConfig.getConfig().getBoolean("animation.bounce"))
                currentFrame = 0;
            else {
                add = false;
                currentFrame = keys.size() - 2;
            }
        }

        String key = keys.get(currentFrame);
        ConfigurationSection section = bossBarConfig.getConfig()
                .getConfigurationSection("animation.frames." + key);

        long nextDelay = bossBarConfig.getConfig().getLong("animation.tpf", 20);
        for (String option : section.getKeys(false)) {
            switch (option.toLowerCase()) {
                case "title":
                    bar.setTitle(section.getString(option));
                    break;
                case "color":
                    String color = section.getString(option).toUpperCase();
                    try {
                        bar.setColor(BarColor.valueOf(color));
                    } catch (IllegalArgumentException e) {
                        cancelAnim();
                        Utils.log(ChatColor.RED +
                                "Invalid color on frame " + key + " -> " + option + ": " + color);
                    }
                    break;
                case "style":
                    String style = section.getString(option).toUpperCase();
                    try {
                        bar.setColor(BarColor.valueOf(style));
                    } catch (IllegalArgumentException e) {
                        cancelAnim();
                        Utils.log(ChatColor.RED +
                                "Invalid style on frame " + key + " -> " + option + ": " + style);
                    }
                    break;
                case "progress":
                    bar.setProgress(
                            clamp(section.getDouble(option, 100) / 100, 0, 100));
                    break;
                case "ticks":
                    nextDelay = section.getLong(option);
                    break;
            }
        }

        if (add)
            currentFrame++;
        else
            currentFrame--;

        int finalFrame = currentFrame;
        boolean finalAdd = add;
        taskId = getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(
                getPlugin(), () -> nextFrame(finalFrame, finalAdd), Math.max(nextDelay, 0L));
    }

    private void cancelAnim() {
        if (taskId == -1) return;
        getPlugin().getServer().getScheduler().cancelTask(taskId);
        taskId = -1;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        if (bar != null)
            bar.addPlayer(e.getPlayer());
    }

    private double clamp(double num, double min, double max) {
        return Math.min(Math.max(num, min), max);
    }

    @Override
    protected void onDisable() {
        cancelAnim();
        if (bar != null)
            bar.removeAll();

        Utils.log(pluginName, " - Disabling module");
    }
}
