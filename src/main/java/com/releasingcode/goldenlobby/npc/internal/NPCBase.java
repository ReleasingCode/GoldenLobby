package com.releasingcode.goldenlobby.npc.internal;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.releasingcode.goldenlobby.configuracion.CustomConfiguration;
import com.releasingcode.goldenlobby.npc.NPCLib;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import com.releasingcode.goldenlobby.modulos.npcserver.history.NPCHistory;
import com.releasingcode.goldenlobby.npc.api.NPC;
import com.releasingcode.goldenlobby.npc.api.events.NPCHideEvent;
import com.releasingcode.goldenlobby.npc.api.events.NPCShowEvent;
import com.releasingcode.goldenlobby.npc.api.skin.Skin;
import com.releasingcode.goldenlobby.npc.api.state.NPCMode;
import com.releasingcode.goldenlobby.npc.api.state.NPCPosition;
import com.releasingcode.goldenlobby.npc.api.state.NPCSlot;
import com.releasingcode.goldenlobby.npc.api.state.NPCState;
import com.releasingcode.goldenlobby.npc.hologram.Hologram;

import java.util.*;

public abstract class NPCBase implements NPC, NPCPacketHandler {

    protected final Set<UUID> hasTeamRegistered = new HashSet<>();
    protected final Set<NPCState> activeStates = EnumSet.noneOf(NPCState.class);
    protected final Map<NPCSlot, ItemStack> items = new EnumMap<>(NPCSlot.class);
    private final Set<UUID> shown = new HashSet<>();
    protected double cosFOV = Math.cos(Math.toRadians(60));
    protected UUID uuid;
    protected String name;
    protected GameProfile gameProfile;
    protected boolean isReady, editing;
    protected NPCLib instance;
    protected String nameObject;
    protected String nameNPC;
    protected List<String> text;
    protected Location location;
    protected Skin skin;
    protected NPCMode modo;
    protected Hologram hologram;
    protected Hologram hologramMounted;
    protected String forSkin;
    protected NPCHistory history;
    protected int entityId;
    protected List<String> command;
    protected List<String> rewardCommands;
    protected boolean lookAtPlayer;
    protected String cooldownValidator;
    protected CustomConfiguration configuration;
    protected NPCPosition positionStatus;
    protected boolean updateHologramAutomatic;

    public NPCBase(NPCLib instance, String name, List<String> text, UUID uuid, String nameNPC) {
        this.uuid = uuid;
        this.name = uuid.toString().replace("-", "").substring(0, 10);
        this.gameProfile = new GameProfile(uuid, "");
        this.instance = instance;
        this.nameObject = name;
        this.text = text == null ? Collections.emptyList() : text;
        this.modo = NPCMode.COMMAND;
        command = new ArrayList<>();
        positionStatus = NPCPosition.NORMAL;
        updateHologramAutomatic = true;
        this.nameNPC = nameNPC;
        lookAtPlayer = false;
        this.history = null;
        rewardCommands = new ArrayList<>();
        isReady = false;
        editing = false;
        cooldownValidator = null;
        configuration = null;
        entityId = NPCManager.getEntitiesIdGenerator();
        NPCManager.reduceEntitiesIdGenerator();
        NPCManager.add(name, this);
    }

    @Override
    public boolean isUpdateHologramAutomatic() {
        return updateHologramAutomatic;
    }

    @Override
    public void setUpdateHologramAutomatic(boolean updateHologramAutomatic) {
        this.updateHologramAutomatic = updateHologramAutomatic;
    }

    @Override
    public void setPositionStatus(Player player, NPCPosition position) {
        if (position != null) {
            setPositionMemory(position);
            setStatus(player, position);

        }
    }

    @Override
    public void setPositionStatus(NPCPosition position) {
        if (position != null) {
            setPositionMemory(position);
            setStatus(position);
        }
    }

    @Override
    public NPCPosition getPositionMemory() {
        return positionStatus;
    }

    @Override
    public void setPositionMemory(NPCPosition position) {
        positionStatus = position;
    }

