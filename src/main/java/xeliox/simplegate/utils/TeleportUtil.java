package xeliox.simplegate.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xeliox.simplegate.SimpleGate;
import xeliox.simplegate.config.Messages;
import xeliox.simplegate.teleport.Destination;
import xeliox.simplegate.teleport.TeleporterException;


public class TeleportUtil {

    public static void teleport(Player teleport, Destination destination) throws TeleporterException {
        teleport.sendMessage(Messages.PREFIX.getMessage() + Messages.TELEPORT_SUCCESS.getMessage().replace("{0}", destination.getLocation().toString()));
        teleportPlayer(teleport, destination);
    }

    public static void teleportPlayer(Player player, Destination destination) throws TeleporterException {
        Location location = getLocation(destination);
        player.teleport(location.clone());
        Bukkit.getScheduler().runTask(SimpleGate.getInstance(), () -> {
            if (!player.isOnline()) return;
            player.setFireTicks(0);
        });
    }

    private static @NotNull Location getLocation(Destination destination) throws TeleporterException {
        try {
            World world = destination.getBukkitWorld();
            double x = destination.getLocation().getBlockX() + 0.5;
            double y = destination.getLocation().getBlockY();
            double z = destination.getLocation().getBlockZ() + 0.5;
            float pitch = destination.getHeading().getPitch();
            float yaw = destination.getHeading().getYaw();
            return new Location(world, x, y, z, yaw, pitch);
        } catch (Exception e) {
            throw new TeleporterException(Messages.PREFIX.getMessage() + Messages.INVALID_LOCATION.getMessage() + e.getMessage());
        }
    }
}
