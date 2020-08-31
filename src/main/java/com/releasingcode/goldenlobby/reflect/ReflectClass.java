package com.releasingcode.goldenlobby.reflect;

import org.bukkit.Bukkit;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.function.Function;

@SuppressWarnings("unchecked")
public class ReflectClass {
    private final Class<?> handle;

    public ReflectClass(Class<?> handle) {
        this.handle = handle;
    }

    public ReflectClass(String name) throws ReflectException {
        try {
            this.handle = Class.forName(name);
        } catch (ClassNotFoundException ex) {
            throw new ReflectException(ex);
        }
    }

    public ReflectClass(Package pkg, String name) throws ReflectException {
        this(pkg.prefix + name);
    }

    public Class<?> getHandle() {
        return this.handle;
    }

    public Constructor getConstructor(Class<?>... params) throws ReflectException {
        return new Constructor(this.handle, params);
    }

    public Field getField(String name) throws ReflectException {
        return new Field(this.handle, name);
    }

    public Method getMethod(String name, Class<?>... params) throws ReflectException {
        return new Method(this.handle, name, params);
    }

    public Field[] getFields() {
        return Arrays.stream(this.handle.getFields()).map(Field::new).toArray(Field[]::new);
    }

    public Field[] getDeclaredFields() {
        return Arrays.stream(this.handle.getDeclaredFields()).map(Field::new).toArray(Field[]::new);
    }

    public Method[] getMethods() {
        return Arrays.stream(this.handle.getMethods()).map(Method::new).toArray(Method[]::new);
    }

    public Method[] getDeclaredMethods() {
        return Arrays.stream(this.handle.getDeclaredMethods()).map(Method::new).toArray(Method[]::new);
    }

    public enum Package {
        CBK(Bukkit.getServer().getClass().getPackage().getName() + '.'),
        NMS("net.minecraft.server" + Package.CBK.prefix.substring(Package.CBK.prefix.lastIndexOf(46, Package.CBK.prefix.length() - 2)));

        private final String prefix;

        Package(String path) {
            this.prefix = path;
        }

        public ReflectClass getClass(String name) {
            return new ReflectClass(this, name);
        }
    }

    public static class ReflectException
            extends RuntimeException {
        ReflectException(Throwable src) {
            super(src);
        }
    }

    public static class Method {
        private final java.lang.reflect.Method handle;

        Method(Class<?> owner, String name, Class<?>... params) throws ReflectException {
            try {
                this.handle = owner.getDeclaredMethod(name, params);
            } catch (NoSuchMethodException ex) {
                throw new ReflectException(ex);
            }
        }

        Method(java.lang.reflect.Method handle) {
            this.handle = handle;
        }

        public String getName() {
            return this.handle.getName();
        }

        public Method makeAccessible() {
            this.handle.setAccessible(true);
            return this;
        }

        public Class<?> getType() {
            return this.handle.getReturnType();
        }

        public ReflectClass getReflectType() {
            return new ReflectClass(this.handle.getReturnType());
        }

        public Class<?> getOwner() {
            return this.handle.getDeclaringClass();
        }

        public ReflectClass getReflectOwner() {
            return new ReflectClass(this.handle.getDeclaringClass());
        }

        public boolean canInvoke(Object instance) {
            return this.handle.getDeclaringClass().isInstance(instance);
        }

        public Object invoke(Object instance, Object... params) throws ReflectException {
            try {
                return this.handle.invoke(instance, params);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                throw new ReflectException(ex);
            }
        }

        public boolean invokeAsBoolean(Object instance, Object... params) throws ReflectException {
            return (Boolean) this.invoke(instance, params);
        }

        public char invokeAsChar(Object instance, Object... params) throws ReflectException {
            return (Character) this.invoke(instance, params);
        }

        public byte invokeAsByte(Object instance, Object... params) throws ReflectException {
            return (Byte) this.invoke(instance, params);
        }

        public short invokeAsShort(Object instance, Object... params) throws ReflectException {
            return (Short) this.invoke(instance, params);
        }

        public int invokeAsInt(Object instance, Object... params) throws ReflectException {
            return (Integer) this.invoke(instance, params);
        }

        public long invokeAsLong(Object instance, Object... params) throws ReflectException {
            return (Long) this.invoke(instance, params);
        }

        public float invokeAsFloat(Object instance, Object... params) throws ReflectException {
            return (Float) this.invoke(instance, params);
        }

