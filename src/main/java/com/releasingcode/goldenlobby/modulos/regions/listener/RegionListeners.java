package com.releasingcode.goldenlobby.modulos.regions.listener;

import com.releasingcode.goldenlobby.LobbyMC;
import com.releasingcode.goldenlobby.managers.DelayPlayer;
import com.releasingcode.goldenlobby.managers.ItemStackBuilder;
import com.releasingcode.goldenlobby.managers.LobbyPlayer;
import com.releasingcode.goldenlobby.managers.LobbyPlayerMap;
import com.releasingcode.goldenlobby.modulos.playerhider.Hider;
import com.releasingcode.goldenlobby.modulos.regions.PlayerEnterRegionEvent;
import com.releasingcode.goldenlobby.modulos.regions.RegionPlugin;
import com.releasingcode.goldenlobby.modulos.regions.cuboid.Cuboid;
import com.releasingcode.goldenlobby.modulos.regions.cuboid.StageOfCreation;
import com.yapzhenyie.GadgetsMenu.GadgetsMenu;
import com.yapzhenyie.GadgetsMenu.api.GadgetsMenuAPI;
import com.yapzhenyie.GadgetsMenu.player.PlayerManager;
import com.yapzhenyie.GadgetsMenu.utils.EnumArmorType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import com.releasingcode.goldenlobby.modulos.inventarios.manager.Inventario;
import com.releasingcode.goldenlobby.modulos.inventarios.manager.ItemSelector;

import java.util.Arrays;

public class RegionListeners implements Listener {
    private final RegionPlugin plugin;

