package com.releasingcode.goldenlobby.modulos.tragacubos.objects.tragacubos;

import com.releasingcode.goldenlobby.LobbyMC;
import com.releasingcode.goldenlobby.modulos.tragacubos.TragacubosPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class TragaperraCreationStages {
    private final List<MarcoTragaperra> marcos = new ArrayList<>(3);
    private final TragacubosPlugin plugin;
    private Stages actualStage = Stages.NONE;
    private Block button;

    public TragaperraCreationStages(TragacubosPlugin plugin) {
        this.plugin = plugin;
    }

    private boolean hasNextStage() {
        return actualStage.next() != Stages.NONE;
    }

    public Tragaperra parseTragaperra() {
        return new Tragaperra(getMarcos(), button);
    }

    /**
     * Guardar la tragaperra en la configuración.
     *
     * @param plugin plugin
     * @return ID con la que fue guardada la tragaperra en la configuración.
     */

    public int saveOnFile(TragacubosPlugin plugin) {
        FileConfiguration config = plugin.getConfiguration().getConfig();
        int index = 0;
        if (config.isSet("data")) {
            ConfigurationSection data = config.getConfigurationSection("data");
            Optional<Integer> lastIndex = data.getKeys(false)
                    .stream()
                    .map(Integer::parseInt)
                    .max(Comparator.naturalOrder()); // Obtener el ultimo index de la config.
            index = lastIndex.map(integer -> integer + 1).orElse(0);
        }

        config.set("data." + index + ".world", button.getWorld().getName());
        setLocations(config, "boton", button.getLocation(), null, index);
        setLocations(config, "marco_0", marcos.get(0).getLocation(), marcos.get(0).getFaceClicked(), index);
        setLocations(config, "marco_1", marcos.get(1).getLocation(), marcos.get(1).getFaceClicked(), index);
        setLocations(config, "marco_2", marcos.get(2).getLocation(), marcos.get(2).getFaceClicked(), index);
        plugin.getConfiguration().save();
        return index;
    }

    private void setLocations(FileConfiguration config, String key, Location location, @Nullable BlockFace faceClicked, int index) {
        config.set("data." + index + "." + key, null);
        config.set("data." + index + "." + key + ".x", location.getX());
        config.set("data." + index + "." + key + ".y", location.getY());
        config.set("data." + index + "." + key + ".z", location.getZ());
        if (faceClicked != null) config.set("data." + index + "." + key + ".blockface", faceClicked.name());
    }

    /**
     * Ir al siguiente paso en la creación de la tragaperra.
     *
     * @param player   jugador que cliqueó
     * @param location bloque-localización que el jugador cliqueó
     * @return false si terminó de crear la tragaperra, true si todavía le faltan pasos.
     */

    public boolean next(Player player, Location location, BlockFace faceClicked) {
        if (!hasNextStage()) return false;
        actualStage = actualStage.next();

        switch (actualStage) {
            case CREATING_BUTTON:
                this.button = location.getBlock();
                player.sendMessage(ChatColor.GREEN + "¡Has establecido el botón de la tragaperra correctamente!");
                break;
            case SETTING_FIRST_FRAME:
            case SETTING_SECOND_FRAME:
            case SETTING_THIRD_FRAME:
                addMarco(player, location, faceClicked);
                break;
        }

        return true;
    }

    private void addMarco(Player player, Location locationClicked, BlockFace faceClicked) {
        marcos.add(new MarcoTragaperra(locationClicked, faceClicked));
        player.sendMessage(ChatColor.GREEN + "¡Has establecido un marco de la tragaperra correctamente!");
        if (!hasNextStage()) {
            Tragaperra tragaperra = parseTragaperra();
            boolean inConflictWithOther = plugin.getTragaperrasManager().isTragaperraInConflictWithOther(tragaperra);
            if (inConflictWithOther) {
                tragaperra.removeAllFrames();
                player.sendMessage(ChatColor.RED + "¿Seleccionaste bien los bloques? La tragaperra que has intentado crear hace conflicto con otra.");
                return;
            }
            int indexOfSaving = saveOnFile(plugin);
            tragaperra.setId(indexOfSaving);
            plugin.getTragaperrasManager().addTragaperra(tragaperra);
            player.removeMetadata("creandoTragaperra", LobbyMC.getInstance());
            player.sendMessage(ChatColor.GOLD + "Has creado una tragaperra correctamente.");
        }

    }

    public boolean isButtonSet() {
        return button != null;
    }

    public Stages getActualStage() {
        return actualStage;
    }

    public List<MarcoTragaperra> getMarcos() {
        return marcos;
    }

    public enum Stages {
        NONE, CREATING_BUTTON, SETTING_FIRST_FRAME, SETTING_SECOND_FRAME, SETTING_THIRD_FRAME;

        private Stages next() {
            int index = this.ordinal();
            int nextIndex = index + 1;
            Stages[] stages = Stages.values();
            nextIndex %= stages.length;
            return stages[nextIndex];
        }

    }

}
