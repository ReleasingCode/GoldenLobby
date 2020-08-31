package com.releasingcode.goldenlobby.modulos.tragacubos.objects.items.recompensas;

import com.releasingcode.goldenlobby.LobbyMC;
import es.minecub.core.gifts.GiftCodes;
import es.minecub.core.gifts.GiftType;
import es.minecub.core.ranks.RanksCore;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Optional;

public class Booster extends Rewardable {
    private int duration;

    private final String[] data = new String[1];

    @Override
    public Optional<Rewardable> loadReward(String reward) throws Exception {
        String[] rewardSplitted = reward.split(":");

        if (!RanksCore.findRankWithName(rewardSplitted[1]).isPresent()) {
            throw new Exception("Rango no encontrado para Booster");
        }

        this.duration = Integer.parseInt(rewardSplitted[2]);

        data[0] = String.valueOf(duration);

        return Optional.of(this);
    }

    @Override
    public void giveReward(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                String code = GiftCodes.getInstance().generateGift(GiftType.BOOSTER, data);
                player.sendMessage("${lobbymc.tragaperras.reward.booster}[" + duration + ", " + code + "]");
            }
        }.runTaskAsynchronously(LobbyMC.getInstance());
    }

}
