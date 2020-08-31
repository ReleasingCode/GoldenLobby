package com.releasingcode.goldenlobby.modulos.tragacubos.objects.tragacubos;

import com.releasingcode.goldenlobby.LobbyMC;
import com.releasingcode.goldenlobby.Utils;
import com.releasingcode.goldenlobby.modulos.tragacubos.TragacubosPlugin;
import com.releasingcode.goldenlobby.modulos.tragacubos.sound.XSound;
import es.minecub.core.sync.player.PlayerManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.influxdb.impl.Preconditions;
import com.releasingcode.goldenlobby.modulos.tragacubos.objects.items.TragaperraItem;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

public class Tragaperra {
    private final TragaperraItem[] items = new TragaperraItem[3];
    private final List<MarcoTragaperra> marcos;
    private final Block button;
    private int id = -1;
    private boolean rolling = false;

    public Tragaperra(List<MarcoTragaperra> marcos, Block button) {
        this.marcos = marcos;
        this.button = button;
    }

    /**
     * Comenzar a dar roll a la tragaperra.
     *
     * @param player jugador que inició el roll
     */

    public void roll(Player player) {
        Preconditions.checkNotNegativeNumber(id, "No se estableció una ID para una tragaperra. No se puede proceder.");
        if (isRolling()) return;
        rolling = true;

        new BukkitRunnable() {
            int iteration = 0;

            @Override
            public void run() {
                if (iteration == 100) {
                    rolling = false;
                    rollEnd(player);
                    cancel();
                    return;
                }

                if (iteration == 32) {
                    playParticle(0);
                } else if (iteration == 64) {
                    playParticle(1);
                } else if (iteration == 96) {
                    playParticle(2);
                } else {
                    for (int i = 0; i < 3; i++) {
                        if (i == 0 && iteration > 32 || i == 1 && iteration > 64) continue;
                        TragaperraItem randomItem = TragaperrasManager.pickOne();
                        items[i] = randomItem;
                        MarcoTragaperra marcoTragaperra = marcos.get(i);
                        marcoTragaperra.setItem(randomItem.getItem());
                    }
                    XSound.UI_BUTTON_CLICK.play(button.getLocation(), 0.1F, 1);
                }

                iteration++;
            }
        }.runTaskTimer(LobbyMC.getInstance(), 0, 1);
    }

    private void rollEnd(Player player) {
        boolean areTheSame = true;

        if (marcos.get(0) == null) {
            return;
        }

        ItemStack item = marcos.get(0).getItem();

        if (item == null) {
            return;
        }

        for (MarcoTragaperra marco : marcos) {
            if (item.getType() == marco.getItem().getType()) continue;
            areTheSame = false;
        }

        if (areTheSame) {
            boolean hadReward = Arrays.stream(items)
                    .findAny()
                    .orElseThrow(() -> new NoSuchElementException("No se encontró un item al finalizar el roll de la tragaperra con ID: " + id))
                    .giftItem(player);

            String playerName = (PlayerManager.getInstance().getDatabase(player).contains("Nickname"))
                    ? PlayerManager.getInstance().getDatabase(player).get("Nickname").asString()
                    : player.getName();

            if (hadReward) {
                Bukkit.broadcastMessage(Utils.chatColor(TragacubosPlugin.BROADCAST_MESSAGE.replace("{jugador}", playerName)));
            }

            XSound.ENTITY_FIREWORK_ROCKET_BLAST.play(player, 0.5F, 1);
            firework();
        } else {
            player.sendMessage("${lobbymc.tragaperras.reward.unlocky}");
            XSound.ENTITY_CAT_AMBIENT.play(player, 0.5F, Float.MIN_VALUE);
        }

    }

    @SuppressWarnings("deprecation")
    private void playParticle(int indexMarco) {
        MarcoTragaperra marcoTragaperra = marcos.get(indexMarco);
        Location loc = marcoTragaperra.getLocation().clone().add(0.5, 0.5, 0.5);
        for (int i = 0; i < 50; i++) {
            loc.getWorld().playEffect(loc, Effect.CRIT, 8);
        }
    }

    public boolean isRolling() {
        return rolling;
    }

    public Block getButton() {
        return button;
    }

    /**
     * Comprobar si una tragaperra
     * coincide AL MENOS una localización de un botón
     * con la otra tragaperra.
     *
     * @param otherTragaperra tragaperra para comprobar a la par de esta
     * @return true si comparten AL MENOS un marco.
     * NOTA: No se tiene en cuenta equalidad de botones.
     */

    public boolean conflictWithOther(Tragaperra otherTragaperra) {
        return marcos.stream()
                .anyMatch(thisMarco -> otherTragaperra.marcos.stream()
                        .anyMatch(otherMarco -> otherMarco.getLocation().equals(thisMarco.getLocation())));
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void removeAllFrames() {
        marcos.forEach(MarcoTragaperra::kill);
    }

    public List<MarcoTragaperra> getMarcos() {
        return marcos;
    }

    private void firework() {
        Firework fw = (Firework) button.getWorld().spawnEntity(button.getLocation().clone().add(0, 2, 0), EntityType.FIREWORK);
        FireworkMeta fwm = fw.getFireworkMeta();

        fwm.setPower(2);
        fwm.addEffect(FireworkEffect.builder().withColor(Color.SILVER, Color.GREEN).build());

        fw.setFireworkMeta(fwm);
        new BukkitRunnable() {
            @Override
            public void run() {
                fw.detonate();
            }
        }.runTaskLater(LobbyMC.getInstance(), 4);
    }

}
