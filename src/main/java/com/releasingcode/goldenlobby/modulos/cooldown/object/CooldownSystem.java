package com.releasingcode.goldenlobby.modulos.cooldown.object;

import com.releasingcode.goldenlobby.managers.FutureTime;
import org.bukkit.Sound;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

public class CooldownSystem {

    private static final ConcurrentHashMap<String, CooldownSystem> cooldownsInMemory = new ConcurrentHashMap<>();
    private final String name;
    private long finishAt;
    private String cooldownString;
    private FutureTime futureTime;
    private boolean finished;
    private long startedAt;
    private CooldownStatus status;

    public CooldownSystem(String name) {
        this.name = name;
        finished = false;
        status = CooldownStatus.DISABLED;
    }

    public CooldownSystem(String name, long finishAt, long startedAt) {
        this.name = name;
        this.finishAt = finishAt;
        this.startedAt = startedAt;
        finished = false;
        status = CooldownStatus.DISABLED;
    }

    public static void addCooldown(String name, CooldownSystem system) {
        cooldownsInMemory.put(name.toLowerCase(), system);
    }

    public static CooldownSystem getCooldown(String name) {
        return cooldownsInMemory.getOrDefault(name.toLowerCase(), null);
    }

    public static Collection<CooldownSystem> getCooldowns() {
        return cooldownsInMemory.values();
    }

    public static void removeCooldown(String name) {
        cooldownsInMemory.remove(name.toLowerCase());
    }

    public static void addCooldowns(ArrayList<CooldownSystem> cooldownSystems) {
        cooldownsInMemory.clear();
        for (CooldownSystem sy :
                cooldownSystems) {
            cooldownsInMemory.put(sy.getName().toLowerCase(), sy);
        }
    }

    public CooldownStatus getStatus() {
        return status;
    }

    public void setStatus(CooldownStatus status) {
        this.status = status;
    }

    public void prepareFinishAt() {
        if (getFutureTime() == null) {
            return;
        }
        FutureTime time = getFutureTime();
        Instant instant = new Date().toInstant();
        if (time.getSecond() != null && time.getSecond() > 0) {
            Duration seconds = Duration.ofSeconds(time.getSecond());
            instant = instant.plus(seconds);
        }
        if (time.getMinute() != null && time.getMinute() > 0) {
            Duration minute = Duration.ofMinutes(time.getMinute());
            instant = instant.plus(minute);
        }
        if (time.getHour() != null && time.getHour() > 0) {
            Duration horas = Duration.ofHours(time.getHour());
            instant = instant.plus(horas);
        }
        if (time.getDay() != null && time.getDay() > 0) {
            Duration dias = Duration.ofHours(time.getDay() * 24);
            instant = instant.plus(dias);
        }
        if (time.getWeek() != null && time.getWeek() > 0) {
            Duration semanas = Duration.ofHours((7 * 24) * time.getWeek());
            instant = instant.plus(semanas);
        }
        if (time.getMonth() != null && time.getMonth() > 0) {
            Duration meses = Duration.ofHours(((30 * 24) * time.getMonth()));
            instant = instant.plus(meses);
        }
        setFinishAt(instant.toEpochMilli());
    }


    public long getFinishAt() {
        return finishAt;
    }

    public void setFinishAt(long finishAt) {
        this.finishAt = finishAt;
    }

    public FutureTime getFutureTime() {
        return futureTime;
    }

    public void setFutureTime(FutureTime futureTime) {
        this.futureTime = futureTime;
    }

    public String getCooldownString() {
        return cooldownString;
    }

    public String getRemaing() {
        if (status == CooldownStatus.DISABLED) {
            if (futureTime != null) {
                return futureTime.toString();
            }
            return "DISABLED";
        }
        if (status == CooldownStatus.FINISHED) {
            return "¡FINISHED!";
        }
        long now = System.currentTimeMillis();
        Date finish = new Date(finishAt);
        Date remaing = new Date(finish.getTime() - now);
        SimpleDateFormat format = new SimpleDateFormat("mm:ss");
        if (futureTime.getDay() != null && futureTime.getDay() > 0) {
            format = new SimpleDateFormat("dd:HH:mm:ss");
        } else if (futureTime.getMonth() != null && futureTime.getMonth() > 0) {
            format = new SimpleDateFormat("MM:dd:HH:mm:ss");
        } else if (futureTime.getHour() != null && futureTime.getHour() > 0) {
            format = new SimpleDateFormat("HH:mm:ss");
        } else if (futureTime.getMinute() != null && futureTime.getMinute() > 0) {
            format = new SimpleDateFormat("mm:ss");
        }
        if (now >= finishAt) {
            return "¡FINISHED!";
        }
        return format.format(remaing);
    }

    public void setCooldownString(String cooldownString) {
        this.cooldownString = cooldownString;
        FutureTime FutTime = FutureTime.parseByString(cooldownString);
        setFutureTime(FutTime);
    }

    public long getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(long startedAt) {
        this.startedAt = startedAt;
    }

    public String getName() {
        return name;
    }

    public void prepareStarted() {
        this.startedAt = new Date().getTime();
        setFinished(false);
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public enum CooldownStatus {
        DISABLED, ENABLED, FINISHED
    }

    public static class CooldownSettings {
        private String sound;
        private double volume;
        private double pitch;
        private Sound soundBukkit;

        public CooldownSettings() {
        }

        public Sound getSoundBukkit() {
            return soundBukkit;
        }

        public String getSound() {
            return sound;
        }

        public void setSound(String sound) {
            this.sound = sound;
            if (sound != null && !sound.trim().isEmpty()) {
                try {
                    soundBukkit = Sound.valueOf(sound.toUpperCase());
                } catch (Exception e) {

                }
            }
        }

        public float getVolume() {
            return (float) volume;
        }

        public void setVolume(double volume) {
            this.volume = volume;
        }

        public float getPitch() {
            return (float) pitch;
        }

        public void setPitch(double pitch) {
            this.pitch = pitch;
        }
    }
}
