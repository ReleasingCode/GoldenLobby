package com.releasingcode.goldenlobby.database.pubsub;

public enum SubChannel {
    SYNC_NPC,
    SYNC_INVENTORY,
    SYNC_SCOREBOARD;


    public String lower() {
        return name().toLowerCase();
    }

    public byte[] tobyte() {
        return lower().getBytes();
    }

    public enum SubOperation {
        GET_FROM_LOCAL,
        GET_FROM_DB,
        PURGE_AND_GET_FROM_DB;

        public static SubOperation from(String lower) {
            for (SubOperation op : values()) {
                if (op.lower().equals(lower)) {
                    return op;
                }
            }
            return null;
        }

        public String lower() {
            return name().toLowerCase();
        }

        public byte[] tobyte() {
            return lower().getBytes();
        }
    }
}
