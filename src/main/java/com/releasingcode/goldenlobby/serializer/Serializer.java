package com.releasingcode.goldenlobby.serializer;

public interface Serializer<E> {
    String serialize(E var1);

    E deserialize(String var1);
}

