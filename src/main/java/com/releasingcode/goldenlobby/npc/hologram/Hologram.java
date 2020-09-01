package com.releasingcode.goldenlobby.npc.hologram;

import com.releasingcode.goldenlobby.GoldenLobby;
import com.releasingcode.goldenlobby.Utils;
import com.releasingcode.goldenlobby.extendido.reflection.Reflection;
import com.releasingcode.goldenlobby.managers.DelayPlayer;
import com.releasingcode.goldenlobby.npc.internal.MinecraftVersion;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Hologram {

    private static final double DELTA = 0.3;

    private static final ArrayList<Hologram> holograms = new ArrayList<>();
    // Classes:
    private static final Class<?> CHAT_COMPONENT_TEXT_CLASS = Reflection.getMinecraftClass("ChatComponentText");
    private static final Class<?> CHAT_BASE_COMPONENT_CLASS = Reflection.getMinecraftClass("IChatBaseComponent");
    private static final Class<?> ENTITY_ARMOR_STAND_CLASS = Reflection.getMinecraftClass("EntityArmorStand");
    private static final Class<?> ENTITY_LIVING_CLASS = Reflection.getMinecraftClass("EntityLiving");
    private static final Class<?> ENTITY_CLASS = Reflection.getMinecraftClass("Entity");
    private static final Class<?> CRAFT_WORLD_CLASS = Reflection.getCraftBukkitClass("CraftWorld");
    private static final Class<?> CRAFT_PLAYER_CLASS = Reflection.getCraftBukkitClass("entity.CraftPlayer");
    private static final Class<?> PACKET_PLAY_OUT_SPAWN_ENTITY_LIVING_CLASS = Reflection.getMinecraftClass(
            "PacketPlayOutSpawnEntityLiving");
    private static final Class<?> PACKET_PLAY_OUT_ENTITY_DESTROY_CLASS = Reflection.getMinecraftClass(
            "PacketPlayOutEntityDestroy");
    private static final Class<?> PACKET_PLAY_OUT_ENTITY_METADATA_CLASS = Reflection.getMinecraftClass(
            "PacketPlayOutEntityMetadata");
    private static final Class<?> DATAWATCHER_CLASS = Reflection.getMinecraftClass("DataWatcher");
    private static final Class<?> ENTITY_PLAYER_CLASS = Reflection.getMinecraftClass("EntityPlayer");
    private static final Class<?> PLAYER_CONNECTION_CLASS = Reflection.getMinecraftClass("PlayerConnection");
    private static final Class<?> PACKET_CLASS = Reflection.getMinecraftClass("Packet");
    // Fields:
    private static final Reflection.FieldAccessor<?> PLAYER_CONNECTION_FIELD = Reflection.getField(ENTITY_PLAYER_CLASS,
            "playerConnection", PLAYER_CONNECTION_CLASS);
    // Constructors:
    private static final Reflection.ConstructorInvoker CHAT_COMPONENT_TEXT_CONSTRUCTOR = Reflection
            .getConstructor(CHAT_COMPONENT_TEXT_CLASS, String.class);
    private static final Reflection.ConstructorInvoker PACKET_PLAY_OUT_SPAWN_ENTITY_LIVING_CONSTRUCTOR = Reflection
            .getConstructor(PACKET_PLAY_OUT_SPAWN_ENTITY_LIVING_CLASS, ENTITY_LIVING_CLASS);
    private static final Reflection.ConstructorInvoker PACKET_PLAY_OUT_ENTITY_DESTROY_CONSTRUCTOR = Reflection
            .getConstructor(PACKET_PLAY_OUT_ENTITY_DESTROY_CLASS, int[].class);
    private static final Reflection.ConstructorInvoker PACKET_PLAY_OUT_ENTITY_METADATA_CONSTRUCTOR = Reflection
            .getConstructor(PACKET_PLAY_OUT_ENTITY_METADATA_CLASS, int.class, DATAWATCHER_CLASS, boolean.class);
    // Methods:
    private static final Reflection.MethodInvoker SET_LOCATION_METHOD = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
            "setLocation", double.class, double.class, double.class, float.class, float.class);
    private static final Reflection.MethodInvoker SET_SMALL_METHOD = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
            "setSmall", boolean.class);
    private static final Reflection.MethodInvoker SET_INVISIBLE_METHOD = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
            "setInvisible", boolean.class);
    private static final Reflection.MethodInvoker SET_BASE_PLATE_METHOD = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
            "setBasePlate", boolean.class);
    private static final Reflection.MethodInvoker SET_ARMS_METHOD = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
            "setArms", boolean.class);
    private static final Reflection.MethodInvoker PLAYER_GET_HANDLE_METHOD = Reflection.getMethod(CRAFT_PLAYER_CLASS,
            "getHandle");
    private static final Reflection.MethodInvoker SEND_PACKET_METHOD = Reflection.getMethod(PLAYER_CONNECTION_CLASS,
            "sendPacket", PACKET_CLASS);
    private static final Reflection.MethodInvoker GET_ID_METHOD = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
            "getId");
    private static final Reflection.MethodInvoker GET_DATAWATCHER_METHOD = Reflection.getMethod(ENTITY_CLASS,
            "getDataWatcher");
    private final ConcurrentHashMap<String, List<String>> playersHologram;

    private final List<Object> armorStands = new ArrayList<>();
    private final List<Object> showPackets = new ArrayList<>();
    private final List<Object> hidePackets = new ArrayList<>();
    private final List<Object> metaPackets = new ArrayList<>();

    private final MinecraftVersion version;
    private Location start;
    private boolean autoEntityController;
    private final Object worldServer;
    private List<String> text;
    private int id;

    public Hologram(MinecraftVersion version, Location location) {
        this.version = version;
        this.start = location;
        autoEntityController = false;
        playersHologram = new ConcurrentHashMap<>();
        this.worldServer = Reflection.getMethod(CRAFT_WORLD_CLASS, "getHandle").invoke(location.getWorld());
        createByMount();
        holograms.add(this);
    }

    public Hologram(MinecraftVersion version, Location location, List<String> text) {
        this.version = version;
        this.start = location;
        this.text = text;
        autoEntityController = false;
        playersHologram = new ConcurrentHashMap<>();
        this.worldServer = Reflection.getMethod(CRAFT_WORLD_CLASS, "getHandle").invoke(location.getWorld());
        createPackets();
        holograms.add(this);
    }

    public void setStart(Location newLocation) {
        this.start = newLocation;
    }


    private static void clearHologramToPlayer(Player player) {
        getHolograms().forEach(Hologram -> {
            Hologram.clearPlayer(player);
        });
    }


    private static ArrayList<Hologram> getHolograms() {
        return holograms;
    }

    public List<String> getText() {
        return text;
    }

    public void createByMount() {
        Reflection.MethodInvoker gravityMethod = (version.isAboveOrEqual(MinecraftVersion.V1_10_R1) ?
                Reflection.getMethod(ENTITY_CLASS, "setNoGravity", boolean.class) :
                Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS, "setGravity", boolean.class));

        Reflection.MethodInvoker customNameVisibilityMethod = Reflection
                .getMethod(ENTITY_CLASS, "setCustomNameVisible", boolean.class);

        Location location = start.clone();
        Class<?> worldClass = worldServer.getClass().getSuperclass();
        if (start.getWorld().getEnvironment() != World.Environment.NORMAL) {
            worldClass = worldClass.getSuperclass();
        }

        Reflection.ConstructorInvoker entityArmorStandConstructor;
        try {
            entityArmorStandConstructor = (version.isAboveOrEqual(MinecraftVersion.V1_14_R1) ?
                    Reflection.getConstructor(ENTITY_ARMOR_STAND_CLASS, worldClass, double.class, double.class,
                            double.class) :
                    Reflection.getConstructor(ENTITY_ARMOR_STAND_CLASS, worldClass));
        } catch (IllegalStateException exception) {
            worldClass = worldClass.getSuperclass();

            entityArmorStandConstructor = (version.isAboveOrEqual(MinecraftVersion.V1_14_R1) ?
                    Reflection.getConstructor(ENTITY_ARMOR_STAND_CLASS, worldClass, double.class, double.class,
                            double.class) :
                    Reflection.getConstructor(ENTITY_ARMOR_STAND_CLASS, worldClass));
        }
        // end #59


        Object entityArmorStand = (version.isAboveOrEqual(MinecraftVersion.V1_14_R1) ?
                entityArmorStandConstructor.invoke(worldServer, location.getX(), location.getY(), location.getZ()) :
                entityArmorStandConstructor.invoke(worldServer));

        if (!version.isAboveOrEqual(MinecraftVersion.V1_14_R1)) {
            SET_LOCATION_METHOD.invoke(entityArmorStand, location.getX(), location.getY(), location.getZ(), 0, 0);
        }

        customNameVisibilityMethod.invoke(entityArmorStand, false);
        gravityMethod.invoke(entityArmorStand, version.isAboveOrEqual(MinecraftVersion.V1_9_R2));
        SET_SMALL_METHOD.invoke(entityArmorStand, true);
        SET_INVISIBLE_METHOD.invoke(entityArmorStand, true);
        SET_BASE_PLATE_METHOD.invoke(entityArmorStand, false);
        SET_ARMS_METHOD.invoke(entityArmorStand, false);

        armorStands.add(entityArmorStand);

        // Create and add the associated show and hide packets.
        showPackets.add(PACKET_PLAY_OUT_SPAWN_ENTITY_LIVING_CONSTRUCTOR.invoke(entityArmorStand));
        id = (int) GET_ID_METHOD.invoke(entityArmorStand);
        hidePackets.add(PACKET_PLAY_OUT_ENTITY_DESTROY_CONSTRUCTOR
                .invoke(new int[]{id}));
        // For 1.15 R1 and up.
        metaPackets.add(PACKET_PLAY_OUT_ENTITY_METADATA_CONSTRUCTOR.invoke(
                GET_ID_METHOD.invoke(entityArmorStand),
                GET_DATAWATCHER_METHOD.invoke(entityArmorStand),
                true));

    }

    public void createPackets() {
        Reflection.MethodInvoker gravityMethod = (version.isAboveOrEqual(MinecraftVersion.V1_10_R1) ?
                Reflection.getMethod(ENTITY_CLASS, "setNoGravity", boolean.class) :
                Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS, "setGravity", boolean.class));

        Reflection.MethodInvoker customNameMethod = Reflection.getMethod(ENTITY_CLASS, "setCustomName",
                version.isAboveOrEqual(MinecraftVersion.V1_13_R1) ? CHAT_BASE_COMPONENT_CLASS : String.class);

        Reflection.MethodInvoker customNameVisibilityMethod = Reflection
                .getMethod(ENTITY_CLASS, "setCustomNameVisible", boolean.class);

        Location location = start.clone().add(0, DELTA * text.size(), 0);
        Class<?> worldClass = worldServer.getClass().getSuperclass();
        if (start.getWorld().getEnvironment() != World.Environment.NORMAL) {
            worldClass = worldClass.getSuperclass();
        }

        Reflection.ConstructorInvoker entityArmorStandConstructor;
        try {
            entityArmorStandConstructor = (version.isAboveOrEqual(MinecraftVersion.V1_14_R1) ?
                    Reflection.getConstructor(ENTITY_ARMOR_STAND_CLASS, worldClass, double.class, double.class,
                            double.class) :
                    Reflection.getConstructor(ENTITY_ARMOR_STAND_CLASS, worldClass));
        } catch (IllegalStateException exception) {
            worldClass = worldClass.getSuperclass();

            entityArmorStandConstructor = (version.isAboveOrEqual(MinecraftVersion.V1_14_R1) ?
                    Reflection.getConstructor(ENTITY_ARMOR_STAND_CLASS, worldClass, double.class, double.class,
                            double.class) :
                    Reflection.getConstructor(ENTITY_ARMOR_STAND_CLASS, worldClass));
        }
        // end #59

        for (String line : text) {
            Object entityArmorStand = (version.isAboveOrEqual(MinecraftVersion.V1_14_R1) ?
                    entityArmorStandConstructor.invoke(worldServer, location.getX(), location.getY(), location.getZ()) :
                    entityArmorStandConstructor.invoke(worldServer));

            if (!version.isAboveOrEqual(MinecraftVersion.V1_14_R1)) {
                SET_LOCATION_METHOD.invoke(entityArmorStand, location.getX(), location.getY(), location.getZ(), 0, 0);
            }

            customNameMethod.invoke(entityArmorStand, version.isAboveOrEqual(MinecraftVersion.V1_13_R1) ?
                    CHAT_COMPONENT_TEXT_CONSTRUCTOR.invoke(line) : line);
            customNameVisibilityMethod.invoke(entityArmorStand, true);
            gravityMethod.invoke(entityArmorStand, version.isAboveOrEqual(MinecraftVersion.V1_9_R2));
            SET_SMALL_METHOD.invoke(entityArmorStand, true);
            SET_INVISIBLE_METHOD.invoke(entityArmorStand, true);
            SET_BASE_PLATE_METHOD.invoke(entityArmorStand, false);
            SET_ARMS_METHOD.invoke(entityArmorStand, false);

            armorStands.add(entityArmorStand);

            // Create and add the associated show and hide packets.
            showPackets.add(PACKET_PLAY_OUT_SPAWN_ENTITY_LIVING_CONSTRUCTOR.invoke(entityArmorStand));
            hidePackets.add(PACKET_PLAY_OUT_ENTITY_DESTROY_CONSTRUCTOR
                    .invoke(new int[]{(int) GET_ID_METHOD.invoke(entityArmorStand)}));
            // For 1.15 R1 and up.
            metaPackets.add(PACKET_PLAY_OUT_ENTITY_METADATA_CONSTRUCTOR.invoke(
                    GET_ID_METHOD.invoke(entityArmorStand),
                    GET_DATAWATCHER_METHOD.invoke(entityArmorStand),
                    true));

            location.subtract(0, DELTA, 0);
        }
    }

    private void clearPlayer(Player player) {
        this.playersHologram.remove(player.getName().toLowerCase());
    }

    public List<Object> getUpdatePackets(List<String> text) {
        List<Object> updatePackets = new ArrayList<>();

        if (this.text.size() != text.size()) {
            throw new IllegalArgumentException(
                    "When updating the text, the old and new text should have the same amount of lines");
        }

        Reflection.MethodInvoker customNameMethod = Reflection.getMethod(ENTITY_CLASS, "setCustomName",
                version.isAboveOrEqual(MinecraftVersion.V1_13_R1) ? CHAT_BASE_COMPONENT_CLASS : String.class);

        for (int i = 0; i < text.size(); i++) {
            Object entityArmorStand = armorStands.get(i);
            String oldLine = this.text.get(i);
            String newLine = text.get(i);
            customNameMethod.invoke(entityArmorStand, version.isAboveOrEqual(MinecraftVersion.V1_13_R1) ?
                    CHAT_COMPONENT_TEXT_CONSTRUCTOR.invoke(newLine) : newLine); // Update the DataWatcher object.
            showPackets.set(i, PACKET_PLAY_OUT_SPAWN_ENTITY_LIVING_CONSTRUCTOR.invoke(entityArmorStand));

            if (newLine.isEmpty() && !oldLine.trim().isEmpty()) {
                updatePackets.add(hidePackets.get(i));
            } else if (!newLine.isEmpty() && oldLine.trim().isEmpty()) {
                updatePackets.add(showPackets.get(i));
            } else if (!oldLine.equals(newLine)) {
                // Update the line for all players using a Metadata packet.
                updatePackets.add(PACKET_PLAY_OUT_ENTITY_METADATA_CONSTRUCTOR.invoke(
                        GET_ID_METHOD.invoke(entityArmorStand),
                        GET_DATAWATCHER_METHOD.invoke(entityArmorStand),
                        true
                ));
            } else {
                updatePackets.add(showPackets.get(i));
            }
        }

        this.text = text;

        return updatePackets;
    }

    public void reset(List<String> text) {
        destroy();
        this.text = text;
    }


    public List<Object> getUpdatePacketsPerPlayer(Player player, List<String> text) {
        List<Object> updatePackets = new ArrayList<>();
        List<String> oldText = playersHologram.getOrDefault(player.getName().toLowerCase(), this.text);
        if (oldText.size() != text.size()) {
            Utils.log("ERROR[diff]_> " + text.toString());
            throw new IllegalArgumentException(
                    "When updating the text, the old and new text should have the same amount of lines");
        }
        Reflection.MethodInvoker customNameMethod = Reflection.getMethod(ENTITY_CLASS, "setCustomName",
                version.isAboveOrEqual(MinecraftVersion.V1_13_R1) ? CHAT_BASE_COMPONENT_CLASS : String.class);
        for (int i = 0; i < text.size(); i++) {
            Object entityArmorStand = armorStands.get(i);
            String oldLine = oldText.get(i);
            String newLine = text.get(i);
            customNameMethod.invoke(entityArmorStand, version.isAboveOrEqual(MinecraftVersion.V1_13_R1) ?
                    CHAT_COMPONENT_TEXT_CONSTRUCTOR.invoke(newLine) : newLine); // Update the DataWatcher object.
            showPackets.set(i, PACKET_PLAY_OUT_SPAWN_ENTITY_LIVING_CONSTRUCTOR.invoke(entityArmorStand));

            if (newLine.isEmpty() && !oldLine.trim().isEmpty()) {
                updatePackets.add(hidePackets.get(i));
            } else if (!newLine.isEmpty() && oldLine.trim().isEmpty() || !playersHologram.containsKey(player.getName().toLowerCase())) {
                updatePackets.add(showPackets.get(i));
            } else if (!oldLine.equals(newLine)) {
                // Update the line for all players using a Metadata packet.
                updatePackets.add(PACKET_PLAY_OUT_ENTITY_METADATA_CONSTRUCTOR.invoke(
                        GET_ID_METHOD.invoke(entityArmorStand),
                        GET_DATAWATCHER_METHOD.invoke(entityArmorStand),
                        true
                ));
            }
        }
        playersHologram.put(player.getName().toLowerCase(), text);
        return updatePackets;
    }

    public void show(Player player) {
        Object show = showPackets.get(0);
        Object playerConnection = PLAYER_CONNECTION_FIELD.get(PLAYER_GET_HANDLE_METHOD.invoke(player));
        SEND_PACKET_METHOD.invoke(playerConnection, show);
        playersHologram.put(player.getName().toLowerCase(), new ArrayList<>());
    }

    public void showOrUpdate(Player player, List<String> text) {
        if (text.size() == 1 || text.size() == 0) {
            if (text.size() == 1) {
                if (text.get(0).trim().isEmpty()) {
                    return;
                }
            }
        }
        List<Object> updatePackets = getUpdatePacketsPerPlayer(player, text);
        Object playerConnection = PLAYER_CONNECTION_FIELD.get(PLAYER_GET_HANDLE_METHOD.invoke(player));
        for (Object packet : updatePackets) {
            SEND_PACKET_METHOD.invoke(playerConnection, packet);
        }
    }

    public boolean isAutoEntityController() {
        return autoEntityController;
    }

    public void setAutoEntityController(boolean autoEntityController) {
        this.autoEntityController = autoEntityController;
    }

    public void clearExceptText() {
        armorStands.clear();
        hidePackets.clear();
        showPackets.clear();
        metaPackets.clear();
        playersHologram.clear();
    }

    public void destroy() {
        armorStands.clear();
        hidePackets.clear();
        showPackets.clear();
        metaPackets.clear();
        playersHologram.clear();
        text.clear();
    }

    public boolean isShowing(Player player) {
        return playersHologram.containsKey(player.getName().toLowerCase());
    }

    public void hideOnlyHologram(Player player) {
        try {
            playersHologram.remove(player.getName().toLowerCase());
            Object playerConnection = PLAYER_CONNECTION_FIELD.get(PLAYER_GET_HANDLE_METHOD.invoke(player));
            SEND_PACKET_METHOD.invoke(playerConnection, hidePackets.get(0));
        } catch (Exception e) {

        }
    }

    public void hide(Player player) {
        playersHologram.remove(player.getName().toLowerCase());
        Object playerConnection = PLAYER_CONNECTION_FIELD.get(PLAYER_GET_HANDLE_METHOD.invoke(player));
        for (int i = 0; i < text.size(); i++) {
            if (text.get(i).isEmpty()) continue;
            SEND_PACKET_METHOD.invoke(playerConnection, hidePackets.get(i));
        }
    }

    public int getId() {
        return id;
    }

    public static class HologramListener implements Listener {
        public HologramListener() {
            GoldenLobby.getInstance().getServer().getPluginManager().registerEvents(this, GoldenLobby.getInstance());
        }

        @EventHandler
        public void onQuit(PlayerQuitEvent event) {
            Hologram.clearHologramToPlayer(event.getPlayer());
        }

        @EventHandler
        public void onTeleport(PlayerTeleportEvent event) {
            if (event.getCause().equals(PlayerTeleportEvent.TeleportCause.PLUGIN)) {
                for (Hologram h : Hologram.getHolograms()) {
                    if (h.isAutoEntityController()) {
                        h.hide(event.getPlayer());
                    }
                }
                DelayPlayer.addDelay(event.getPlayer(), "show_hologram", 500);
            }
        }
    }

}
