package com.releasingcode.goldenlobby.modulos.tragacubos.objects.items.recompensas;

import com.releasingcode.goldenlobby.LobbyMC;
import es.minecub.core.gifts.GiftCodes;
import es.minecub.core.gifts.GiftType;
import es.minecub.core.ranks.PlayerRank;
import es.minecub.core.ranks.RanksCore;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Optional;

public class CodigoVip extends Rewardable {
    private int duration;
    private PlayerRank vip;

    private final String[] data = new String[2];

    @Override
    public Optional<Rewardable> loadReward(String reward) throws Exception {
        String[] rewardSplitted = reward.split(":");

        if (!RanksCore.findRankWithName(rewardSplitted[1]).isPresent()) {
            throw new Exception("Rango no encontrado para codigo vip");
        }

        this.vip = RanksCore.findRankWithName(rewardSplitted[1]).get();
        this.duration = Integer.parseInt(rewardSplitted[2]);

        data[0] = vip.getName();
        data[1] = String.valueOf(duration);

        return Optional.of(this);
    }

    @Override
    public void giveReward(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                String gift = GiftCodes.getInstance().generateGift(GiftType.RANK, data);
                player.sendMessage("${lobbymc.tragaperras.reward.codigovip}[" + vip.getTranslation() + ", " + duration + "]");
                player.sendMessage(ChatColor.GREEN + "Utiliza: " + ChatColor.RESET + "/canjear " + gift);
            }
        }.runTaskAsynchronously(LobbyMC.getInstance());
    }

}
