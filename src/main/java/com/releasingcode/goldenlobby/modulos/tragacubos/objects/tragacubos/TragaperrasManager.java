package com.releasingcode.goldenlobby.modulos.tragacubos.objects.tragacubos;

import com.google.common.base.Preconditions;
import com.releasingcode.goldenlobby.Utils;
import com.releasingcode.goldenlobby.modulos.tragacubos.TragacubosPlugin;
import com.releasingcode.goldenlobby.modulos.tragacubos.objects.items.ItemRecompensable;
import com.releasingcode.goldenlobby.modulos.tragacubos.objects.items.TragaperraItem;
import org.apache.commons.lang3.RandomUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class TragaperrasManager {
    private static final List<TragaperraItem> TRAGAPERRA_ITEMS = new ArrayList<>();
    // K = Localización del botón | V = Tragaperra
    private final Map<Location, Tragaperra> tragaperras = new HashMap<>();
    private final TragacubosPlugin plugin;

    public TragaperrasManager(TragacubosPlugin plugin) {
        this.plugin = plugin;
        loadTragaperras();
        loadItems();
    }

    public static TragaperraItem pickOne() {
        return TRAGAPERRA_ITEMS.get(RandomUtils.nextInt(0, TRAGAPERRA_ITEMS.size()));
    }

    public boolean removeTragaperra(int id) {
        Optional<Location> location = getTragaperras().stream()
                .filter(tragaperra -> tragaperra.getId() == id)
                .map(tragaperra -> tragaperra.getButton().getLocation())
                .findAny();
        if (location.isPresent()) {
            tragaperras.remove(location.get()).removeAllFrames();
            plugin.getConfiguration().getConfig().set("data." + id, null);
            plugin.getConfiguration().save();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Permite comprobar si una tragaperra que será creada
     * no está en conflicto con otra.
     *
     * @param possibleTragaperra tragaperra que se creará
     * @return true si la tragaperra que se creará interfiere con otra.
     */

    public boolean isTragaperraInConflictWithOther(Tragaperra possibleTragaperra) {
        return tragaperras.values().stream()
                .anyMatch(tragaperra -> tragaperra.getButton().equals(possibleTragaperra.getButton())
                        || tragaperra.conflictWithOther(possibleTragaperra));
    }

    public void addTragaperra(Tragaperra tragaperra) {
        tragaperras.putIfAbsent(tragaperra.getButton().getLocation(), tragaperra);
    }

    public Optional<Tragaperra> getTragaperra(Block button) {
        return Optional.ofNullable(tragaperras.get(button.getLocation()));
    }

    @SuppressWarnings("unchecked")
    private void loadItems() {
        FileConfiguration config = plugin.getConfiguration().getConfig();
        ConfigurationSection recompensables = config.getConfigurationSection("lista-items.items-recompensables");
        recompensables.getKeys(false).forEach(index -> {
            ConfigurationSection itemRecompensable = recompensables.getConfigurationSection(index);
            ItemStack item = new ItemStack(Material.valueOf(itemRecompensable.getString("material")));
            List<String> recompensas = (List<String>) itemRecompensable.getList("recompensas");
            TRAGAPERRA_ITEMS.add(new ItemRecompensable(item, recompensas));
        });

        ConfigurationSection noRecompensables = config.getConfigurationSection("lista-items");

        if (noRecompensables != null) {
            recompensables.getKeys(false).forEach(index -> {
                List<String> itemsRelleno = (List<String>) noRecompensables.getList("items-relleno");
                if (itemsRelleno != null) {
                    itemsRelleno.forEach(relleno -> TRAGAPERRA_ITEMS
                            .add(new TragaperraItem(new ItemStack(Material.valueOf(relleno)))));
                }
            });
            Utils.log("Cargados " + TRAGAPERRA_ITEMS.size() + " items de tragaperras.");
        }
    }

    private void loadTragaperras() {
        try {
            FileConfiguration config = plugin.getConfiguration().getConfig();
            ConfigurationSection section = config.getConfigurationSection("data");

            if (section == null) return;

            Set<String> data = section.getKeys(false);
            data.forEach(tragaperra -> {
                ConfigurationSection dataSection = section.getConfigurationSection(tragaperra);
                Block button = getButton(dataSection);
                tragaperras.put(button.getLocation(), parseConfig(button, dataSection));
            });

        } catch (NullPointerException e) {
            Utils.log("No se pudo cargar una tragaperra. Es posible que se haya colocado el ItemFrame en una posición inválida.");
        }

        if (!tragaperras.isEmpty()) Utils.log("¡Cargadas " + tragaperras.size() + " tragaperras!");
    }

    private Tragaperra parseConfig(Block button, ConfigurationSection section) {
        Preconditions.checkArgument(button == null || button.getType() == Material.STONE_BUTTON, "El botón de una tragaperra no existe en el mundo.");
        World world = Bukkit.getWorld(section.getString("world"));
        Tragaperra tragaperra = new Tragaperra(getMarcos(world, section), button);
        tragaperra.setId(Integer.parseInt(section.getName()));
        return tragaperra;
    }

    private Block getButton(ConfigurationSection section) {
        ConfigurationSection botonSection = section.getConfigurationSection("boton");
        World world = Bukkit.getWorld(section.getString("world"));
        double xBoton = botonSection.getDouble("x");
        double yBoton = botonSection.getDouble("y");
        double zBoton = botonSection.getDouble("z");

        return world.getBlockAt(new Location(world, xBoton, yBoton, zBoton));
    }

    private List<MarcoTragaperra> getMarcos(World world, ConfigurationSection section) {
        List<MarcoTragaperra> marcos = new ArrayList<>(3);
        for (int i = 0; i < 3; i++) {
            ConfigurationSection marcoSection = section.getConfigurationSection("marco_" + i);
            double marcoX = marcoSection.getDouble("x");
            double marcoY = marcoSection.getDouble("y");
            double marcoZ = marcoSection.getDouble("z");
            BlockFace blockFace = BlockFace.valueOf(marcoSection.getString("blockface"));
            Location loc = new Location(world, marcoX, marcoY, marcoZ);
            marcos.add(new MarcoTragaperra(loc, blockFace));
        }
        return marcos;
    }

    public Collection<Tragaperra> getTragaperras() {
        return tragaperras.values();
    }

}