    public RegionListeners(RegionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAttack(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player victim = (Player) e.getEntity();

        if (e.getCause() == EntityDamageEvent.DamageCause.FALL) {
            e.setCancelled(!isAPlayerOutsidePvpRegion(victim));
            return;
        }

        if (e instanceof EntityDamageByEntityEvent && e.getEntity() instanceof Player && ((EntityDamageByEntityEvent) e).getDamager() instanceof Player) {
            Player damager = (Player) ((EntityDamageByEntityEvent) e).getDamager();

            DelayPlayer.addDelay(damager, "inCombat", 5000);
            DelayPlayer.addDelay(victim, "inCombat", 5000);

            e.setCancelled(isAPlayerOutsidePvpRegion(victim, damager));
        } else {
            e.setCancelled(isAPlayerOutsidePvpRegion(victim));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        if (isAPlayerOutsidePvpRegion(e.getPlayer())) return;
        if (RegionPlugin.COMMANDS_ALLOWED.contains(e.getMessage())) return;

        if (e.getMessage().startsWith("/gmenu")
                || e.getMessage().startsWith("/gadgetsmenu")
                || e.getMessage().startsWith("/gadgetsmenu:gmenu")
                || e.getMessage().startsWith("/gadgetsmenu:gadgetsmenu")) {
            e.getPlayer().sendMessage("${lobbymc.regions.commands.notallowed}");
            e.setCancelled(true);
            return;
        }

        if (DelayPlayer.containsDelay(e.getPlayer(), "inCombat")) {
            e.getPlayer().sendMessage("${lobbymc.regions.commands.notallowed}");
            e.setCancelled(true);
        }

    }

    @EventHandler
    public void onRegionEnter(PlayerEnterRegionEvent e) {
        Player player = e.getPlayer();
        StageOfCreation.Regions region = e.getRegion();
        LobbyPlayer jugador = LobbyPlayerMap.getJugador(player);

        new BukkitRunnable() {
            @Override public void run() {
                player.setFlying(false);
                player.setAllowFlight(false);

                if (region == StageOfCreation.Regions.PVP) {

                    PlayerManager playerManager = GadgetsMenuAPI.getPlayerManager(player);
                    playerManager.saveEquippedCosmetics(false);
                    playerManager.unequipActiveCosmetics();
                    playerManager.removeMenuSelector();

                    Equipment.setInventory(player);
                    player.setFlying(false);
                    player.setAllowFlight(false);
                    jugador.setFlyer(false);

                    showAllPlayersTo(player);
                    Hider.removeHiderItem(player);
                } else {
                    new BukkitRunnable() {
                        @Override public void run() {
                            if (!isAPlayerOutsidePvpRegion(player)) return;

                            player.setAllowFlight(true);
                            Equipment.removeEquipment(player, true);

                            if (!player.hasMetadata("Vanished")) {
                                Hider.setHiderItem(itemToAdd -> {
                                    Inventario.clearItemsSelectorToPlayer(player);
                                    Inventario.setSelectorToPlayer(player);
                                    if (itemToAdd != null) player.getInventory().addItem(itemToAdd);
                                }, player, true);
                            }

                            PlayerManager playerManager = GadgetsMenuAPI.getPlayerManager(player);
                            playerManager.loadEquippedCosmetics();
                            playerManager.giveMenuSelector();

                            showAllPlayersTo(player);
                            jugador.setFlyer(true);
                        }
                    }.runTaskLater(LobbyMC.getInstance(), 3 * 20);
                }
            }
        }.runTask(LobbyMC.getInstance());
    }

    private void showAllPlayersTo(Player player) {
        if (player.hasMetadata("showingPlayers")) {
            Hider hider = (Hider) player.getMetadata("showingPlayers").get(0).value();
            hider.showPlayers(false);
        } else {
            Hider hider = new Hider(player);
            hider.showPlayers(false);
            player.setMetadata("showingPlayers", new FixedMetadataValue(LobbyMC.getInstance(), hider));
        }
    }

    private boolean isAPlayerOutsidePvpRegion(Player... players) {
        if (!plugin.getCuboidManager().getRegions().containsKey(StageOfCreation.Regions.PVP)) return true;

        boolean isOutside = false;
        Cuboid cuboid = plugin.getCuboidManager().getRegions().get(StageOfCreation.Regions.PVP);

        for (Player player : players) {
            if (!cuboid.isIn(player)) {
                isOutside = true;
                break;
            }
        }
        return isOutside;
    }

    public enum Equipment {
        HELMET(39, new ItemStackBuilder(Material.LEATHER_HELMET).build()),
        CHESTPLATE(38, new ItemStackBuilder(Material.LEATHER_CHESTPLATE).build()),
        LEGGINGS(37, new ItemStackBuilder(Material.LEATHER_LEGGINGS).build()),
        BOOTS(36, new ItemStackBuilder(Material.LEATHER_BOOTS).build()),

        SWORD(new ItemStackBuilder(Material.WOOD_SWORD).build());

        private int slot = -1;
        private final ItemStack items;

        Equipment(int slot, ItemStack items) {
            this.slot = slot;
            this.items = items;
        }

        Equipment(ItemStack item) {
            this.items = item;
        }

        public static void setInventory(Player p) {
            Arrays.stream(p.getInventory().getContents())
                    .filter(ItemSelector::hasItemSelector)
                    .forEach(item -> p.getInventory().remove(item));

            for (Equipment value : values()) {
                if (value.slot == -1) {
                    if (!p.getInventory().contains(value.items)) {
                        p.getInventory().addItem(value.items);
                    }
                } else {
                    ItemStack item = p.getInventory().getItem(value.slot);
                    if (item != null && !item.isSimilar(value.items)) continue;
                    p.getInventory().setItem(value.slot, value.items);
                }
            }
            removeEquipment(p, true);
        }

        /**
         * Evitar que el jugador se lleve
         * el equipamiento básico (por defecto) consigo cuando deje la zona pvp.
         *
         * @param p               jugador
         * @param removeJustArmor true si se desea remover no solo armadura sino items normales.
         */

        public static void removeEquipment(Player p, boolean removeJustArmor) {
            Arrays.stream(p.getInventory().getContents())
                    .forEach(item -> Arrays.stream(values())
                            .filter(equipment -> {
                                if (removeJustArmor)
                                    return equipment.slot != -1 && item != null && equipment.items.isSimilar(item);
                                else
                                    return item != null && equipment.items.isSimilar(item);
                            }).forEach(armorItem -> p.getInventory().remove(armorItem.items)));
        }

        public static boolean isEquipment(ItemStack item) {
            return Arrays.stream(values()).anyMatch(equipment -> ItemStackBuilder.equalsItem(item, equipment.items));
        }

    }

}
