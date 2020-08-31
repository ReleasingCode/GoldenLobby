package com.releasingcode.goldenlobby.modulos.npcserver.db;

public class NPCFetch {
    private final String dir;
    private final String name;
    private String base64;

    public NPCFetch(String name, String dir) {
        this.name = name;
        this.dir = dir;
    }

    public String getDir() {
        return dir;
    }


    public String getName() {
        return name;
    }

    public String getBase64() {
        return base64;
    }

    public void setBase64(String base64) {
        this.base64 = base64;
    }
}
