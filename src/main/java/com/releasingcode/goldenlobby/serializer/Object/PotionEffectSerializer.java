package com.releasingcode.goldenlobby.serializer.Object;

import com.releasingcode.goldenlobby.serializer.Serializer;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PotionEffectSerializer
        implements Serializer<PotionEffect> {
    @Override
    public String serialize(PotionEffect potionEffect) {
        return "" + potionEffect.getAmplifier() + "-" + potionEffect.getDuration() + "-" + potionEffect.getType().getName();
    }

    @Override
    public PotionEffect deserialize(String string) {
        String[] arrstring = string.split("-");
        try {
            return new PotionEffect(PotionEffectType.getByName(arrstring[2]), Integer.parseInt(arrstring[1]), Integer.parseInt(arrstring[0]));
        } catch (Exception exception) {
            return new PotionEffect(PotionEffectType.SPEED, 30, 0);
        }
    }
}

