package com.releasingcode.goldenlobby.extendido.nms;

import org.bukkit.Particle;

public class ParticleBuilder {
    public static Particle getParticle(ParticleEffect particleEffect) {
        switch (particleEffect) {
            case BLOCK_CRACK: {
                return Particle.BLOCK_CRACK;
            }
            case BLOCK_DUST: {
                return Particle.BLOCK_DUST;
            }
            case CLOUD: {
                return Particle.CLOUD;
            }
            case CRIT: {
                return Particle.CRIT;
            }
            case CRIT_MAGIC: {
                return Particle.CRIT_MAGIC;
            }
            case DRIP_LAVA:
            case LAVA: {
                return Particle.LAVA;
            }
            case DRIP_WATER: {
                return Particle.DRIP_LAVA;
            }
            case ENCHANTMENT_TABLE: {
                return Particle.ENCHANTMENT_TABLE;
            }
            case EXPLOSION_HUGE: {
                return Particle.EXPLOSION_HUGE;
            }
            case EXPLOSION_LARGE: {
                return Particle.EXPLOSION_LARGE;
            }
            case EXPLOSION_NORMAL: {
                return Particle.EXPLOSION_NORMAL;
            }
            case FIREWORKS_SPARK: {
                return Particle.FIREWORKS_SPARK;
            }
            case FLAME: {
                return Particle.FLAME;
            }
            case FOOTSTEP: {
                return Particle.FOOTSTEP;
            }
            case HEART: {
                return Particle.HEART;
            }
            case ITEM_CRACK: {
                return Particle.ITEM_CRACK;
            }
            case ITEM_TAKE: {
                return Particle.ITEM_TAKE;
            }
            case MOB_APPEARANCE: {
                break;
            }
            case NOTE: {
                return Particle.NOTE;
            }
            case PORTAL: {
                return Particle.PORTAL;
            }
            case REDSTONE: {
                return Particle.REDSTONE;
            }
            case SLIME: {
                return Particle.SLIME;
            }
            case SMOKE_LARGE: {
                return Particle.SMOKE_LARGE;
            }
            case SMOKE_NORMAL: {
                return Particle.SMOKE_NORMAL;
            }
            case SNOWBALL: {
                return Particle.SNOWBALL;
            }
            case SNOW_SHOVEL: {
                return Particle.SNOW_SHOVEL;
            }
            case SPELL: {
                return Particle.SPELL;
            }
            case SPELL_INSTANT: {
                return Particle.SPELL_INSTANT;
            }
            case SPELL_MOB: {
                return Particle.SPELL_MOB;
            }
            case SPELL_MOB_AMBIENT: {
                return Particle.SPELL_MOB_AMBIENT;
            }
            case SPELL_WITCH: {
                return Particle.SPELL_WITCH;
            }
            case SUSPENDED: {
                return Particle.SUSPENDED;
            }
            case SUSPENDED_DEPTH: {
                return Particle.SUSPENDED_DEPTH;
            }
            case TOWN_AURA: {
                return Particle.TOWN_AURA;
            }
            case VILLAGER_ANGRY: {
                return Particle.VILLAGER_ANGRY;
            }
            case VILLAGER_HAPPY: {
                return Particle.VILLAGER_HAPPY;
            }
            case WATER_BUBBLE: {
                return Particle.WATER_BUBBLE;
            }
            case WATER_DROP: {
                return Particle.WATER_DROP;
            }
            case WATER_SPLASH: {
                return Particle.WATER_SPLASH;
            }
            case WATER_WAKE: {
                return Particle.WATER_WAKE;
            }
            case BARRIER:
            default: {
                return Particle.BARRIER;
            }
        }
        return Particle.BARRIER;
    }

}

