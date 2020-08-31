package com.releasingcode.goldenlobby.modulos.tragacubos.listener;

import com.releasingcode.goldenlobby.modulos.tragacubos.TragacubosPlugin;
import es.minecub.core.minecubos.MinecubosAPI;
import es.minecub.core.mysql.MysqlManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import com.releasingcode.goldenlobby.modulos.tragacubos.objects.tragacubos.Tragaperra;

public class ButtonClick implements Listener {
    private final TragacubosPlugin plugin;

    public ButtonClick(TragacubosPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    private void onButtonUse(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getClickedBlock().getType() != Material.STONE_BUTTON) return;
        if (!plugin.getTragaperrasManager().getTragaperra(e.getClickedBlock()).isPresent()) return;

        Tragaperra tragaperra = plugin.getTragaperrasManager().getTragaperra(e.getClickedBlock()).get();
        Player player = e.getPlayer();

        if (tragaperra.isRolling()) {
            player.sendMessage("${lobbymc.tragaperras.messages.cannotroll}");
            return;
        }

        int minecubos = MinecubosAPI.getMinecubos(player);
        int price = TragacubosPlugin.ROLL_PRICE;

        if (minecubos < price) {
            player.sendMessage("${lobbymc.regions.shop.notenoughminecubos}");
            return;
        }

        MinecubosAPI.takeMinecubos(player, price);

        tragaperra.roll(player);

        MysqlManager.performAsyncUpdate("UPDATE mLobbyN.tragacubosHologram SET AmountInvested = AmountInvested + ?;",
                () -> plugin.getHologramManager().updateHologram(), price);

    }

}