        public double invokeAsDouble(Object instance, Object... params) throws ReflectException {
            return (Double) this.invoke(instance, params);
        }

        public <T> T invokeGeneric(Object instance, Object... params) throws ReflectException {
            return (T) this.invoke(instance, params);
        }

        public Object invokeStatic(Object... params) throws ReflectException {
            return this.invoke(null, params);
        }

        public <T> T invokeGenericStatic(Object... params) throws ReflectException {
            return (T) this.invoke(null, params);
        }
    }

    public static class Field {
        private static final java.lang.reflect.Field modifierField = new Field(java.lang.reflect.Field.class, "modifiers").makeAccessible().handle;
        private final java.lang.reflect.Field handle;

        Field(Class<?> owner, String name) throws ReflectException {
            try {
                this.handle = owner.getDeclaredField(name);
            } catch (NoSuchFieldException ex) {
                throw new ReflectException(ex);
            }
        }

        Field(java.lang.reflect.Field handle) {
            this.handle = handle;
        }

        public String getName() {
            return this.handle.getName();
        }

        public Field makeAccessible() {
            this.handle.setAccessible(true);
            return this;
        }

        public Field stripModifiers(int mod) throws ReflectException {
            try {
                modifierField.set(this.handle, this.handle.getModifiers() & ~mod);
                return this;
            } catch (IllegalAccessException ex) {
                throw new ReflectException(ex);
            }
        }

        public Class<?> getType() {
            return this.handle.getType();
        }

        public ReflectClass getReflectType() {
            return new ReflectClass(this.handle.getType());
        }

        public Class<?> getOwner() {
            return this.handle.getDeclaringClass();
        }

        public ReflectClass getReflectOwner() {
            return new ReflectClass(this.handle.getDeclaringClass());
        }

        public boolean isOwner(Object instance) {
            return this.handle.getDeclaringClass().isInstance(instance);
        }

        public Object get(Object instance) throws ReflectException {
            try {
                return this.handle.get(instance);
            } catch (IllegalAccessException | IllegalArgumentException ex) {
                throw new ReflectException(ex);
            }
        }

        public <T> T getGeneric(Object instance) throws ReflectException {
            return (T) this.get(instance);
        }

        public boolean getBoolean(Object instance) throws ReflectException {
            try {
                return this.handle.getBoolean(instance);
            } catch (IllegalAccessException | IllegalArgumentException ex) {
                throw new ReflectException(ex);
            }
        }

        public char getChar(Object instance) throws ReflectException {
            try {
                return this.handle.getChar(instance);
            } catch (IllegalAccessException | IllegalArgumentException ex) {
                throw new ReflectException(ex);
            }
        }

        public byte getByte(Object instance) throws ReflectException {
            try {
                return this.handle.getByte(instance);
            } catch (IllegalAccessException | IllegalArgumentException ex) {
                throw new ReflectException(ex);
            }
        }

        public short getShort(Object instance) throws ReflectException {
            try {
                return this.handle.getShort(instance);
            } catch (IllegalAccessException | IllegalArgumentException ex) {
                throw new ReflectException(ex);
            }
        }

        public int getInt(Object instance) throws ReflectException {
            try {
                return this.handle.getInt(instance);
            } catch (IllegalAccessException | IllegalArgumentException ex) {
                throw new ReflectException(ex);
            }
        }

        public long getLong(Object instance) throws ReflectException {
            try {
                return this.handle.getLong(instance);
            } catch (IllegalAccessException | IllegalArgumentException ex) {
                throw new ReflectException(ex);
            }
        }

        public float getFloat(Object instance) throws ReflectException {
            try {
                return this.handle.getFloat(instance);
            } catch (IllegalAccessException | IllegalArgumentException ex) {
                throw new ReflectException(ex);
            }
        }

        public double getDouble(Object instance) throws ReflectException {
            try {
                return this.handle.getDouble(instance);
            } catch (IllegalAccessException | IllegalArgumentException ex) {
                throw new ReflectException(ex);
            }
        }

        public void set(Object instance, Object value) throws ReflectException {
            try {
                this.handle.set(instance, value);
            } catch (IllegalAccessException | IllegalArgumentException ex) {
                throw new ReflectException(ex);
            }
        }

        public void set(Object instance, boolean value) throws ReflectException {
            try {
                this.handle.setBoolean(instance, value);
            } catch (IllegalAccessException | IllegalArgumentException ex) {
                throw new ReflectException(ex);
            }
        }

