package xeliox.simplegate.utils;

import org.bukkit.block.Block;

public class BlockBoundingBox extends AbstractBoundingBox {

    public BlockBoundingBox(Block block) {
        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();

        this.minX = x;
        this.minY = y;
        this.minZ = z;

        this.maxX = x + 1;
        this.maxY = y + 1;
        this.maxZ = z + 1;
    }
}
