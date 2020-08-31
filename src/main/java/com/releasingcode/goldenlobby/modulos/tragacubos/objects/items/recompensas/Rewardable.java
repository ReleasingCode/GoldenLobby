package com.releasingcode.goldenlobby.modulos.tragacubos.objects.items.recompensas;

import org.bukkit.entity.Player;

import java.util.Optional;

public abstract class Rewardable {

    public abstract Optional<Rewardable> loadReward(String reward) throws Exception;

    public abstract void giveReward(Player player);

}
