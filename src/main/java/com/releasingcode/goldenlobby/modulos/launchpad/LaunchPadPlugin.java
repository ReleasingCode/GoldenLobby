package com.releasingcode.goldenlobby.modulos.launchpad;

import com.releasingcode.goldenlobby.Utils;
import com.releasingcode.goldenlobby.configuracion.CustomConfiguration;
import com.releasingcode.goldenlobby.loader.LobbyComponente;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class LaunchPadPlugin extends LobbyComponente implements Listener {

    private final String name = "LaunchPadPlugin";
    private final CustomConfiguration config = new CustomConfiguration("launchPad", getPlugin());

    @Override
    protected void onEnable() {
        Utils.log(name, " - Enabling module");

        new LaunchPadReload(this).register();
        getPlugin().getServer().getPluginManager().registerEvents(this, getPlugin());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        String configPath = null;
        switch (player.getLocation().getBlock().getType().toString()) {
            case "WOOD_PLATE":
            case "ACACIA_PRESSURE_PLATE":
            case "BIRCH_PRESSURE_PLATE":
            case "CRIMSON_PRESSURE_PLATE":
            case "DARK_OAK_PRESSURE_PLATE":
            case "JUNGLE_PRESSURE_PLATE":
            case "OAK_PRESSURE_PLATE":
            case "SPRUCE_PRESSURE_PLATE":
            case "WARPED_PRESSURE_PLATE":
                configPath = "wood";
                break;
            case "STONE_PLATE":
            case "STONE_PRESSURE_PLATE":
            case "POLISHED_BLACKSTONE_PRESSURE_PLATE":
                configPath = "stone";
                break;
            case "GOLD_PLATE":
            case "LIGHT_WEIGHTED_PRESSURE_PLATE":
                configPath = "gold";
                break;
            case "IRON_PLATE":
            case "HEAVY_WEIGHTED_PRESSURE_PLATE":
                configPath = "iron";
                break;
        }

        if (configPath == null || !config.getConfig().getBoolean("enabled-plates." + configPath))
            return;

        player.setVelocity(player.getLocation().getDirection()
                .multiply(config.getConfig().getDouble("horizontal-impulse", 200) / 100)
                .setY(config.getConfig().getDouble("vertical-impulse", 100) / 100));

        try {
            Sound sound = Sound.valueOf(config.getConfig().getString("launch-sound", "").toUpperCase());
            player.playSound(player.getLocation(), sound, 2.0F, 1.0F);
        } catch (IllegalArgumentException ignored) {
        }
    }

    public CustomConfiguration getConfig() {
        return config;
    }

    public String getName() {
        return name;
    }

    @Override
    protected void onDisable() {
        Utils.log(name, " - Disabling module");
    }
}
