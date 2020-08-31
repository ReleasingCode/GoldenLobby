package com.releasingcode.goldenlobby.modulos.repartidor.listener;


import com.releasingcode.goldenlobby.LobbyMC;
import com.releasingcode.goldenlobby.modulos.repartidor.RepartidorCorePlugin;
import es.minecub.core.minecubos.MinecubosAPI;
import org.apache.commons.lang3.EnumUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import com.releasingcode.goldenlobby.modulos.repartidor.items.Item;
import com.releasingcode.goldenlobby.modulos.repartidor.items.ItemType;
import com.releasingcode.goldenlobby.modulos.repartidor.items.VoteItem;
import com.releasingcode.goldenlobby.modulos.repartidor.manager.RepartidorManager;
import com.releasingcode.goldenlobby.modulos.repartidor.playerdata.MinecubPlayer;
import com.releasingcode.goldenlobby.modulos.repartidor.runnables.MainRunnable;

public class MainListener implements Listener {
    private static final String ALREADY_PICKED_UP = "${lobby.deliveryman.already}";


    public MainListener() {
        Bukkit.getPluginManager().registerEvents(this, LobbyMC.getInstance());
        new MainRunnable();
    }

    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onPlayerJoin(PlayerJoinEvent e) {
        RepartidorCorePlugin.addPlayer(e.getPlayer());

    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        RepartidorCorePlugin.removePlayer(e.getPlayer());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        if (e.getInventory().getName().equalsIgnoreCase("${lobby.inventories.deliveryman}") && e
                .getCurrentItem() != null) {
            MinecubPlayer mp = RepartidorCorePlugin.getPlayers().get(p.getName());
            if (e.getCurrentItem().getType().name().endsWith("MINECART")) {
                String dName = e.getCurrentItem().getItemMeta().getDisplayName();
                if (dName.startsWith(ChatColor.RED.toString())) {
                    p.sendMessage("${lobby.deliveryman.already}");
                    p.playSound(p.getLocation(), Sound.valueOf(
                            EnumUtils.isValidEnum(Sound.class, "BLOCK_ANVIL_LAND") ? "BLOCK_ANVIL_LAND" : "ANVIL_LAND"),
                            1.0F, 1.0F);
                } else {
                    for (Item item : RepartidorManager.getItems().values()) {
                        if (item.getSlot() == e.getSlot()) {
                            if (mp.getLongs().containsKey(item.getName())) {
                                p.playSound(p.getLocation(), Sound.valueOf(EnumUtils.isValidEnum(Sound.class,
                                        "BLOCK_ANVIL_LAND") ? "BLOCK_ANVIL_LAND" : "ANVIL_LAND"), 1.0F, 1.0F);
                                p.sendMessage("${lobby.deliveryman.already}");
                                break;
                            }

                            String permission = item.getPermission();
                            if (permission != null) {
                                if (permission.contains("viph")) {
                                    if (!p.hasPermission("core.viph") && !p.hasPermission("core.vipo") && !p
                                            .hasPermission("core.vipd")) {
                                        p.sendMessage("${lobby.deliveryman.vip}");
                                        break;
                                    }
                                } else if (permission.contains("vipo")) {
                                    if (!p.hasPermission("core.vipo") && !p.hasPermission("core.vipd")) {
                                        p.sendMessage("${lobby.deliveryman.vipo}");
                                        break;
                                    }
                                } else if (permission.contains("vipd") && !p.hasPermission("core.vipd")) {
                                    p.sendMessage("${lobby.deliveryman.vipd}");
                                    break;
                                }
                            }

                            if (item.getType() == ItemType.COMMAND) {
                                long time = System.currentTimeMillis();
                                mp.getLongs().put(item.getName(), time);
                                Bukkit.getScheduler().runTaskAsynchronously(LobbyMC.getInstance(), () -> {
                                    MinecubosAPI.giveMinecubos(p, item.getCoins(), true);
                                });
                                RepartidorManager.updateLong(p.getName(), item.getName(), time);
                                RepartidorManager.spawnDiamonds();
                                MainRunnable.updateInv(mp);
                                p.sendMessage("${lobby.deliveryman.loot}[" + item.getCoins() + "]");
                            } else if (item.getType() == ItemType.VOTEPAGE) {
                                VoteItem vItem = (VoteItem) item;
                                p.sendMessage("${lobby.deliveryman.vote.json}[" + vItem.getLink() + ", " + vItem
                                        .getServiceName() + ", " + item.getCoins() + "]");
                                p.sendMessage(" ");
                                p.sendMessage("${lobby.deliveryman.vote.onlypremium}");
                            }
                            break;
                        }
                    }
                }
            } else if (e.getCurrentItem().getType().name().contains("GLASS_PANE")) {
                p.sendMessage("${error}[No se han podido cargar los datos]");
            }
            e.setCancelled(true);
        }

    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent e) {
        if (e.getItem().getItemStack().getItemMeta().getDisplayName() != null && e.getItem().getItemStack()
                .getItemMeta().getDisplayName().startsWith("DPU")) {
            e.setCancelled(true);
        }
    }
}