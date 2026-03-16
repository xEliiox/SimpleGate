package xeliox.simplegate.config;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;

import java.util.List;

public class PortalType {

    private final Material iconMaterial;
    private final Material contentMaterial;
    private final int slot;
    private final String name;
    private final List<String> lore;
    private final String particleEffectName;
    private final String particleColorString;

    public PortalType(Material iconMaterial,
                      Material contentMaterial,
                      int slot,
                      String name,
                      List<String> lore,
                      String particleEffectName, String particleColorString) {

        this.iconMaterial = iconMaterial;
        this.contentMaterial = contentMaterial;
        this.slot = slot;
        this.name = name;
        this.lore = lore;
        this.particleEffectName = particleEffectName;
        this.particleColorString = particleColorString;
    }

    public Material getIconMaterial() {
        return iconMaterial;
    }

    public Material getContentMaterial() {
        return contentMaterial;
    }

    public int getSlot() {
        return slot;
    }

    public String getName() {
        return name;
    }

    public List<String> getLore() {
        return lore;
    }


/**
 * Retrieves a Particle enum value based on the configured particle effect name.
 * This method handles cases where the particle effect name might be null, empty, or invalid.
 *
 * @return The corresponding Particle enum value, or PORTAL if the specified name is invalid,
 *         or null if the particleEffectName is null or empty.
 */
    public Particle getParticleFromConfig() {
    // Check if particleEffectName is null or empty after trimming whitespace
        if (particleEffectName == null || particleEffectName.trim().isEmpty()) {
            return null;
        }

        try {
        // Try to get the Particle enum value matching the configured name (case-insensitive)
            return Particle.valueOf(particleEffectName.toUpperCase());
        } catch (IllegalArgumentException e) {
        // Return default PORTAL particle if the specified name doesn't match any Particle enum value
            return Particle.PORTAL;
        }
    }

    public Color getParticleColorFromConfig() {
        if (particleColorString == null || particleColorString.trim().isEmpty()) {
            return null;
        }

        switch (particleColorString.toUpperCase()) {
            case "RED":
                return Color.RED;
            case "GREEN":
                return Color.GREEN;
            case "BLUE":
                return Color.BLUE;
            case "YELLOW":
                return Color.YELLOW;
            case "ORANGE":
                return Color.ORANGE;
            case "PURPLE":
                return Color.PURPLE;
            case "BLACK":
                return Color.BLACK;
            case "AQUA":
                return Color.AQUA;
            case "FUCHSIA":
                return Color.FUCHSIA;
            case "GRAY":
                return Color.GRAY;
            case "LIME":
                return Color.LIME;
            case "MAROON":
                return Color.MAROON;
            case "NAVY":
                return Color.NAVY;
            case "OLIVE":
                return Color.OLIVE;
            case "SILVER":
                return Color.SILVER;
            case "TEAL":
                return Color.TEAL;
            case "WHITE":
            default:
                return Color.WHITE;
        }
    }
}
