package com.releasingcode.goldenlobby.modulos.regions.mobs;

import com.releasingcode.goldenlobby.LobbyMC;
import com.releasingcode.goldenlobby.modulos.regions.RegionPlugin;
import com.releasingcode.goldenlobby.modulos.regions.cuboid.Cuboid;
import com.releasingcode.goldenlobby.modulos.regions.cuboid.StageOfCreation;
import org.bukkit.scheduler.BukkitRunnable;
import com.releasingcode.goldenlobby.modulos.regions.mobs.types.ConejoRabioso;
import com.releasingcode.goldenlobby.modulos.regions.mobs.types.CustomMob;
import com.releasingcode.goldenlobby.modulos.regions.mobs.types.ElBicho;
import com.releasingcode.goldenlobby.modulos.regions.mobs.types.PequenoJig;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class MobsManager {
    private final List<Class<? extends CustomMob>> mobs = Arrays.asList(ConejoRabioso.class,
                                                                        PequenoJig.class,
                                                                        ElBicho.class);
    private final RegionPlugin plugin;
    private final Cuboid cuboid;
    private final TimeUnit[] randomUnitsAvailable = new TimeUnit[]{TimeUnit.HOURS, TimeUnit.MINUTES};

    public MobsManager(RegionPlugin plugin) {
        this.plugin = plugin;
        cuboid = plugin.getCuboidManager().getRegions().get(StageOfCreation.Regions.PVP);
        startTicking();
    }

    public void startTicking() {
        int interval = randomInterval();

        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    mobs.get(ThreadLocalRandom.current().nextInt(mobs.size()))
                            .getDeclaredConstructor(RegionPlugin.class, Cuboid.class)
                            .newInstance(plugin, cuboid);
                } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskLater(LobbyMC.getInstance(), interval);

    }

    private int randomInterval() {
        int time = ThreadLocalRandom.current().nextInt(1, 9);
        TimeUnit randomUnit = randomUnitsAvailable[ThreadLocalRandom.current().nextInt(randomUnitsAvailable.length)];

        return new RandomTime(time, randomUnit).getInterval();

    }

    private static class RandomTime {
        private final int interval;
        private final TimeUnit timeUnit;

        private RandomTime(int interval, TimeUnit timeUnit) {
            this.interval = interval;
            this.timeUnit = timeUnit;
        }

        private int getInterval() {
            if (timeUnit == TimeUnit.HOURS) {
                return (interval * 3600) * 20;
            } else {
                return (interval * 60) * 20;
            }
        }

    }

}