        public void set(Object instance, char value) throws ReflectException {
            try {
                this.handle.setChar(instance, value);
            } catch (IllegalAccessException | IllegalArgumentException ex) {
                throw new ReflectException(ex);
            }
        }

        public void set(Object instance, byte value) throws ReflectException {
            try {
                this.handle.setByte(instance, value);
            } catch (IllegalAccessException | IllegalArgumentException ex) {
                throw new ReflectException(ex);
            }
        }

        public void set(Object instance, short value) throws ReflectException {
            try {
                this.handle.setShort(instance, value);
            } catch (IllegalAccessException | IllegalArgumentException ex) {
                throw new ReflectException(ex);
            }
        }

        public void set(Object instance, int value) throws ReflectException {
            try {
                this.handle.setInt(instance, value);
            } catch (IllegalAccessException | IllegalArgumentException ex) {
                throw new ReflectException(ex);
            }
        }

        public void set(Object instance, long value) throws ReflectException {
            try {
                this.handle.setLong(instance, value);
            } catch (IllegalAccessException | IllegalArgumentException ex) {
                throw new ReflectException(ex);
            }
        }

        public void set(Object instance, float value) throws ReflectException {
            try {
                this.handle.setFloat(instance, value);
            } catch (IllegalAccessException | IllegalArgumentException ex) {
                throw new ReflectException(ex);
            }
        }

        public void set(Object instance, double value) throws ReflectException {
            try {
                this.handle.setDouble(instance, value);
            } catch (IllegalAccessException | IllegalArgumentException ex) {
                throw new ReflectException(ex);
            }
        }

        public void compute(Object instance, Function<Object, ?> function) throws ReflectException {
            this.set(instance, function.apply(this.get(instance)));
        }

        public Object getStatic() throws ReflectException {
            return this.get(null);
        }

        public void setStatic(Object value) throws ReflectException {
            this.set(null, value);
        }

        public void setStatic(boolean value) throws ReflectException {
            this.set(null, value);
        }

        public void setStatic(char value) throws ReflectException {
            this.set(null, value);
        }

        public void setStatic(byte value) throws ReflectException {
            this.set(null, value);
        }

        public void setStatic(short value) throws ReflectException {
            this.set(null, value);
        }

        public void setStatic(int value) throws ReflectException {
            this.set(null, value);
        }

        public void setStatic(long value) throws ReflectException {
            this.set(null, value);
        }

        public void setStatic(float value) throws ReflectException {
            this.set(null, value);
        }

        public void setStatic(double value) throws ReflectException {
            this.set(null, value);
        }

        public <T> T getGenericStatic() throws ReflectException {
            return (T) this.get(null);
        }

        public boolean getStaticBoolean() throws ReflectException {
            return this.getBoolean(null);
        }

        public char getStaticChar() throws ReflectException {
            return this.getChar(null);
        }

        public byte getStaticByte() throws ReflectException {
            return this.getByte(null);
        }

        public short getStaticShort() throws ReflectException {
            return this.getShort(null);
        }

        public int getStaticInt() throws ReflectException {
            return this.getInt(null);
        }

        public long getStaticLong() throws ReflectException {
            return this.getLong(null);
        }

        public float getStaticFloat() throws ReflectException {
            return this.getFloat(null);
        }

        public double getStaticDouble() throws ReflectException {
            return this.getDouble(null);
        }

        public void computeStatic(Function<Object, ?> function) throws ReflectException {
            this.set(null, function.apply(this.get(null)));
        }
    }

    public static class Constructor {
        private final java.lang.reflect.Constructor<?> handle;

        Constructor(Class<?> owner, Class<?>... params) throws ReflectException {
            try {
                this.handle = owner.getDeclaredConstructor(params);
            } catch (NoSuchMethodException ex) {
                throw new ReflectException(ex);
            }
        }

        public Constructor makeAccessible() {
            this.handle.setAccessible(true);
            return this;
        }

        public Class<?> getOwner() {
            return this.handle.getDeclaringClass();
        }

        public ReflectClass getReflectOwner() {
            return new ReflectClass(this.handle.getDeclaringClass());
        }

        public Object newInstance(Object... params) throws ReflectException {
            try {
                return this.handle.newInstance(params);
            } catch (Exception ex) {
                throw new ReflectException(ex);
            }
        }
    }

}

