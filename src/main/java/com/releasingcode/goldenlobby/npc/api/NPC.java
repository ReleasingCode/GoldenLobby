package com.releasingcode.goldenlobby.npc.api;


import com.releasingcode.goldenlobby.configuracion.CustomConfiguration;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import com.releasingcode.goldenlobby.modulos.npcserver.history.NPCHistory;
import com.releasingcode.goldenlobby.npc.api.skin.Skin;
import com.releasingcode.goldenlobby.npc.api.state.NPCMode;
import com.releasingcode.goldenlobby.npc.api.state.NPCPosition;
import com.releasingcode.goldenlobby.npc.api.state.NPCSlot;
import com.releasingcode.goldenlobby.npc.api.state.NPCState;

import java.util.List;
import java.util.UUID;

public interface NPC {

    boolean isEditing();

    void setEditing(boolean editing);

    String getNameNPC();

    /**
     * Set the NPC's skin.
     * Use this method before using {@link NPC#create}.
     *
     * @param skin The skin(data) you'd like to apply.
     * @return object instance.
     */
    NPC setSkin(Skin skin);

    boolean isReady();

    void setReady(boolean isReady);

    String getValueSkin();

    /**
     * Get the location of the NPC.
     *
     * @return The location of the NPC.
     */
    Location getLocation();

    /**
     * Set the NPC's location.
     * Use this method before using {@link NPC#create}.
     *
     * @param location The spawn location for the NPC.
     * @return object instance.
     */
    NPC setLocation(Location location);

    /**
     * Get the world the NPC is located in.
     *
     * @return The world the NPC is located in.
     */
    World getWorld();

    /**
     * Create all necessary packets for the NPC so it can be shown to players.
     *
     * @return object instance.
     */
    NPC create();

    /**
     * @return a un identificador del NPC UUID
     */
    String getUid();

    /**
     * Get the ID of the NPC.
     *
     * @return the ID of the NPC.
     */
    String getId();

    int getEntityId();

    /**
     * Test if a player can see the NPC.
     * E.g. is the player is out of range, this method will return false as the NPC is automatically hidden by the library.
     *
     * @param player The player you'd like to check.
     * @return Value on whether the player can see the NPC.
     */
    boolean isShown(Player player);

    /**
     * Show the NPC to a player.
     * Requires {@link NPC#create} to be used first.
     *
     * @param player the player to show the NPC to.
     */
    void show(Player player);

    void hide(Player player);

    void hide(Player player, boolean hide);

    void show(Player player, boolean show);

    List<String> getCommand();

    void setCommand(List<String> command);

    /**
     * Destroy the NPC, i.e. remove it from the registry.
     * Requires {@link NPC#create} to be used first.
     */
    void destroy();

    /**
     * Toggle a state of the NPC.
     *
     * @param state The state to be toggled.
     * @return Object instance.
     */
    NPC toggleState(NPCState state);

    /**
     * Establece el tipo de uso del NPC
     *
     * @param modo el modo del NPC
     */
    void setMode(NPCMode modo);

    NPCMode getNPCMode();

    /**
     * Get state of NPC.
     *
     * @param state The state requested.
     * @return boolean on/off status.
     */
    boolean getState(NPCState state);

    NPC setHashItem(NPCSlot slot, ItemStack item);

    /**
     * Change the item in the inventory of the NPC.
     *
     * @param slot The slot to set the item of.
     * @param item The item to set.
     * @return Object instance.
     */
    NPC setItem(NPCSlot slot, ItemStack item);

    NPC updateText(List<String> text, Player player);

    /**
     * Get the text of an NPC
     *
     * @return List<String> text
     */
    List<String> getText();

    NPC setText(List<String> text);

    /**
     * Get a NPC's item.
     *
     * @param slot The slot the item is in.
     * @return ItemStack item.
     */
    ItemStack getItem(NPCSlot slot);

    void updateSkin();

    boolean isUpdateHologramAutomatic();

    void setUpdateHologramAutomatic(boolean updateHologramAutomatic);

    void setPositionStatus(Player player, NPCPosition position);

    void setPositionStatus(NPCPosition position);

    NPCPosition getPositionMemory();

    void setPositionMemory(NPCPosition position);

    String getCooldownValidator();

    void setCooldownValidator(String cooldownValidator);

    NPCHistory getHistory();

    void setHistory(NPCHistory history);

    void setConfigurationFile(CustomConfiguration configurationFile);

    CustomConfiguration getConfiguration();

    boolean isLookAtPlayer();

    void setLookAtPlayer(boolean look);

    List<String> getRewardCommands();

    void setRewardCommands(List<String> rewardCommands);

    /**
     * Get the UUID of the NPC.
     *
     * @return The UUID of the NPC.
     */
    UUID getUniqueId();


    String getName();

    void destroyForUpdate();

    void onLogout(Player player);

    void lookAt(Player player);


    boolean inRangeOf(Player player, int square);

    boolean inRangeOf(Player p);


}
