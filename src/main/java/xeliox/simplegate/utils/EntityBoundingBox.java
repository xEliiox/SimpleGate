package xeliox.simplegate.utils;

import org.bukkit.entity.Entity;

public final class EntityBoundingBox extends AbstractBoundingBox {

    public EntityBoundingBox(Entity entity) {
        org.bukkit.util.BoundingBox box = entity.getBoundingBox();
        this.minX = box.getMinX();
        this.minY = box.getMinY();
        this.minZ = box.getMinZ();
        this.maxX = box.getMaxX();
        this.maxY = box.getMaxY();
        this.maxZ = box.getMaxZ();
    }
}
