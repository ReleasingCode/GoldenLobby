package com.releasingcode.goldenlobby.npc.listener;

import com.releasingcode.goldenlobby.managers.DelayPlayer;
import com.releasingcode.goldenlobby.managers.LobbyPlayerMap;
import com.releasingcode.goldenlobby.npc.api.NPC;
import com.releasingcode.goldenlobby.npc.internal.NPCManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;


public class PlayerListener implements Listener {


    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        for (NPC npc : NPCManager.getAllNPCs())
            npc.onLogout(event.getPlayer());
        LobbyPlayerMap.removeJugador(event.getPlayer());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        // Need to auto hide the NPCs from the player, or else the system will think they can see the NPC on respawn.
        Player player = event.getEntity();
        for (NPC npc : NPCManager.getAllNPCs()) {
            if (npc.isShown(player) && npc.getWorld().equals(player.getWorld())) {
                npc.hide(player, true);
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        // If the player dies in the server spawn world, the world change event isn't called (nor is the PlayerTeleportEvent).
        Player player = event.getPlayer();
        for (NPC npc : NPCManager.getAllNPCs()) {
            if (npc.isShown(player) && npc.getWorld().equals(player.getWorld())) {
                npc.hide(player, true);
            }
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        if (event.getCause().equals(PlayerTeleportEvent.TeleportCause.PLUGIN)) {
            DelayPlayer.addDelay(event.getPlayer(), "show_npc", 1200);
        }
    }

//    @EventHandler(priority = EventPriority.MONITOR)
//    public void onChunkUnloadEvent(ChunkUnloadEvent event) {
//        if (event.getChunk().getEntities() != null && event.getChunk().getEntities().length == 0)
//            return;
//        for (Entity entity : event.getChunk().getEntities()) {
//            for (NPC npc : NPCManager.getAllNPCs()) {
//                if (npc.getEntityId() == entity.getEntityId()) {
//                    event.setCancelled(true);
//                }
//            }
//        }
//    }

}
