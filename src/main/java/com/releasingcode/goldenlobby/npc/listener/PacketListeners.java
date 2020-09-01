package com.releasingcode.goldenlobby.npc.listener;


import com.releasingcode.goldenlobby.managers.DelayPlayer;
import com.releasingcode.goldenlobby.npc.api.NPC;
import com.releasingcode.goldenlobby.npc.internal.NPCManager;
import com.releasingcode.goldenlobby.extendido.packetlistener.listener.PacketHandler;
import com.releasingcode.goldenlobby.extendido.packetlistener.listener.PacketListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import com.releasingcode.goldenlobby.npc.api.events.NPCInteractEvent;
import com.releasingcode.goldenlobby.npc.listener.PacketEvents.EntityClickEvent;

public class PacketListeners implements PacketListener, Listener {

    private static void callEvent(NPCInteractEvent event) {
        Player player = event.getWhoClicked();
        if (!player.getWorld().equals(event.getNPC().getWorld()))
            return; // If the NPC and player are not in the same world, abort!
        double distance = player.getLocation().distance(event.getNPC().getLocation());
        if (player.getName().equals("Metrofico")) {
            player.sendMessage("[" + distance + "] " + event.getNPC().getName() + " " + event.getNPC().getId());
        }
        if (distance <= 32) { // Only handle the interaction if the player is within interaction range. This way, hacked clients can't interact with NPCs that they shouldn't be able to interact with.
            Bukkit.getPluginManager().callEvent(event);
        }
    }

    @PacketHandler
    public void onClickPacketNPC(EntityClickEvent event) {
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }
        int ID = event.getID();
        NPC npc = null;
        for (NPC testNPC : NPCManager.getAllNPCs()) {
            if (testNPC.getEntityId() == ID) {
                if (testNPC.isShown(player)) {
                    npc = testNPC;
                    break;
                }
            }
        }
        if (npc == null) {
            // Default player, not doing magic with the packet.
            return;
        }
        if (DelayPlayer.containsDelay(player, "npc_interact")) { // There is an active delay.
            //event.setCancelled(true);
            return;
        }
        //delay.remove(player.getUniqueId()); // Remove the NPC from the interact cooldown.
        NPCInteractEvent.ClickType clickType = event.getAction().toUpperCase().equals("ATTACK")
                ? NPCInteractEvent.ClickType.LEFT_CLICK : NPCInteractEvent.ClickType.RIGHT_CLICK;
        DelayPlayer.addDelay(player, "npc_interact", 1000);
        callEvent(new NPCInteractEvent(player, clickType, npc));
    }
}
