package com.releasingcode.goldenlobby.modulos.tragacubos.objects.items.recompensas;

import es.minecub.core.gifts.types.RankGift;
import es.minecub.core.ranks.PlayerRank;
import es.minecub.core.ranks.RanksCore;
import org.bukkit.entity.Player;

import java.util.Optional;

public class VIP extends Rewardable {
    private int duration;
    private PlayerRank vip;

    private final String[] data = new String[2];


    @Override
    public Optional<Rewardable> loadReward(String reward) throws Exception {
        String[] rewardSplitted = reward.split(":");
        if (!RanksCore.findRankWithName(rewardSplitted[1]).isPresent()) {
            throw new Exception("Rango no encontrado");
        }
        this.vip = RanksCore.findRankWithName(rewardSplitted[1]).get();
        this.duration = Integer.parseInt(rewardSplitted[2]);
        data[0] = vip.getName();
        data[1] = String.valueOf(duration);
        return Optional.of(this);
    }

    @Override
    public void giveReward(Player player) {
        RankGift rankGift = new RankGift();

        boolean canBeRewarded = rankGift.validatePlayer(player, data);

        if (canBeRewarded) {
            rankGift.apply(player, data);
            player.sendMessage("${lobbymc.tragaperras.reward.vip}[" + vip.getTranslation() + ", " + duration + "]");
        }

    }

}
