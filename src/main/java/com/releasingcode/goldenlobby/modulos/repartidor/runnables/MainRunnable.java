package com.releasingcode.goldenlobby.modulos.repartidor.runnables;

import com.releasingcode.goldenlobby.LobbyMC;
import com.releasingcode.goldenlobby.modulos.repartidor.RepartidorCorePlugin;
import com.releasingcode.goldenlobby.modulos.repartidor.items.Item;
import com.releasingcode.goldenlobby.modulos.repartidor.manager.RepartidorManager;
import com.releasingcode.goldenlobby.modulos.repartidor.playerdata.MinecubPlayer;
import com.releasingcode.goldenlobby.modulos.repartidor.utils.Utils;
import es.minecub.core.translations.translator.TranslatorAPI;
import es.minecub.core.utils.PlayerUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainRunnable extends BukkitRunnable {
    private static final ItemStack ERROR_ITEM = Utils.createItem(Material.STAINED_GLASS_PANE, ChatColor.RED + "ERROR!", 1, (short) 14);
    private boolean isRed;

    public MainRunnable() {
        this.isRed = false;
        runTaskTimerAsynchronously(LobbyMC.getInstance(), 0L, 20L);
    }

    public static void updateInv(final MinecubPlayer mp) {
        final Inventory inv = mp.getInventory();
        if (mp.hasAnyError()) {
            for (int i = 0; i < 9; ++i) {
                inv.setItem(i, ERROR_ITEM);
            }
            return;
        }
        final long now = System.currentTimeMillis();
        for (Item item : RepartidorManager.getItems().values()) {
            if (!mp.getLongs().containsKey(item.getName())) {
                final ItemStack invItem = inv.getItem(item.getSlot());
                if (invItem != null && invItem.getType() == Material.STORAGE_MINECART) {
                    continue;
                }
                inv.setItem(item.getSlot(), item.getItem());
            } else {
                final long until = mp.getLongs().get(item.getName());
                final long time = now - until;
                final long unit = 86400000L * item.getTime();
                if (time >= unit) {
                    mp.getLongs().remove(item.getName());
                    inv.setItem(item.getSlot(), item.getItem());
                    mp.getLongs().size();
                    RepartidorManager.getItems().size();
                } else {
                    final long difference = until + unit - now;
                    final String formatExpires = Utils.formatDateDiff(difference);
                    inv.setItem(item.getSlot(),
                            Utils.createItem(Material.MINECART, ChatColor.RED + item.getDisplayName(), 1,
                                    Collections
                                            .singletonList("${lobby.deliveryman.already.lore}[" + formatExpires + "]")));
                }
            }
        }
    }

    @Override
    public void run() {
        this.isRed = !this.isRed;
        /*NPC hologram = NPCManager.getNPC("deliveryman");
        if (hologram == null) {
           // us.minecub.lobbymc.Utils.log("&aNo se ha encontrado el npc DeliveryMan");
            return;
        }*/
        for (MinecubPlayer mp : RepartidorCorePlugin.getPlayersCollections()) {
            String text;
            if (!mp.hasOnline()) {
                //hologram.hide(mp.getPlayer());
                continue;
            }
            if (mp.hasLoaded()) {
                String paquetes;
                if (mp.hasAnyError()) {
                    paquetes = "ERROR";
                } else {
                    int left = RepartidorManager.getItems().size() - mp.getLongs().size();
                    if (left <= 0) {
                        paquetes = (this.isRed ? ChatColor.RED : ChatColor.WHITE) + "${repartidor.paquetes.nothing}";
                    } else {
                        paquetes = (this.isRed ? ChatColor.RED : ChatColor.WHITE) + "${repartidor.paquetes." + (left == 1 ? "singular" : "plural") + "}[" + left + "]";
                    }
                }
                text = TranslatorAPI
                        .translate(paquetes, PlayerUtils.getLanguage(mp.getPlayer()));
                String next = TranslatorAPI
                        .translate("${lobby.holos.deliveryman.name}", PlayerUtils.getLanguage(mp.getPlayer()));
                String next2 = TranslatorAPI
                        .translate("${holos.commons.rightclick}", PlayerUtils.getLanguage(mp.getPlayer()));
                List<String> copy = new ArrayList<>(Arrays.asList(text, next, next2));
                //hologram.updateText(copy, mp.getPlayer());
            }
            updateInv(mp);
        }
    }
}
