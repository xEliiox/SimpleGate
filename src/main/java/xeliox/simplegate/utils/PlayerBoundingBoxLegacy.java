package xeliox.simplegate.utils;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public final class PlayerBoundingBoxLegacy extends AbstractBoundingBox {

    public PlayerBoundingBoxLegacy(Player player) {
        Location loc = player.getLocation();

        double width = 0.6 / 2.0;
        double height = 1.8;

        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();

        this.minX = x - width;
        this.minY = y;
        this.minZ = z - width;

        this.maxX = x + width;
        this.maxY = y + height;
        this.maxZ = z + width;
    }
}
