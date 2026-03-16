package xeliox.simplegate.utils;

import org.bukkit.Location;
import org.bukkit.event.player.PlayerMoveEvent;

public class LocationComparatorUtil {

    public static boolean isSameBlock(PlayerMoveEvent event) {
        return isSameBlock(event.getFrom(), event.getTo());
    }

    private static boolean isSameBlock(Location one, Location two) {
        if (one.getBlockX() != two.getBlockX()) return false;
        if (one.getBlockZ() != two.getBlockZ()) return false;
        return one.getBlockY() == two.getBlockY() && one.getWorld().equals(two.getWorld());
    }
}