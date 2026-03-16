package xeliox.simplegate.config;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.jetbrains.annotations.Contract;

public class ParticleData {

    private final Particle particle;
    private final Particle.DustOptions dustOptions;
    private final Color color;

    @Contract(pure = true)
    public ParticleData(Particle particle) {
        this.particle = particle;
        this.dustOptions = null;
        this.color = null;
    }

    @Contract(pure = true)
    public ParticleData(Particle particle, Particle.DustOptions dustOptions) {
        this.particle = particle;
        this.dustOptions = dustOptions;
        this.color = null;
    }

    @Contract(pure = true)
    public ParticleData(Particle particle, Color color) {
        this.particle = particle;
        this.color = color;
        this.dustOptions = null;
    }

    public Particle getParticle() {
        return particle;
    }

    public Particle.DustOptions getDustOptions() {
        return dustOptions;
    }

    public Color getColor() {
        return color;
    }
}
