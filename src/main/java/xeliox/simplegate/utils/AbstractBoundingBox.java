package xeliox.simplegate.utils;

public class AbstractBoundingBox implements IBoundingBox {

    protected double minX, minY, minZ;
    protected double maxX, maxY, maxZ;

    @Override public double getMinX() { return minX; }
    @Override public double getMinY() { return minY; }
    @Override public double getMinZ() { return minZ; }

    @Override public double getMaxX() { return maxX; }
    @Override public double getMaxY() { return maxY; }
    @Override public double getMaxZ() { return maxZ; }
}
