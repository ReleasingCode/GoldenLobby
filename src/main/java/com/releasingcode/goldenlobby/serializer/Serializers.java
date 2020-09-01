package com.releasingcode.goldenlobby.serializer;

import com.releasingcode.goldenlobby.serializer.Object.ColorSerializer;
import com.releasingcode.goldenlobby.serializer.Object.ItemStackSerializer;
import com.releasingcode.goldenlobby.serializer.Object.MaterialSerializer;
import com.releasingcode.goldenlobby.serializer.Object.PotionEffectSerializer;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public enum Serializers {
    color(new ColorSerializer(), Color.class),
    itemStack(new ItemStackSerializer(), ItemStack.class),
    potionEffect(new PotionEffectSerializer(), PotionEffect.class),
    material(new MaterialSerializer(), Material.class);

    private final Serializer<?> serializer;
    private final Class<?> classSer;

    Serializers(Serializer<?> serializer, Class<?> class_) {
        this.serializer = serializer;
        this.classSer = class_;
    }

    public static <E> Serializer<E> getSerializer(Class<E> class_) {
        for (Serializers serializers : Serializers.values()) {
            if (!class_.getName().equals(serializers.getClassSer().getName())) continue;
            return ((Serializer<E>) serializers.getSerializer());
        }
        return null;
    }

    public static <E> List<E> deserializeList(Collection<String> collection, Class<E> class_) {
        ArrayList<E> arrayList = new ArrayList<E>();
        Serializer<E> serializer = Serializers.getSerializer(class_);
        if (serializer == null) {
            return arrayList;
        }
        for (String string : collection) {
            E e = serializer.deserialize(string);
            if (e == null) continue;
            arrayList.add(e);
        }
        return arrayList;
    }

    public static <E> List<String> serializeList(Collection<E> collection, Class<E> class_) {
        ArrayList<String> arrayList = new ArrayList<String>();
        Serializer<E> serializer = Serializers.getSerializer(class_);
        if (serializer == null) {
            for (E e : collection) {
                arrayList.add(e.toString());
            }
            return arrayList;
        }
        for (E e : collection) {
            String string = serializer.serialize(e);
            arrayList.add(string);
        }
        return arrayList;
    }

    public static String listToString(Collection<?> collection, String string) {
        String string2 = "";
        for (Object obj : collection) {
            string2 = string2 + string + obj.toString();
        }
        return string2.replaceFirst(string, "");
    }

    public static List<String> stringToList(String string, String string2) {
        return Arrays.asList(string.split(string2));
    }

    public static <E> String superSerializer(Collection<E> collection, Class<E> class_, String string) {
        return Serializers.listToString(Serializers.serializeList(collection, class_), string);
    }

    public static <E> List<E> superDeserializer(String string, Class<E> class_, String string2) {
        return Serializers.deserializeList(Serializers.stringToList(string, string2), class_);
    }

    public static String listToBytes(Collection<?> collection, String string) {
        StringBuilder string2 = new StringBuilder();
        for (Object obj : collection) {
            byte[] arrby = obj.toString().getBytes();
            String string3 = "";
            for (byte by : arrby) {
                string3 = string3 + "s" + String.valueOf(by);
            }
            string2.append(string).append(string3.replaceFirst("s", ""));
        }
        return string2.toString().replaceFirst(string, "");
    }

    public static List<byte[]> bytesToList(String string, String string2) {
        String[] arrstring = string.split(string2);
        ArrayList<byte[]> arrayList = new ArrayList<byte[]>();
        for (String string3 : arrstring) {
            List<String> list = Serializers.stringToList(string3, "s");
            byte[] arrby = new byte[list.size()];
            for (int i = 0; i < list.size(); ++i) {
                arrby[i] = new Byte(list.get(i));
            }
            arrayList.add(arrby);
        }
        return arrayList;
    }

    public static <E> List<E> superByteDeserializer(String string, Class<E> class_, String string2) {
        List<byte[]> list = Serializers.bytesToList(string, string2);
        ArrayList<String> arrayList = new ArrayList<String>();
        for (byte[] arrby : list) {
            arrayList.add(new String(arrby));
        }
        return Serializers.deserializeList(arrayList, class_);
    }

    public static String stringToByteString(String string) {
        byte[] arrby = string.getBytes();
        String string2 = "";
        for (byte by : arrby) {
            string2 = string2 + "s" + String.valueOf(by);
        }
        return string2.replaceFirst("s", "");
    }

    public static String byteStringToString(String string) {
        String[] arrstring = string.split("s");
        byte[] arrby = new byte[arrstring.length];
        for (int i = 0; i < arrstring.length; ++i) {
            arrby[i] = Byte.parseByte(arrstring[i]);
        }
        return new String(arrby);
    }

    public Serializer<?> getSerializer() {
        return this.serializer;
    }

    public Class<?> getClassSer() {
        return this.classSer;
    }
}

