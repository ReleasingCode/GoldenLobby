package com.releasingcode.goldenlobby.extendido.classaccess.reflection;

interface Access {

    Object newInstance();

    Object newInstance(int index, Object... args);

    Object invoke(Object instance, int methodIndex, Object... args);

    void set(Object instance, int index, Object value);

    void setBoolean(Object instance, int index, boolean value);

    void setByte(Object instance, int index, byte value);

    void setShort(Object instance, int index, short value);

    void setInt(Object instance, int index, int value);

    void setLong(Object instance, int index, long value);

    void setDouble(Object instance, int index, double value);

    void setFloat(Object instance, int index, float value);

    void setChar(Object instance, int index, char value);

    Object get(Object instance, int index);

    char getChar(Object instance, int index);

    boolean getBoolean(Object instance, int index);

    byte getByte(Object instance, int index);

    short getShort(Object instance, int index);

    int getInt(Object instance, int index);

    long getLong(Object instance, int index);

    double getDouble(Object instance, int index);

    float getFloat(Object instance, int index);

}