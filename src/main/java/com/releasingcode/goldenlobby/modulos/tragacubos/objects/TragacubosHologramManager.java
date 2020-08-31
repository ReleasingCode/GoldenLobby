package com.releasingcode.goldenlobby.modulos.tragacubos.objects;

import com.releasingcode.goldenlobby.Utils;
import com.releasingcode.goldenlobby.modulos.tragacubos.TragacubosPlugin;
import com.releasingcode.goldenlobby.npc.hologram.Hologram;
import com.releasingcode.goldenlobby.npc.internal.MinecraftVersion;
import es.minecub.core.mysql.MysqlManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TragacubosHologramManager implements Listener {

    private final TragacubosPlugin plugin;
    private final List<String> text;

    private Hologram hologram;
    private int minecubosInvested;

    public TragacubosHologramManager(TragacubosPlugin plugin, List<String> text) {
        this.plugin = plugin;
        this.text = text;
        loadHolograms();
    }

    private void loadHolograms() {
        FileConfiguration config = plugin.getConfiguration().getConfig();

        if (!config.isSet("hologram-data.hologram")) return;

        ConfigurationSection configurationSection = config.getConfigurationSection("hologram-data.hologram");

        World world = Bukkit.getWorld(configurationSection.getString("world"));
        double x = configurationSection.getDouble("x");
        double y = configurationSection.getDouble("y");
        double z = configurationSection.getDouble("z");

        createHologram(new Location(world, x, y, z));

        Utils.log("Cargado holograma de tragacubos.");

    }

    public void createHologram(Location loc) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < TragacubosPlugin.HOLOGRAM_TEXT.size(); i++) {
            list.add("");
        } // Sí, esto no tiene sentido pero así funciona.

        hologram = new Hologram(MinecraftVersion.getNMSVersion(), loc, list);

        updateHologram();
    }

    public void updateHologram() {
        if (hologram == null) return;
        MysqlManager.performAsyncQuery("SELECT * FROM mLobbyN.tragacubosHologram;",
                (set) -> {
                    if (set.next()) {
                        minecubosInvested = set.getInt("AmountInvested");
                        List<String> replaced = text.stream().map(str -> str.replace("{amount}", String.valueOf(minecubosInvested))).collect(Collectors.toList());

                        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                            hologram.showOrUpdate(onlinePlayer, replaced);
                        }
                    }
                }, Throwable::printStackTrace);
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent e) {
        if (hologram != null) {
            List<String> replaced = text.stream().map(str -> str.replace("{amount}", String.valueOf(minecubosInvested))).collect(Collectors.toList());
            hologram.showOrUpdate(e.getPlayer(), replaced);
        }
    }

    public void setHologram(Hologram hologram) {
        this.hologram = hologram;
    }

    public Hologram getHologram() {
        return hologram;
    }

}
