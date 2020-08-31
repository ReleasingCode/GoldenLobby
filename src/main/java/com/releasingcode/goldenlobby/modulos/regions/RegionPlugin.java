package com.releasingcode.goldenlobby.modulos.regions;

import com.releasingcode.goldenlobby.Utils;
import com.releasingcode.goldenlobby.configuracion.CustomConfiguration;
import com.releasingcode.goldenlobby.loader.LobbyComponente;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import com.releasingcode.goldenlobby.modulos.regions.cuboid.Cuboid;
import com.releasingcode.goldenlobby.modulos.regions.cuboid.CuboidPlayerManager;
import com.releasingcode.goldenlobby.modulos.regions.cuboid.StageOfCreation;
import com.releasingcode.goldenlobby.modulos.regions.listener.BlockInteract;
import com.releasingcode.goldenlobby.modulos.regions.listener.RegionListeners;
import com.releasingcode.goldenlobby.modulos.regions.mobs.MobsManager;
import com.releasingcode.goldenlobby.modulos.regions.tienda.ItemStackConfigParser;
import com.releasingcode.goldenlobby.modulos.regions.tienda.ShopInventory;
import com.releasingcode.goldenlobby.modulos.regions.tienda.TiendaCommand;

import java.util.EnumMap;
import java.util.List;
import java.util.NoSuchElementException;

public class RegionPlugin extends LobbyComponente {
    public static List<String> COMMANDS_ALLOWED;
    public static ItemStackConfigParser shopItemsParser;

    private CustomConfiguration configuration;
    private CuboidPlayerManager cuboidManager;
    private ShopInventory shopInventory;
    private MobsManager mobsManager;

    @Override
    protected void onEnable() {
        Utils.log(" - Cargando módulo de Regiones");
        cuboidManager = new CuboidPlayerManager();

        getPlugin().getServer().getPluginManager().registerEvents(new BlockInteract(this), getPlugin());
        getPlugin().getServer().getPluginManager().registerEvents(new RegionListeners(this), getPlugin());
        getPlugin().getServer().getPluginManager().registerEvents(cuboidManager, getPlugin());

        new RegionCommand(this).register();
        configuration = new CustomConfiguration("regiones", getPlugin());
        CustomConfiguration shopConfiguration = new CustomConfiguration("shop", getPlugin());
        shopItemsParser = new ItemStackConfigParser(shopConfiguration.getConfig());
        shopInventory = new ShopInventory(this);

        COMMANDS_ALLOWED = configuration.getConfig().getStringList("comandos-permitidos-pvp-region");

        getPlugin().getServer().getPluginManager().registerEvents(shopInventory, getPlugin());
        new TiendaCommand(this).register();
        loadRegions();

        if (configuration.getConfig().getBoolean("enable-random-mob-events", false)
                && cuboidManager.getRegions().containsKey(StageOfCreation.Regions.PVP)) {
            mobsManager = new MobsManager(this);
        }

    }

    @Override
    protected void onDisable() {
        Utils.log(" - Inhabilitando módulo de Regiones");
    }

    private void loadRegions() {
        FileConfiguration config = configuration.getConfig();
        EnumMap<StageOfCreation.Regions, Cuboid> regions = cuboidManager.getRegions();
        for (StageOfCreation.Regions region : StageOfCreation.Regions.values()) {
            if (!config.isSet("regiones." + region.name().toLowerCase())) continue;
            Location firstLocation = readLocation(region.name().toLowerCase(), "first-point");
            Location secondLocation = readLocation(region.name().toLowerCase(), "second-point");
            cuboidManager.createCuboid(StageOfCreation.Regions.valueOf(region.name().toUpperCase()), new Cuboid(firstLocation, secondLocation));
        }
        if (!regions.isEmpty()) Utils.log(regions.size() + " regiones cargadas.");
    }

    public CustomConfiguration getConfiguration() {
        return configuration;
    }

    private Location readLocation(String regionName, String point) {
        FileConfiguration config = configuration.getConfig();
        String worldName = config.getString("regiones." + regionName + "." + point + ".world");
        World world = Bukkit.getWorld(worldName);
        if (world == null)
            throw new NoSuchElementException("No existe el mundo: " + worldName + " especificado en la configuración de regiones.");

        double x = config.getDouble("regiones." + regionName + "." + point + ".x");
        double y = config.getDouble("regiones." + regionName + "." + point + ".y");
        double z = config.getDouble("regiones." + regionName + "." + point + ".z");

        float yaw = Float.parseFloat(config.getString("regiones." + regionName + "." + point + ".yaw"));
        float pitch = Float.parseFloat(config.getString("regiones." + regionName + "." + point + ".pitch"));

        return new Location(world, x, y, z, yaw, pitch);
    }

    public CuboidPlayerManager getCuboidManager() {
        return cuboidManager;
    }

    public ItemStackConfigParser getShopItemsParser() {
        return shopItemsParser;
    }

    public ShopInventory getShopInventory() {
        return shopInventory;
    }

    public MobsManager getMobsManager() {
        return mobsManager;
    }

}
