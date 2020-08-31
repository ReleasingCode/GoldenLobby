package com.releasingcode.goldenlobby.serializer.Object;

import com.releasingcode.goldenlobby.Utils;
import com.releasingcode.goldenlobby.serializer.Serializer;
import org.bukkit.Color;

public class ColorSerializer
        implements Serializer<Color> {
    @Override
    public String serialize(Color color) {
        return String.valueOf(color.asRGB());
    }

    @Override
    public Color deserialize(String string) {
        if (!Utils.tryParseInt(string)) {
            return Color.WHITE;
        }
        return Color.fromRGB(Integer.parseInt(string));
    }
}

