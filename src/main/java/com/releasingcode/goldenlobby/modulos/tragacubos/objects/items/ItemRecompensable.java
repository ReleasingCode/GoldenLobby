package com.releasingcode.goldenlobby.modulos.tragacubos.objects.items;

import com.releasingcode.goldenlobby.Utils;
import com.releasingcode.goldenlobby.modulos.tragacubos.objects.items.recompensas.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import us.minecub.lobbymc.modulos.tragacubos.objects.items.recompensas.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ItemRecompensable extends TragaperraItem {
    private final Set<Rewardable> rewards = new HashSet<>();

    public ItemRecompensable(ItemStack item, List<String> recompensas) {
        super(item);
        loadRewards(recompensas);
    }

    @Override
    public boolean giftItem(Player player) {
        rewards.forEach(rewardable -> rewardable.giveReward(player));
        return true;
    }

    private void loadRewards(List<String> recompensas) {
        recompensas.forEach(recompensa -> {
            String[] recompensaSplitted = recompensa.split(":");
            switch (recompensaSplitted[0]) {
                case "MINECUBOS": {
                    try {
                        new Minecubos().loadReward(recompensa).ifPresent(rewards::add);
                    } catch (Exception e) {
                        Utils.log("Recompensa de minecubos no valida: " + recompensa);
                    }
                    break;
                }
                case "VIP": {
                    try {
                        new VIP().loadReward(recompensa).ifPresent(rewards::add);
                    } catch (Exception e) {
                        Utils.log("Recompensa de rango no valida: " + recompensa);
                    }
                    break;
                }
                case "CODIGO_VIP": {
                    try {
                        new CodigoVip().loadReward(recompensa).ifPresent(rewards::add);
                    } catch (Exception e) {
                        Utils.log("Recompensa de codigo vip no valida: " + recompensa);
                    }
                    break;
                }
                case "BOOSTER": {
                    try {
                        new Booster().loadReward(recompensa).ifPresent(rewards::add);
                    } catch (Exception e) {
                        Utils.log("Recompensa de Booster no valida: " + recompensa);
                    }
                    break;
                }
                default: {
                    Utils.log("No existe una recompensa para la String proporcionada: " + recompensaSplitted[0]);
                    break;
                }
            }
        });
    }

}
