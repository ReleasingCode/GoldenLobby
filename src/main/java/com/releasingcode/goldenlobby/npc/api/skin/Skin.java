package com.releasingcode.goldenlobby.npc.api.skin;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;

public class Skin {

    private final String value, signature, from;

    public Skin(GameProfile profile, String from) {
        PropertyMap propertyMap = profile.getProperties();
        Property property = propertyMap.get("textures").iterator().next();
        this.value = property.getValue();
        this.signature = property.getSignature();
        this.from = from;
    }

    public Skin(String value, String signature, String from) {
        this.value = value;
        this.signature = signature;
        this.from = from;
    }

    public String getFrom() {
        return from;
    }

    public String getValue() {
        return this.value;
    }

    public String getSignature() {
        return this.signature;
    }
}
