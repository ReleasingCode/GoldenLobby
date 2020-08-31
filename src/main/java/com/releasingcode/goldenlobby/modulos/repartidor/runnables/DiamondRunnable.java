package com.releasingcode.goldenlobby.modulos.repartidor.runnables;

import com.releasingcode.goldenlobby.LobbyMC;
import com.releasingcode.goldenlobby.modulos.repartidor.manager.RepartidorManager;
import com.releasingcode.goldenlobby.modulos.repartidor.utils.Utils;
import com.releasingcode.goldenlobby.npc.internal.NPCManager;
import org.apache.commons.lang3.EnumUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class DiamondRunnable extends BukkitRunnable {
    private final List<Item> diamonds = new ArrayList<>();

    public DiamondRunnable() {
        super();
        if (RepartidorManager.isDiamondsTaskRunning()) {
            return;
        }
        RepartidorManager.toggleDiamonds();
        for (final Entity e : Bukkit.getServer().getWorlds().get(0).getEntities()) {
            if (e.getType() == EntityType.DROPPED_ITEM) {
                final Item i = (Item) e;
                if (i.getItemStack().getType() != Material.DIAMOND || i.getItemStack().getItemMeta() == null || i
                        .getItemStack().getItemMeta().getDisplayName() == null || !i.getItemStack().getItemMeta()
                        .getDisplayName().startsWith("DPU")) {
                    continue;
                }
                i.remove();
            }
        }
        runTaskTimer(LobbyMC.getInstance(), 0L, 5L);
    }

    @Override
    public void run() {
        if (this.diamonds.size() < 11) {
            final ItemStack item = Utils.createItem(Material.DIAMOND, "DPU" + UUID.randomUUID());
            final Location loc = NPCManager.getNPC("deliveryman").getLocation();
            final Item drop = Bukkit.getWorlds().get(0).dropItemNaturally(loc.add(0.0, 2.0, 0.25), item);
            loc.getWorld().playSound(loc, Sound.valueOf(
                    EnumUtils.isValidEnum(Sound.class, "ENTITY_ITEM_PICKUP") ? "ENTITY_ITEM_PICKUP" : "ITEM_PICKUP"),
                    1.0f, 1.0f);
            this.diamonds.add(drop);
        } else {
            for (final Item item2 : this.diamonds) {
                item2.remove();
            }
            this.diamonds.clear();
            RepartidorManager.toggleDiamonds();
            this.cancel();
        }
    }
}
