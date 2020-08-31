package com.releasingcode.goldenlobby.modulos.inventarios.db;

public class InventoryFetch {

    private final String name;
    private String base64;

    public InventoryFetch(String name) {
        this.name = name;
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

