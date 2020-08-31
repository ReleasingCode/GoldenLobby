package com.releasingcode.goldenlobby.modulos.tragacubos.objects.items.recompensas;

import es.minecub.core.minecubos.MinecubosAPI;
import org.bukkit.entity.Player;

import java.util.Optional;

public class Minecubos extends Rewardable {
    private int amount;

    @Override public Optional<Rewardable> loadReward(String reward) {
        String[] rewardSplitted = reward.split(":");
        this.amount = Integer.parseInt(rewardSplitted[1]);
        return Optional.of(this);
    }

    @Override public void giveReward(Player player) {
        MinecubosAPI.giveMinecubos(player, amount, true);
        player.sendMessage("${lobbymc.tragaperras.reward.minecubos}[" + amount + "]");
    }

}