    @Override
    public String getCooldownValidator() {
        return cooldownValidator;
    }

    @Override
    public void setCooldownValidator(String cooldownValidator) {
        this.cooldownValidator = cooldownValidator;
    }

    @Override
    public NPCHistory getHistory() {
        return history;
    }

    @Override
    public void setHistory(NPCHistory history) {
        this.history = history;
    }

    @Override
    public void setConfigurationFile(CustomConfiguration configurationFile) {
        this.configuration = configurationFile;
    }

    @Override
    public CustomConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public boolean isLookAtPlayer() {
        return lookAtPlayer;
    }

    @Override
    public void setLookAtPlayer(boolean look) {
        lookAtPlayer = look;
    }

    @Override
    public List<String> getRewardCommands() {
        return rewardCommands;
    }

    @Override
    public void setRewardCommands(List<String> rewardCommands) {
        this.rewardCommands = rewardCommands;
    }

    @Override
    public UUID getUniqueId() {
        return uuid;
    }

    @Override
    public String getUid() {
        return getUniqueId().toString().replace("-", "").substring(0, 16);
    }

    @Override
    public String getId() {
        return name;
    }

    @Override
    public boolean isEditing() {
        return editing;
    }

    @Override
    public void setEditing(boolean editing) {
        this.editing = editing;
    }

    @Override
    public String getNameNPC() {
        return nameNPC;
    }


    @Override
    public NPC setSkin(Skin skin) {
        this.skin = skin;
        gameProfile.getProperties().get("textures").clear();
        if (skin != null) {
            this.forSkin = skin.getFrom();
            gameProfile.getProperties().put("textures", new Property("textures", skin.getValue(), skin.getSignature()));
        }
        updateSkin();
        return this;
    }

    @Override
    public boolean isReady() {
        return isReady;
    }

    @Override
    public void setReady(boolean isReady) {
        this.isReady = isReady;
    }

    @Override
    public String getValueSkin() {
        return forSkin;
    }

    @Override
    public List<String> getCommand() {
        return this.command;
    }

    @Override
    public void setCommand(List<String> command) {
        this.command = new ArrayList<>(command);
    }

    @Override
    public void destroy() {
        isReady = false;
        for (UUID uuid : shown) {
            hide(Bukkit.getPlayer(uuid), false);
        }
        NPCManager.remove(this.nameObject);
        this.shown.clear();
        this.activeStates.clear();
    }

    @Override
    public void setMode(NPCMode modo) {
        this.modo = modo;
    }

    @Override
    public NPCMode getNPCMode() {
        return this.modo;
    }

    @Override
    public void destroyForUpdate() {
        isReady = false;
        for (UUID uuid : shown) {
            hide(Bukkit.getPlayer(uuid), false);
        }
        shown.clear();
        this.activeStates.clear();
    }

    public void disableFOV() {
        this.cosFOV = 0;
    }

    public void setFOV(double fov) {
        this.cosFOV = Math.cos(Math.toRadians(fov));
    }

    public Set<UUID> getShown() {
        return shown;
    }


