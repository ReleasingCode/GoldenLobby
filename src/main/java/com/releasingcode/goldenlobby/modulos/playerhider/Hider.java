package com.releasingcode.goldenlobby.modulos.playerhider;

import com.releasingcode.goldenlobby.Utils;
import com.releasingcode.goldenlobby.managers.ItemStackBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class Hider {
    private static final Set<String> PLAYERS_WITH_HIDING_ON = new HashSet<>();
    private final Player player;
    private long lastChangeTime;

    protected static final ItemStack HIDER_ITEM = new ItemStackBuilder(Material.WATCH)
            .setName(Utils.chatColor("${lobby.item.hide.player.on}"))
            .addLore("${lobby.item.hide.lore}")
            .addLore("${lobby.item.hide.lore.1}")
            .build();

    public Hider(Player player) {
        this.player = player;
    }

    public void showPlayers(boolean sendMessage) {
        for (Player otherPlayer : Bukkit.getOnlinePlayers()) {
            if (otherPlayer.hasMetadata("Vanished")) continue; // No mostrar staffs en vanish
            player.showPlayer(otherPlayer);
        }

        lastChangeTime = System.currentTimeMillis();
        removeHider(player);
        player.getInventory().setItem(PlayerHidePlugin.SLOT, formatHiderItem(true));

        if (sendMessage) player.sendMessage("${lobbymc.playerhider.messages.activated}");
    }

    protected void hidePlayers() {
        for (Player otherPlayer : Bukkit.getOnlinePlayers()) {
            player.hidePlayer(otherPlayer);
        }

        lastChangeTime = System.currentTimeMillis();
        PLAYERS_WITH_HIDING_ON.add(player.getName());
        player.getInventory().setItem(PlayerHidePlugin.SLOT, formatHiderItem(false));
        player.sendMessage("${lobbymc.playerhider.messages.desactivated}");
    }

    private static ItemStack formatHiderItem(boolean showPlayers) {
        String state = (showPlayers) ? "${lobby.item.hide.player.on}" : "${lobby.item.hide.player.off}";

        return new ItemStackBuilder(HIDER_ITEM.clone())
                .setName(state)
                .build();
    }

    protected boolean isInCooldown() {
        long lastChangeTimeSeconds = TimeUnit.MILLISECONDS.toSeconds(lastChangeTime);
        long actualTimeSeconds = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());

        if (actualTimeSeconds - lastChangeTimeSeconds >= 3) {
            return false;
        }

        player.sendMessage("${lobbymc.playerhider.messages.nottoofast}");
        return true;
    }

    public static boolean isNotHiderItem(ItemStack itemToCompare) {
        if (itemToCompare == null || itemToCompare.getType() == Material.AIR) return true;
        if (itemToCompare.getType() != HIDER_ITEM.getType() || !itemToCompare.hasItemMeta()) return true;

        ItemMeta meta = itemToCompare.getItemMeta();

        if (meta.getLore().get(0) == null) return true;

        return !meta.getLore().get(0).equals(HIDER_ITEM.getItemMeta().getLore().get(0));
    }

    public static void hideIncomingPlayers(Player playerWhoJoined) {
        PLAYERS_WITH_HIDING_ON.forEach(playerHiding -> Bukkit.getPlayer(playerHiding).hidePlayer(playerWhoJoined));
    }

    public static void removeHiderItem(Player player) {
        for (ItemStack itemStack : player.getInventory().getContents()) {
            if (isNotHiderItem(itemStack)) continue;
            player.getInventory().remove(itemStack);
        }
    }

    /**
     * Establecer el Item del Hider.
     * @param itemsAdder instancia con función de callback, retornará el item que se
     *                   encontró en el slot del player hider. Retornará null si {@link #isNotHiderItem(ItemStack)} es falso.
     * @param playerToSetItem jugador al cual setear el item
     * @param showPlayers true si el item que se agregará tendrá en el nombre "jugadores activados"
     */

    public static void setHiderItem(ItemsAdder itemsAdder, Player playerToSetItem, boolean showPlayers) {
        if (playerToSetItem.hasMetadata("Vanished")) return;

        PlayerInventory inventory = playerToSetItem.getInventory();
        ItemStack item = inventory.getItem(PlayerHidePlugin.SLOT);

        if (!isNotHiderItem(item)) {
            itemsAdder.addItemAfterExecution(null);
            return;
        }

        inventory.setItem(PlayerHidePlugin.SLOT, formatHiderItem(showPlayers));
        // Evitar que un item que el jugador haya colocado en el slot del playerhider se overridee por el hider.
        itemsAdder.addItemAfterExecution(item);
    }

    protected static void removeHider(Player player) {
        PLAYERS_WITH_HIDING_ON.remove(player.getName());
    }

    public interface ItemsAdder {
        void addItemAfterExecution(ItemStack itemToAdd);
    }

}
