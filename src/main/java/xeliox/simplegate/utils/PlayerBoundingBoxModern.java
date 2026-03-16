package xeliox.simplegate.utils;

import org.bukkit.entity.Player;

public final class PlayerBoundingBoxModern extends AbstractBoundingBox {

    public PlayerBoundingBoxModern(Player player) {
        org.bukkit.util.BoundingBox box = player.getBoundingBox();

        this.minX = box.getMinX();
        this.minY = box.getMinY();
        this.minZ = box.getMinZ();

        this.maxX = box.getMaxX();
        this.maxY = box.getMaxY();
        this.maxZ = box.getMaxZ();
    }
}
