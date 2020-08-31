package com.releasingcode.goldenlobby.managers;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import com.releasingcode.goldenlobby.extendido.nms.NMSNetwork;

public class Potion1_9 {
    private PotionType type;
    private boolean strong;
    private boolean _long;
    private PotionIdType idType;

    public Potion1_9(PotionType potionType, boolean bl, boolean bl2, PotionIdType potionIdType) {
        this.type = potionType;
        this.strong = bl;
        this._long = bl2;
        this.idType = potionIdType;
    }

    public static Potion1_9 fromItemStack(ItemStack itemStack) {
        try {
            if (itemStack != null && (itemStack.getType().equals(Material.POTION) || itemStack.getType().equals(Material.SPLASH_POTION) || itemStack.getType().equals(Material.LINGERING_POTION) || itemStack.getType().equals(Material.TIPPED_ARROW))) {
                Object object;
                Object object2 = NMSNetwork.getOBCClass("inventory.CraftItemStack").getMethod("asNMSCopy", ItemStack.class).invoke(null, itemStack);
                Object object3 = object2.getClass().getMethod("getTag", new Class[0]).invoke(object2);
                String string = (String) object3.getClass().getMethod("getString", String.class).invoke(object3, "Potion");
                String string2 = string.replace("minecraft:", "");
                PotionType potionType = null;
                boolean bl = string2.contains("strong");
                boolean bl2 = string2.contains("long");
                switch (string2) {
                    case "fire_restistance":
                    case "long_fire_resistance": {
                        potionType = PotionType.FIRE_RESISTANCE;
                        break;
                    }
                    case "harming":
                    case "strong_harming": {
                        potionType = PotionType.INSTANT_DAMAGE;
                        break;
                    }
                    case "healing":
                    case "strong_healing": {
                        potionType = PotionType.INSTANT_HEAL;
                        break;
                    }
                    case "invisibility":
                    case "long_invisibility": {
                        potionType = PotionType.INVISIBILITY;
                        break;
                    }
                    case "leaping":
                    case "long_leaping":
                    case "strong_leaping": {
                        potionType = PotionType.JUMP;
                        break;
                    }
                    case "luck": {
                        potionType = PotionType.LUCK;
                        break;
                    }
                    case "night_vision":
                    case "long_night_vision": {
                        potionType = PotionType.NIGHT_VISION;
                        break;
                    }
                    case "poison":
                    case "long_poison":
                    case "strong_poison": {
                        potionType = PotionType.POISON;
                        break;
                    }
                    case "regeneration":
                    case "long_regeneration":
                    case "strong_regeneration": {
                        potionType = PotionType.REGEN;
                        break;
                    }
                    case "slowness":
                    case "long_slowness": {
                        potionType = PotionType.SLOWNESS;
                        break;
                    }
                    case "swiftness":
                    case "long_swiftness":
                    case "strong_swiftness": {
                        potionType = PotionType.SPEED;
                        break;
                    }
                    case "strength":
                    case "long_strength":
                    case "strong_strength": {
                        potionType = PotionType.STRENGTH;
                        break;
                    }
                    case "water_breathing":
                    case "long_water_breathing": {
                        potionType = PotionType.WATER_BREATHING;
                        break;
                    }
                    case "water": {
                        potionType = PotionType.WATER;
                        break;
                    }
                    case "weakness":
                    case "long_weakness": {
                        potionType = PotionType.WEAKNESS;
                        break;
                    }
                    case "empty": {
                        potionType = PotionType.EMPTY;
                        break;
                    }
                    case "mundane": {
                        potionType = PotionType.MUNDANE;
                        break;
                    }
                    case "thick": {
                        potionType = PotionType.THICK;
                        break;
                    }
                    case "awkward": {
                        potionType = PotionType.AWKWARD;
                        break;
                    }
                    default: {
                        potionType = null;
                    }
                }
                switch (itemStack.getType()) {
                    case LINGERING_POTION: {
                        object = PotionIdType.lingeling;
                        break;
                    }
                    case SPLASH_POTION: {
                        object = PotionIdType.splash;
                        break;
                    }
                    case TIPPED_ARROW: {
                        object = PotionIdType.arrow;
                        break;
                    }
                    default: {
                        object = PotionIdType.normal;
                    }
                }
                return new Potion1_9(potionType, bl, bl2, (PotionIdType) object);
            }
            return null;
        } catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }

