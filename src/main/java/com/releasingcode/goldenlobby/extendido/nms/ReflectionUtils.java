package com.releasingcode.goldenlobby.extendido.nms;

import org.bukkit.Bukkit;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public final class ReflectionUtils {
    public static Constructor<?> getConstructor(final Class<?> clazz, final Class<?>... array) throws NoSuchMethodException {
        final Class<?>[] primitive = DataType.getPrimitive(array);
        for (final Constructor constructor : clazz.getConstructors()) {
            if (!DataType.compare(DataType.getPrimitive(constructor.getParameterTypes()), primitive)) {
                return (Constructor<?>) constructor;
            }
        }
        throw new NoSuchMethodException("There is no such constructor in this class with the specified parameter types");
    }

    public static Constructor<?> getConstructor(final String s, final PackageType packageType, final Class<?>... array) throws NoSuchMethodException, ClassNotFoundException {
        return getConstructor(packageType.getClass(s), array);
    }

    public static Field getField(final Class<?> clazz, final boolean b, final String s) throws NoSuchFieldException {
        final Field field = b ? clazz.getDeclaredField(s) : clazz.getField(s);
        field.setAccessible(true);
        return field;
    }

    public static Field getField(final String s, final PackageType packageType, final boolean b, final String s2) throws NoSuchFieldException, ClassNotFoundException {
        return getField(packageType.getClass(s), b, s2);
    }

    public static Method getMethod(final Class<?> clazz, final String s, final Class<?>... array) throws NoSuchMethodException {
        final Class<?>[] primitive = DataType.getPrimitive(array);
        for (final Method method : clazz.getMethods()) {
            if (method.getName().equals(s) && !DataType.compare(DataType.getPrimitive(method.getParameterTypes()), primitive)) {
                return method;
            }
        }
        throw new NoSuchMethodException("There is no such method in this class with the specified name and parameter types");
    }

    public static Object invokeConstructor(final Class<?> clazz, final Class<?>[] array, final Object... array2) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return clazz.getConstructor(array).newInstance(array2);
    }

    public static Method getMethod(final String s, final PackageType packageType, final String s2, final Class<?>... array) throws NoSuchMethodException, ClassNotFoundException {
        return getMethod(packageType.getClass(s), s2, array);
    }

    public static Object getValue(final Object o, final boolean b, final String s) throws NoSuchFieldException, IllegalAccessException {
        return getValue(o, o.getClass(), b, s);
    }

    public static Object getValue(final Object o, final Class<?> clazz, final boolean b, final String s) throws NoSuchFieldException, IllegalAccessException {
        return getField(clazz, b, s).get(o);
    }

    public static Object getValue(final Object o, final String s, final PackageType packageType, final boolean b, final String s2) throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        return getValue(o, packageType.getClass(s), b, s2);
    }

    public static Object instantiateObject(final Class<?> clazz, final Object... array) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return getConstructor(clazz, DataType.getPrimitive(array)).newInstance(array);
    }

    public static Object instantiateObject(final String s, final PackageType packageType, final Object... array) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        return instantiateObject(packageType.getClass(s), array);
    }

    public static Object invokeMethod(final Object o, final Class<?> clazz, final String s, final Object... array) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        return getMethod(clazz, s, DataType.getPrimitive(array)).invoke(o, array);
    }

    public static Object invokeMethod(final Object o, final String s, final Object... array) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        return getMethod(o.getClass(), s, DataType.getPrimitive(array)).invoke(o, array);
    }

    public static Object invokeMethod(final Object o, final String s, final PackageType packageType, final String s2, final Object... array) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        return invokeMethod(o, packageType.getClass(s), s2, array);
    }

    public static void setValue(final Object o, final boolean b, final String s, final Object o2) throws NoSuchFieldException, IllegalAccessException {
        setValue(o, o.getClass(), b, s, o2);
    }

    public static void setValue(final Object o, final Class<?> clazz, final boolean b, final String s, final Object o2) throws NoSuchFieldException, IllegalAccessException {
        getField(clazz, b, s).set(o, o2);
    }

    public static void setValue(final Object o, final String s, final PackageType packageType, final boolean b, final String s2, final Object o2) throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        setValue(o, packageType.getClass(s), b, s2, o2);
    }

    public static Object getObject(final Object o, final String s) throws NoSuchFieldException, IllegalAccessException {
        final Field declaredField = o.getClass().getDeclaredField(s);
        final boolean accessible = declaredField.isAccessible();
        if (!accessible) {
            declaredField.setAccessible(true);
        }
        final Object value = declaredField.get(o);
        if (!accessible) {
            declaredField.setAccessible(false);
        }
        return value;
    }

    public static Object getFirstObject(final Class<?> clazz, final Class<?> clazz2, final Object o) throws IllegalAccessException {
        Field field = null;
        for (final Field field2 : clazz.getDeclaredFields()) {
            if (field2.getType().equals(clazz2)) {
                field = field2;
                break;
            }
        }
        if (field == null) {
            for (final Field field3 : clazz.getFields()) {
                if (field3.getType().equals(clazz2)) {
                    field = field3;
                    break;
                }
            }
        }
        field.setAccessible(true);
        return field.get(o);
    }

    public static Enum<?> getEnum(final Class<?> clazz, final String s, final String s2) throws Exception {
        for (final Enum enum1 : (Enum[]) Class.forName(clazz.getName() + "$" + s).getEnumConstants()) {
            if (enum1.name().equalsIgnoreCase(s2)) {
                return (Enum<?>) enum1;
            }
        }
        throw new Exception("Enum constant not found " + s2);
    }

    public static Enum<?> getEnum(final Class<?> clazz, final String s) throws Exception {
        for (final Enum enum1 : (Enum[]) Class.forName(clazz.getName()).getEnumConstants()) {
            if (enum1.name().equalsIgnoreCase(s)) {
                return (Enum<?>) enum1;
            }
        }
        throw new Exception("Enum constant not found " + s);
    }

    public enum DataType {
        BYTE(Byte.TYPE, Byte.class),
        SHORT(Short.TYPE, Short.class),
        INTEGER(Integer.TYPE, Integer.class),
        LONG(Long.TYPE, Long.class),
        CHARACTER(Character.TYPE, Character.class),
        FLOAT(Float.TYPE, Float.class),
        DOUBLE(Double.TYPE, Double.class),
        BOOLEAN(Boolean.TYPE, Boolean.class);

        private static final Map<Class<?>, DataType> CLASS_MAP;

        static {
            CLASS_MAP = new HashMap<Class<?>, DataType>();
            for (final DataType dataType : values()) {
                DataType.CLASS_MAP.put(dataType.primitive, dataType);
                DataType.CLASS_MAP.put(dataType.reference, dataType);
            }
        }

        private final Class<?> primitive;
        private final Class<?> reference;

        DataType(final Class<?> primitive, final Class<?> reference) {
            this.primitive = primitive;
            this.reference = reference;
        }

        public static boolean compare(final Class<?>[] array, final Class<?>[] array2) {
            if (array == null || array2 == null || array.length != array2.length) {
                return true;
            }
            for (int i = 0; i < array.length; ++i) {
                final Class<?> clazz = array[i];
                final Class<?> clazz2 = array2[i];
                if (!clazz.equals(clazz2) && !clazz.isAssignableFrom(clazz2)) {
                    return true;
                }
            }
            return false;
        }

        public static DataType fromClass(final Class<?> clazz) {
            return DataType.CLASS_MAP.get(clazz);
        }

        public static Class<?> getPrimitive(final Class<?> clazz) {
            final DataType fromClass = fromClass(clazz);
            return (fromClass == null) ? clazz : fromClass.getPrimitive();
        }

        public static Class<?>[] getPrimitive(final Class<?>[] array) {
            final int n = (array == null) ? 0 : array.length;
            final Class[] array2 = new Class[n];
            for (int i = 0; i < n; ++i) {
                array2[i] = getPrimitive(array[i]);
            }
            return (Class<?>[]) array2;
        }

        public static Class<?>[] getPrimitive(final Object[] array) {
            final int n = (array == null) ? 0 : array.length;
            final Class[] array2 = new Class[n];
            for (int i = 0; i < n; ++i) {
                array2[i] = getPrimitive(array[i].getClass());
            }
            return (Class<?>[]) array2;
        }

        public static Class<?> getReference(final Class<?> clazz) {
            final DataType fromClass = fromClass(clazz);
            return (fromClass == null) ? clazz : fromClass.getReference();
        }

        public static Class<?>[] getReference(final Class<?>[] array) {
            final int n = (array == null) ? 0 : array.length;
            final Class[] array2 = new Class[n];
            for (int i = 0; i < n; ++i) {
                array2[i] = getReference(array[i]);
            }
            return (Class<?>[]) array2;
        }

        public static Class<?>[] getReference(final Object[] array) {
            final int n = (array == null) ? 0 : array.length;
            final Class[] array2 = new Class[n];
            for (int i = 0; i < n; ++i) {
                array2[i] = getReference(array[i].getClass());
            }
            return (Class<?>[]) array2;
        }

        public Class<?> getPrimitive() {
            return this.primitive;
        }

        public Class<?> getReference() {
            return this.reference;
        }
    }

    public enum PackageType {
        MINECRAFT_SERVER("net.minecraft.server." + getServerVersion()),
        CRAFTBUKKIT("org.bukkit.craftbukkit." + getServerVersion()),
        CRAFTBUKKIT_BLOCK(PackageType.CRAFTBUKKIT, "block"),
        CRAFTBUKKIT_CHUNKIO(PackageType.CRAFTBUKKIT, "chunkio"),
        CRAFTBUKKIT_COMMAND(PackageType.CRAFTBUKKIT, "command"),
        CRAFTBUKKIT_CONVERSATIONS(PackageType.CRAFTBUKKIT, "conversations"),
        CRAFTBUKKIT_ENCHANTMENS(PackageType.CRAFTBUKKIT, "enchantments"),
        CRAFTBUKKIT_ENTITY(PackageType.CRAFTBUKKIT, "entity"),
        CRAFTBUKKIT_EVENT(PackageType.CRAFTBUKKIT, "event"),
        CRAFTBUKKIT_GENERATOR(PackageType.CRAFTBUKKIT, "generator"),
        CRAFTBUKKIT_HELP(PackageType.CRAFTBUKKIT, "help"),
        CRAFTBUKKIT_INVENTORY(PackageType.CRAFTBUKKIT, "inventory"),
        CRAFTBUKKIT_MAP(PackageType.CRAFTBUKKIT, "map"),
        CRAFTBUKKIT_METADATA(PackageType.CRAFTBUKKIT, "metadata"),
        CRAFTBUKKIT_POTION(PackageType.CRAFTBUKKIT, "potion"),
        CRAFTBUKKIT_PROJECTILES(PackageType.CRAFTBUKKIT, "projectiles"),
        CRAFTBUKKIT_SCHEDULER(PackageType.CRAFTBUKKIT, "scheduler"),
        CRAFTBUKKIT_SCOREBOARD(PackageType.CRAFTBUKKIT, "scoreboard"),
        CRAFTBUKKIT_UPDATER(PackageType.CRAFTBUKKIT, "updater"),
        CRAFTBUKKIT_UTIL(PackageType.CRAFTBUKKIT, "util");

        private final String path;

        PackageType(final PackageType packageType, final String s2) {
            this(packageType + "." + s2);
        }

        PackageType(final String path) {
            this.path = path;
        }

        public static String getServerVersion() {
            return Bukkit.getServer().getClass().getPackage().getName().substring(23);
        }

        public Class<?> getClass(final String s) throws ClassNotFoundException {
            return Class.forName(this + "." + s);
        }

        public String getPath() {
            return this.path;
        }

        @Override
        public String toString() {
            return this.path;
        }
    }
}

