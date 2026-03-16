package xeliox.simplegate.utils;

import org.bukkit.*;

public enum ParticleType {

    ENCHANTMENT {
        @Override
        public Particle get() {
            return retrieve("ENCHANTMENT_TABLE", "ENCHANT");
        }
    },
    EXPLOSION {
        @Override
        public Particle get() {
            return retrieve("EXPLOSION_LARGE", "EXPLOSION");
        }
    },
    PORTAL {
        @Override
        public Particle get() {
            return Particle.PORTAL;
        }
    },
    REDSTONE {
        @Override
        public Particle get() {
            return retrieve("REDSTONE", "DUST");
        }
    },
    SPELL {
        @Override
        public Particle get() {
            return retrieve("SPELL_MOB_AMBIENT", "OMINOUS_SPAWNING");
        }
    },
    SNOW {
        @Override
        public Particle get() {
            return retrieve("SNOWFLAKE", "SNOWFLAKE");
        }
    },
    SCULK {
        @Override
        public Particle get() {
            return retrieve("SCULK_CHARGE_POP", "SCULK_CHARGE_POP");
        }
    },
    LAVA {
      @Override
        public Particle get() {
            return retrieve("LAVA", "LAVA");
        }
    },
    WATER {
        @Override
        public Particle get() {
            return retrieve("WATER_BUBBLE", "NAUTILUS");
        }
    };

    public abstract Particle get();

    public Color getColor() {
        return null;
    }

    public <T> void spawn(World world, Location location, int count, double offsetX, double offsetY, double offsetZ, double extra) {
        spawn(world, location, count, offsetX, offsetY, offsetZ, extra, (T) getColor());
    }

    protected <T> void spawn(World world, Location location, int count, double offsetX, double offsetY, double offsetZ, double extra, T data) {
        try {
            if (getColor() != null) {
                world.spawnParticle(get(), location, count, offsetX, offsetY, offsetZ, extra, data);
                return;
            }
        } catch (IllegalArgumentException e) {
            // 1.20+ support fallback
        }
        world.spawnParticle(get(), location, count, offsetX, offsetY, offsetZ, extra);
    }



    public Particle retrieve(String oldValue, String newValue) {
        try {
            return Particle.valueOf(oldValue);
        } catch (IllegalArgumentException e) {
            return Particle.valueOf(newValue);
        }
    }
}
