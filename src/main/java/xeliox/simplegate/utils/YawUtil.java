package xeliox.simplegate.utils;

import org.bukkit.block.BlockFace;

public final class YawUtil {

    private YawUtil() {
    }

    public static Float getYaw(BlockFace face) {
        if (face == null) return null;

        switch (face) {
            case NORTH:
                return 0f;
            case EAST:
                return 90f;
            case SOUTH:
                return 180f;
            case WEST:
                return 270f;

            case NORTH_EAST:
                return 45f;
            case SOUTH_EAST:
                return 135f;
            case SOUTH_WEST:
                return 225f;
            case NORTH_WEST:
                return 315f;

            case WEST_NORTH_WEST:
                return 292.5f;
            case NORTH_NORTH_WEST:
                return 337.5f;
            case NORTH_NORTH_EAST:
                return 22.5f;
            case EAST_NORTH_EAST:
                return 67.5f;
            case EAST_SOUTH_EAST:
                return 112.5f;
            case SOUTH_SOUTH_EAST:
                return 157.5f;
            case SOUTH_SOUTH_WEST:
                return 202.5f;
            case WEST_SOUTH_WEST:
                return 247.5f;

            case UP:
            case DOWN:
            case SELF:
            default:
                return null;
        }
    }
}
