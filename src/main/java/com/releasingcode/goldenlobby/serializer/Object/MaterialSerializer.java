package com.releasingcode.goldenlobby.serializer.Object;

import com.releasingcode.goldenlobby.Utils;
import com.releasingcode.goldenlobby.serializer.Serializer;
import org.bukkit.Material;

public class MaterialSerializer
        implements Serializer<Material> {
    @Override
    public String serialize(Material material) {
        return String.valueOf(material.getId());
    }

    @Override
    public Material deserialize(String string) {
        return Utils.tryParseInt(string) ? Material.getMaterial(Integer.parseInt(string)) : Material.getMaterial(string);
    }
}

