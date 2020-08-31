package com.releasingcode.goldenlobby.modulos.cooldown;

import com.releasingcode.goldenlobby.Utils;
import com.releasingcode.goldenlobby.configuracion.CustomConfiguration;
import com.releasingcode.goldenlobby.loader.LobbyComponente;
import com.releasingcode.goldenlobby.npc.api.NPC;
import com.releasingcode.goldenlobby.npc.internal.NPCManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import com.releasingcode.goldenlobby.extendido.nms.ParticleEffect;
import com.releasingcode.goldenlobby.modulos.cooldown.command.CooldownCommand;
import com.releasingcode.goldenlobby.modulos.cooldown.db.CooldownDB;
import com.releasingcode.goldenlobby.modulos.cooldown.object.CooldownSystem;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CooldownPlugin extends LobbyComponente {

    CustomConfiguration cooldownSettings;
    ScheduledExecutorService executorService;
    private CooldownDB cooldownDB;
    private CooldownSystem.CooldownSettings cooldownSettingsObject;
    Runnable scheduler = () -> {
        for (CooldownSystem cooldown : CooldownSystem.getCooldowns()) {
            if (cooldown.getStatus().equals(CooldownSystem.CooldownStatus.FINISHED)) {
                continue;
            }
            long currentNow = System.currentTimeMillis();
            if (currentNow > cooldown.getFinishAt()) {//superó el tiempo estimado
                cooldown.setFinished(true);
                cooldown.setStatus(CooldownSystem.CooldownStatus.FINISHED);
                Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), () -> {
                    cooldownDB.createOrUpdate(cooldown, () -> {

                    });
                    if (cooldownSettingsObject.getSoundBukkit() != null) {
                        Bukkit.getScheduler().runTask(getPlugin(), () -> {
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                p.playSound(p.getLocation(), cooldownSettingsObject.getSoundBukkit(),
                                        cooldownSettingsObject.getVolume(), cooldownSettingsObject.getPitch());
                                for (NPC npc : NPCManager.getAllNPCs()) {
                                    if (npc.getCooldownValidator() != null) {
                                        CooldownSystem system = CooldownSystem.getCooldown(npc.getCooldownValidator());
                                        if (system != null) {
                                            if (system.getStatus().equals(CooldownSystem.CooldownStatus.FINISHED)) {
                                                ParticleEffect.EXPLOSION_HUGE
                                                        .display(0.3f, 1.0f, 0.3f, 0.0f, 40, npc.getLocation(),
                                                                p);
                                            }
                                            for (Entity eme : npc.getLocation().getWorld().getNearbyEntities(npc.getLocation(), 6.0, 6.0, 6.0)) {
                                                if (!(eme instanceof Player)) continue;
                                                Player ppp = ((Player) eme).getPlayer();
                                                Vector spV = ppp.getLocation().toVector();
                                                Vector plV = npc.getLocation().toVector();
                                                Vector v = spV.clone().subtract(plV).multiply(2.5 / spV.distance(plV)).setY(1.5);
                                                ppp.getPlayer().setVelocity(v);
                                            }
                                        }
                                    }
                                }
                            }

                        });
                    }
                });
            }
        }
    };

    @Override
    protected void onEnable() {
        executorService = Executors.newScheduledThreadPool(2);
        cooldownSettings = new CustomConfiguration("cooldownSettings", getPlugin());
        cooldownSettingsObject = new CooldownSystem.CooldownSettings();
        cooldownSettingsObject.setSound(cooldownSettings.getConfig().getString("SoundFinish.name"));
        cooldownSettingsObject.setPitch(cooldownSettings.getConfig().getDouble("SoundFinish.volume"));
        cooldownSettingsObject.setVolume(cooldownSettings.getConfig().getDouble("SoundFinish.pitch"));
        cooldownDB = new CooldownDB(this);
        new CooldownCommand(this, "mccooldown").register();
        Utils.log("CooldownPlugin", "&aSe ha iniciado correctamente!");
        Utils.log("CooldownPlugin", "&aCargando cooldowns");
        executorService.scheduleWithFixedDelay(scheduler, 0, 1, TimeUnit.SECONDS);
    }

    public CooldownDB getCooldownDB() {
        return cooldownDB;
    }

    public void reloadSettings() {
        cooldownSettings = new CustomConfiguration("cooldownSettings", getPlugin());
        cooldownSettingsObject = new CooldownSystem.CooldownSettings();
        cooldownSettingsObject.setSound(cooldownSettings.getConfig().getString("SoundFinish.name"));
        cooldownSettingsObject.setPitch(cooldownSettings.getConfig().getDouble("SoundFinish.volume"));
        cooldownSettingsObject.setVolume(cooldownSettings.getConfig().getDouble("SoundFinish.pitch"));
    }

    public void loadCooldown() {
        cooldownDB.fetchCooldown(callback -> {
            Utils.log("CooldownPlugin", "se ha cargado " + callback.size() + " cooldown(s)");
            if (!callback.isEmpty()) {
                CooldownSystem.addCooldowns(callback);
            }
        });
    }

    @Override
    protected void onDisable() {
        Utils.log("CooldownPlugn", "&cSe ha desabilitado correctamente!");
    }
}
