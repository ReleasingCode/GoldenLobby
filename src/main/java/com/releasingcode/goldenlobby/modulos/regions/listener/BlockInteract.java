package com.releasingcode.goldenlobby.modulos.regions.listener;

import com.releasingcode.goldenlobby.LobbyMC;
import com.releasingcode.goldenlobby.modulos.regions.RegionPlugin;
import com.releasingcode.goldenlobby.modulos.regions.cuboid.Cuboid;
import com.releasingcode.goldenlobby.modulos.regions.cuboid.StageOfCreation;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class BlockInteract implements Listener {
    private final RegionPlugin regionPlugin;

    public BlockInteract(RegionPlugin regionPlugin) {
        this.regionPlugin = regionPlugin;
    }

    @EventHandler
    public void onRegionSet(PlayerInteractEvent e) {
        if (e.getAction() == Action.PHYSICAL
                || e.getAction() == Action.LEFT_CLICK_AIR
                || e.getAction() == Action.RIGHT_CLICK_AIR
                || !e.getPlayer().hasMetadata("creatingRegion")) return;
        Player player = e.getPlayer();
        Location location = e.getClickedBlock().getLocation();
        StageOfCreation stage = (StageOfCreation) player.getMetadata("creatingRegion").get(0).value();

        if (stage.getStage() == StageOfCreation.Stages.FIRST_POINT) {
            player.removeMetadata("creatingRegion", LobbyMC.getInstance());
            player.setMetadata("creatingRegion", new FixedMetadataValue(LobbyMC.getInstance(), stage.setFirstLoc(location)));
            player.sendMessage(ChatColor.GREEN + "Has establecido el primer punto de región correctamente.");
        } else { // última etapa
            if (!location.getWorld().equals(stage.getFirstLoc().getWorld())) {
                player.sendMessage(ChatColor.RED + "No puedes crear regiones con diferentes mundos.");
                return;
            }

            stage.setSecondLoc(location);
            createRegion(player, stage);
            player.removeMetadata("creatingRegion", LobbyMC.getInstance());
        }

        e.setCancelled(true);
    }

    private void createRegion(Player player, StageOfCreation stage) {
        String regionName = stage.getRegion().name().toLowerCase();
        if (isRegionCreated(regionName)) {
            player.sendMessage(ChatColor.RED + "¡Esa región ya está creada!");
            return;
        }

        setToConfig(stage.getFirstLoc(), regionName, "first-point");
        setToConfig(stage.getSecondLoc(), regionName, "second-point");
        regionPlugin.getCuboidManager().createCuboid(stage.getRegion(), new Cuboid(stage.getFirstLoc(), stage.getSecondLoc()));
        player.sendMessage(ChatColor.GREEN + "Has creado la región " + regionName + " correctamente.");
    }

    private boolean isRegionCreated(String regionName) {
        return regionPlugin.getConfiguration().getConfig().isSet("regiones." + regionName);
    }

    private void setToConfig(Location location, String regionName, String point) {
        FileConfiguration config = regionPlugin.getConfiguration().getConfig();
        setPoints(config, regionName, location, point);
        setPoints(config, regionName, location, point);
        regionPlugin.getConfiguration().save();
    }

    private void setPoints(FileConfiguration config, String regionName, Location location, String point) {
        config.set("regiones." + regionName + "." + point + ".world", location.getWorld().getName());
        config.set("regiones." + regionName + "." + point + ".x", location.getX());
        config.set("regiones." + regionName + "." + point + ".y", location.getY());
        config.set("regiones." + regionName + "." + point + ".z", location.getZ());
        config.set("regiones." + regionName + "." + point + ".yaw", location.getYaw());
        config.set("regiones." + regionName + "." + point + ".pitch", location.getPitch());
    }

}