    public ItemStack toItemStack(int n) {
        try {
            ItemStack itemStack;
            switch (this.idType) {
                case lingeling: {
                    itemStack = new ItemStack(Material.LINGERING_POTION, n);
                    break;
                }
                case splash: {
                    itemStack = new ItemStack(Material.SPLASH_POTION, n);
                    break;
                }
                case arrow: {
                    itemStack = new ItemStack(Material.TIPPED_ARROW, n);
                    break;
                }
                default: {
                    itemStack = new ItemStack(Material.POTION, n);
                }
            }
            Object object = NMSNetwork.getOBCClass("inventory.CraftItemStack").getMethod("asNMSCopy", ItemStack.class).invoke(null, itemStack);
            Object object2 = object.getClass().getMethod("getTag", new Class[0]).invoke(object);
            if (object2 == null) {
                object2 = NMSNetwork.getNMSClass("NBTTagCompound").newInstance();
            }
            String string = "";
            string = this.type == null ? null : (this.type.equals(PotionType.FIRE_RESISTANCE) ? (this._long ? "long_fire_resistance" : "fire_restistance") : (this.type.equals(PotionType.INSTANT_DAMAGE) ? (this.strong ? "strong_harming" : "harming") : (this.type.equals(PotionType.INSTANT_HEAL) ? (this.strong ? "strong_healing" : "healing") : (this.type.equals(PotionType.INVISIBILITY) ? (this._long ? "long_invisibility" : "invisibility") : (this.type.equals(PotionType.JUMP) ? (this._long ? "long_leaping" : (this.strong ? "strong_leaping" : "leaping")) : (this.type.equals(PotionType.LUCK) ? "luck" : (this.type.equals(PotionType.NIGHT_VISION) ? (this._long ? "long_night_vision" : "night_vision") : (this.type.equals(PotionType.POISON) ? (this._long ? "long_poison" : (this.strong ? "strong_poison" : "poison")) : (this.type.equals(PotionType.REGEN) ? (this._long ? "long_regeneration" : (this.strong ? "strong_regeneration" : "regeneration")) : (this.type.equals(PotionType.SLOWNESS) ? (this._long ? "long_slowness" : "slowness") : (this.type.equals(PotionType.SPEED) ? (this._long ? "long_swiftness" : (this.strong ? "strong_swiftness" : "swiftness")) : (this.type.equals(PotionType.STRENGTH) ? (this._long ? "long_strength" : (this.strong ? "strong_strength" : "strength")) : (this.type.equals(PotionType.WATER_BREATHING) ? (this._long ? "long_water_breathing" : "water_breathing") : (this.type.equals(PotionType.WATER) ? "water" : (this.type.equals(PotionType.WEAKNESS) ? (this._long ? "long_weakness" : "weakness") : (this.type.equals(PotionType.EMPTY) ? "empty" : (this.type.equals(PotionType.MUNDANE) ? "mundane" : (this.type.equals(PotionType.THICK) ? "thick" : (this.type.equals(PotionType.AWKWARD) ? "awkward" : null)))))))))))))))))));
            if (string != null) {
                object2.getClass().getMethod("setString", String.class, String.class).invoke(object2, "Potion", "minecraft:" + string);
            }
            object.getClass().getMethod("setTag", object2.getClass()).invoke(object, object2);
            return (ItemStack) NMSNetwork.getOBCClass("inventory.CraftItemStack").getMethod("asBukkitCopy", object.getClass()).invoke(null, object);
        } catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }

    public PotionType getType() {
        return this.type;
    }

    public void setType(PotionType potionType) {
        this.type = potionType;
    }

    public boolean isStrong() {
        return this.strong;
    }

    public void setStrong(boolean bl) {
        this.strong = bl;
    }

    public boolean isLong() {
        return this._long;
    }

    public void setLong(boolean bl) {
        this._long = bl;
    }

    public PotionIdType getIdType() {
        return this.idType;
    }

    public void setIdType(PotionIdType potionIdType) {
        this.idType = potionIdType;
    }

    public enum PotionIdType {
        normal,
        splash,
        lingeling,
        arrow;


        PotionIdType() {
        }

        public static PotionIdType fromString(String string) {
            switch (string.toLowerCase()) {
                case "true":
                case "splash": {
                    return splash;
                }
                case "lingeling": {
                    return lingeling;
                }
                case "arrow": {
                    return arrow;
                }
            }
            return normal;
        }
    }

    public enum PotionType {
        FIRE_RESISTANCE,
        INSTANT_DAMAGE,
        INSTANT_HEAL,
        INVISIBILITY,
        JUMP,
        LUCK,
        NIGHT_VISION,
        POISON,
        REGEN,
        SLOWNESS,
        SPEED,
        STRENGTH,
        WATER,
        WATER_BREATHING,
        WEAKNESS,
        EMPTY,
        MUNDANE,
        THICK,
        AWKWARD;


        PotionType() {
        }

        public static PotionType fromString(String string) {
            try {
                return PotionType.valueOf(string.toUpperCase());
            } catch (Exception exception) {
                return null;
            }
        }
    }

}

