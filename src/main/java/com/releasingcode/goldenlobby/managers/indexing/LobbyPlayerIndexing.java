package com.releasingcode.goldenlobby.managers.indexing;

import java.util.concurrent.ConcurrentHashMap;

public class LobbyPlayerIndexing {
    private final ConcurrentHashMap<Integer, Integer> byIndex = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> extra = new ConcurrentHashMap<>();

    //* Esto sirve para incrementar el indice por index y por jugador
    public int getAndIncrementIndexAtInt(int index, int max) {
        if (byIndex.containsKey(index)) {
            if (byIndex.get(index) >= (max - 1)) {
                return byIndex.compute(index, (key, value) -> 0);
            }
            return byIndex.compute(index, (key, value) -> value != null ? value + 1 : 0);
        }
        return byIndex.computeIfAbsent(index, key -> 0);
    }

    public int getIndexAtExtra(String keyExtra, int startIndex) {
        if (extra.containsKey(keyExtra.toLowerCase())) {
            return extra.getOrDefault(keyExtra.toLowerCase(), startIndex);
        }
        extra.put(keyExtra.toLowerCase(), startIndex);
        return startIndex;
    }

    public void remove(String... keyExtras) {
        for (String keyExtra : keyExtras)
            extra.remove(keyExtra.toLowerCase());
    }

    public void pushIndexAtExtra(String keyExtra, int increment) {
        if (extra.containsKey(keyExtra.toLowerCase())) {
            extra.compute(keyExtra.toLowerCase(), (key, value) -> value != null ? value + increment : increment);
        }
    }

    public int getAndIncrementIndexAtExtra(String keyExtra, int max) {
        if (extra.containsKey(keyExtra)) {
            if (extra.get(keyExtra) >= (max - 1)) {
                extra.put(keyExtra, 0);
                return 0;
            }
            int next = extra.get(keyExtra) + 1;
            extra.put(keyExtra, next);
            return next;
        }

        extra.put(keyExtra, 0);
        return 0;
    }


    public int getAndIncrementIndexAtExtraCondition(String keyExtra, int max, boolean conditionNext) {
        if (extra.containsKey(keyExtra)) {
            if (conditionNext) {
                if (extra.get(keyExtra) >= (max - 1)) {
                    return extra.compute(keyExtra, (key, value) -> 0);
                }
                return extra.compute(keyExtra, (key, value) -> value != null ? value + 1 : 0);
            }
            return extra.get(keyExtra);
        }
        return extra.computeIfAbsent(keyExtra, key -> 0);
    }

    public int getAndIncrementIndexAtExtra(String keyExtra, long max) {
        if (extra.containsKey(keyExtra)) {
            if (extra.get(keyExtra) >= (max - 1)) {
                return extra.compute(keyExtra, (key, value) -> 0);
            }
            return extra.compute(keyExtra, (key, value) -> value != null ? value + 1 : 0);
        }
        return extra.computeIfAbsent(keyExtra, key -> 0);
    }
}
