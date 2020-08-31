package com.releasingcode.goldenlobby.modulos.regions.mobs.types;

import com.releasingcode.goldenlobby.LobbyMC;
import com.releasingcode.goldenlobby.modulos.regions.RegionPlugin;
import com.releasingcode.goldenlobby.modulos.regions.cuboid.Cuboid;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.List;

public class ElBicho extends CustomMob implements Listener {

    private boolean hasIncreasedDamage = false;
    private double damage;

    public ElBicho(RegionPlugin plugin, Cuboid region) {
        super(plugin, region, EntityType.ENDERMITE, "&7El bicho", 250, 7, 375, "&4El bicho ha sido derrotado por {killer}.");
        damage = getDamage();
        LobbyMC.getInstance().getServer().getPluginManager().registerEvents(this, LobbyMC.getInstance());
    }

    @Override void initializeEntity() {
        List goalB = (List) getPrivateField("b", PathfinderGoalSelector.class, goalSelector); goalB.clear();
        List goalC = (List) getPrivateField("c", PathfinderGoalSelector.class, goalSelector); goalC.clear();
        List targetB = (List) getPrivateField("b", PathfinderGoalSelector.class, targetSelector); targetB.clear();
        List targetC = (List) getPrivateField("c", PathfinderGoalSelector.class, targetSelector); targetC.clear();

        this.goalSelector.a(0, new PathfinderGoalFloat(this));
        this.goalSelector.a(2, new PathfinderGoalMeleeAttack(this, EntityHuman.class, 1.0D, false));
        this.goalSelector.a(5, new PathfinderGoalMoveTowardsRestriction(this, 1.0D));
        this.goalSelector.a(6, new PathfinderGoalMoveThroughVillage(this, 1.0D, false));
        this.goalSelector.a(8, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
        this.goalSelector.a(8, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, true));
        this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, false));
    }

    @Override
    @EventHandler
    void handleAttack(EntityDamageByEntityEvent e) {
        if (!e.getEntity().equals(super.getEntity())) return;

        if (getEntity().getHealth() < 30 && !hasIncreasedDamage) {
            damage = getDamage() * 2;
            hasIncreasedDamage = true;
        }

        e.setDamage(damage);

    }

    @Override
    @EventHandler
    void handleDeath(EntityDeathEvent e) {
        if (!e.getEntity().equals(super.getEntity())) return;

        if (e.getEntity().getKiller() != null) {
            giveReward(e.getEntity().getKiller());
            Bukkit.broadcastMessage(getMessageWhenDefeated().replace("{killer}", e.getEntity().getKiller().getName()));
        } else {
            Bukkit.broadcastMessage(getMessageWhenDefeated().replace("{killer}", ""));
        }

        getPlugin().getMobsManager().startTicking();
        e.getDrops().clear();
        HandlerList.unregisterAll(this);
        Bukkit.broadcastMessage(getMessageWhenDefeated());
    }

}
