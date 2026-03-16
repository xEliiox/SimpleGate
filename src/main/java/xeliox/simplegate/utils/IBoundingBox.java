package xeliox.simplegate.utils;

public interface IBoundingBox {

    double getMinX();
    double getMinY();
    double getMinZ();

    double getMaxX();
    double getMaxY();
    double getMaxZ();

    default boolean intersects(IBoundingBox other) {
        return this.getMaxX() > other.getMinX()
                && this.getMinX() < other.getMaxX()
                && this.getMaxY() > other.getMinY()
                && this.getMinY() < other.getMaxY()
                && this.getMaxZ() > other.getMinZ()
                && this.getMinZ() < other.getMaxZ();
    }
}
