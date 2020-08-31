package com.releasingcode.goldenlobby.modulos.regions.mobs.types;

import com.releasingcode.goldenlobby.Utils;
import com.releasingcode.goldenlobby.modulos.regions.RegionPlugin;
import com.releasingcode.goldenlobby.modulos.regions.cuboid.Cuboid;
import es.minecub.core.minecubos.MinecubosAPI;
import net.minecraft.server.v1_8_R3.EntityMonster;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.lang.reflect.Field;

public abstract class CustomMob extends EntityMonster {

    private final RegionPlugin plugin;
    private final double damage;
    private final int reward; // Cantidad de minecubos
    private final String messageWhenDefeated;

    private final LivingEntity entity;

    protected CustomMob(RegionPlugin plugin, Cuboid region, EntityType entityType, String name, double health, double damage, int reward, String messageWhenDefeated) {
        super(((CraftWorld) region.getCenter().getWorld()).getHandle());
        this.plugin = plugin;
        this.damage = damage;
        this.reward = reward;
        this.messageWhenDefeated = Utils.chatColor(messageWhenDefeated);

        this.entity = (LivingEntity) region.getCenter().getWorld().spawnEntity(region.getCenter(), entityType);

        entity.setCustomName(Utils.chatColor(name));
        entity.setMaxHealth(health);
        entity.setHealth(health);

        initializeEntity();
    }

    abstract void initializeEntity();

    abstract void handleAttack(EntityDamageByEntityEvent e);

    abstract void handleDeath(EntityDeathEvent e);

    protected void giveReward(Player player) {
        MinecubosAPI.giveMinecubos(player, reward, false);
    }

    public static Object getPrivateField(String fieldName, Class clazz, Object object) {
        Field field;
        Object o = null;
        try {
            field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            o = field.get(object);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return o;
    }

    public double getDamage() {
        return damage;
    }

    public int getReward() {
        return reward;
    }

    public String getMessageWhenDefeated() {
        return messageWhenDefeated;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public RegionPlugin getPlugin() {
        return plugin;
    }

}
