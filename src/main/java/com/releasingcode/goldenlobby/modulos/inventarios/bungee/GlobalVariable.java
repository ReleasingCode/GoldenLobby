package com.releasingcode.goldenlobby.modulos.inventarios.bungee;

import java.util.HashMap;
import java.util.Map;

public class GlobalVariable {
    static final Map<String, Integer> extraVariable = new HashMap<>();

    public static int getExtraVariable(String name) {
        return extraVariable.getOrDefault(name.toLowerCase(), 0);
    }

    public static void setExtraVariable(String name, int value) {
        extraVariable.put(name.toLowerCase(), value);
    }
}
