package com.releasingcode.goldenlobby.modulos.tragacubos;

import com.releasingcode.goldenlobby.LobbyMC;
import com.releasingcode.goldenlobby.Utils;
import com.releasingcode.goldenlobby.configuracion.CustomConfiguration;
import com.releasingcode.goldenlobby.loader.LobbyComponente;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import com.releasingcode.goldenlobby.modulos.tragacubos.command.TragaperraCommand;
import com.releasingcode.goldenlobby.modulos.tragacubos.listener.ButtonClick;
import com.releasingcode.goldenlobby.modulos.tragacubos.listener.CreationModeListeners;
import com.releasingcode.goldenlobby.modulos.tragacubos.objects.TragacubosHologramManager;
import com.releasingcode.goldenlobby.modulos.tragacubos.objects.tragacubos.Tragaperra;
import com.releasingcode.goldenlobby.modulos.tragacubos.objects.tragacubos.TragaperrasManager;

import java.util.List;

public class TragacubosPlugin extends LobbyComponente {
    public static String BROADCAST_MESSAGE;
    public static int ROLL_PRICE;
    public static List<String> HOLOGRAM_TEXT;

    private CustomConfiguration configuration;
    private TragaperrasManager tragaperrasManager;
    private TragacubosHologramManager hologramManager;

    @Override
    public void onEnable() {
        new BukkitRunnable() {
            @Override
            public void run() {
                configuration = new CustomConfiguration("tragaperras", getPlugin());
                FileConfiguration config = configuration.getConfig();

                if (!config.getBoolean("enabled")) return;

                Utils.log(" - Cargando módulo de Tragaperras");

                BROADCAST_MESSAGE = config.getString("broadcast-at-win");
                ROLL_PRICE = config.getInt("price");
                HOLOGRAM_TEXT = Utils.coloredList(config.getStringList("hologram-text"));

                tragaperrasManager = new TragaperrasManager(TragacubosPlugin.this);
                hologramManager = new TragacubosHologramManager(TragacubosPlugin.this, HOLOGRAM_TEXT);

                new TragaperraCommand(TragacubosPlugin.this).register();

                getPlugin().getServer().getPluginManager().registerEvents(new ButtonClick(TragacubosPlugin.this), getPlugin());
                getPlugin().getServer().getPluginManager().registerEvents(new CreationModeListeners(TragacubosPlugin.this), getPlugin());
                getPlugin().getServer().getPluginManager().registerEvents(hologramManager, getPlugin());

            }
        }.runTaskLater(LobbyMC.getInstance(), 5 * 20);
    }

    @Override
    public void onDisable() {
        hologramManager.getHologram().destroy();
        Utils.log(" - Inhabilitando módulo de Tragaperras");
    }

    public CustomConfiguration getConfiguration() {
        return configuration;
    }

    public TragaperrasManager getTragaperrasManager() {
        return tragaperrasManager;
    }

    public void stop() {
        tragaperrasManager.getTragaperras().forEach(Tragaperra::removeAllFrames);
    }

    public TragacubosHologramManager getHologramManager() {
        return hologramManager;
    }

}
