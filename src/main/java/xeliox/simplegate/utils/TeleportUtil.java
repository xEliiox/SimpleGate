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

        // Eject passengers and unmount before transport
        player.eject();
        if (player.getVehicle() != null) {
            player.getVehicle().eject();
        }



        Location finalLocation = location.clone();


        // Do the teleport
        player.teleport(finalLocation);

        Bukkit.getScheduler().runTask(SimpleGate.getInstance(), () -> {
            if (!player.isOnline()) return;
            player.setFireTicks(0);
        });
    }

    private static @NotNull Location getLocation(Destination destination) throws TeleporterException {
        Location location;
        try {
            World world = destination.getBukkitWorld();
            double locationX = destination.getLocation().getBlockX();
            double locationY = destination.getLocation().getBlockY();
            double locationZ = destination.getLocation().getBlockZ();
            float pitch = destination.getHeading().getPitch();
            float yaw = destination.getHeading().getYaw();
            location = new Location(world, locationX, locationY, locationZ, yaw, pitch);
        } catch (Exception e) {
            throw new TeleporterException(Messages.PREFIX.getMessage() + Messages.INVALID_LOCATION.getMessage() + e.getMessage());
        }
        return location;
    }
}