    @Override
    public String getName() {
        return nameObject;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public World getWorld() {
        return location != null ? location.getWorld() : null;
    }

    @Override
    public int getEntityId() {
        return entityId;
    }

    @Override
    public boolean isShown(Player player) {
        return shown.contains(player.getUniqueId());
    }

    @Override
    public NPC setLocation(Location location) {
        this.location = location;
        return this;
    }

    @Override
    public NPC create() {
        createPackets();
        return this;
    }

    @Override
    public void onLogout(Player player) {
        getShown().remove(player.getUniqueId());
        hasTeamRegistered.remove(player.getUniqueId());
    }

    @Override
    public void lookAt(Player player) {
        if (isLookAtPlayer()) {
            sendLookAtPlayer(player);
        }
    }


    @Override
    public boolean inRangeOf(Player player, int square) {
        if (!player.getWorld().equals(location.getWorld())) {
            return false;
        }
        double distanceSquared = player.getLocation().distanceSquared(location);
        return distanceSquared <= square;
    }

    @Override
    public boolean inRangeOf(Player player) {
        if (!player.getWorld().equals(location.getWorld())) {
            return false;
        }
        double distanceSquared = player.getLocation().distanceSquared(location);
        return distanceSquared <= 2500;
    }

    public boolean inViewOf(Player player) {
        Vector dir = location.toVector().subtract(player.getEyeLocation().toVector()).normalize();
        return dir.dot(player.getLocation().getDirection()) >= cosFOV;
    }


    @Override
    public void show(Player player) {
        try {
            show(player, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void hide(Player player) {
        hide(player, true);
    }

    public void show(Player player, boolean auto) {
        NPCShowEvent event = new NPCShowEvent(this, player, auto);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        if (isShown(player)) {
            throw new IllegalArgumentException("NPC is already shown to player");
        }
        sendShowPackets(player);
        sendMetadataPacket(player);
        sendEquipmentPackets(player);
        shown.add(player.getUniqueId());
    }

    @Override
    public void hide(Player player, boolean removeShown) {
        if (player == null) {
            return;
        }
        NPCHideEvent event = new NPCHideEvent(this, player, removeShown);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }

        if (!shown.contains(player.getUniqueId())) {
            throw new IllegalArgumentException("NPC cannot be hidden from player before calling NPC#show first");
        }
        if (removeShown) {
            shown.remove(player.getUniqueId());
        }
        sendHidePackets(player);
    }

    @Override
    public boolean getState(NPCState state) {
        return activeStates.contains(state);
    }

    @Override
    public NPC toggleState(NPCState state) {
        if (activeStates.contains(state)) {
            activeStates.remove(state);
        } else {
            activeStates.add(state);
        }
        for (UUID shownUuid : shown) {
            Player player = Bukkit.getPlayer(shownUuid);
            if (player != null && isShown(player)) {
                sendMetadataPacket(player);
            }
        }
        return this;
    }

    @Override
    public ItemStack getItem(NPCSlot slot) {
        Objects.requireNonNull(slot, "Slot cannot be null");
        return items.get(slot);
    }

    @Override
    public NPC setHashItem(NPCSlot slot, ItemStack item) {
        Objects.requireNonNull(slot, "Slot cannot be null");
        items.put(slot, item);
        return this;
    }

    @Override
    public NPC setItem(NPCSlot slot, ItemStack item) {
        Objects.requireNonNull(slot, "Slot cannot be null");
        items.put(slot, item);
        for (UUID shownUuid : shown) {
            Player player = Bukkit.getPlayer(shownUuid);
            if (player != null && isShown(player)) {
                sendEquipmentPacket(player, slot, false);
            }
        }
        return this;
    }

    @Override
    public NPC updateText(List<String> text, Player player) {
        if (getText().size() != text.size()) {
            return this;
        }
        ArrayList<String> copy = new ArrayList<>(text);
        if (isShown(player)) {
            hologram.showOrUpdate(player, copy);
        }
        return this;
    }

    @Override
    public NPC setText(List<String> text) {
        if (getText().size() != text.size()) {
            for (UUID shownUuid : shown) {
                Player player = Bukkit.getPlayer(shownUuid);
                if (player != null) {
                    hologram.hide(player);
                }
            }
            hologram.reset(new ArrayList<>(text));
            hologram.createPackets();
            for (UUID shouwn : shown) {
                Player player = Bukkit.getPlayer(shouwn);
                if (player != null) {
                    hologram.showOrUpdate(player, text);
                }
            }
            this.text = new ArrayList<>(text);
            return this;
        }
        for (UUID shownUuid : shown) {
            Player player = Bukkit.getPlayer(shownUuid);
            if (player != null && isShown(player)) {
                hologram.showOrUpdate(player, text);
            }
        }
        this.text = new ArrayList<>(text);
        return this;
    }

    @Override
    public List<String> getText() {
        return text;
    }
}
