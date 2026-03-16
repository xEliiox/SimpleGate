package xeliox.simplegate.teleport;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.Serializable;

public class Destination implements Serializable {

    private String world;
    private BlockLocation location;
    private Heading heading;


    public Destination(String world, BlockLocation location, Heading heading) {
        this.world = world;
        this.location = location;
        this.heading = heading;
    }

    public String getWorld() {
        return world;
    }

    public BlockLocation getLocation() {
        return location;
    }

    public Heading getHeading() {
        return heading;
    }



    @JsonIgnore
    public World getBukkitWorld() {
        World bukkitWorld = Bukkit.getWorld(world);
        if (bukkitWorld == null) {
            throw new IllegalStateException("El mundo " + world + " no existe.");
        }
        return bukkitWorld;
    }

    @Override
    public String toString() {
        return "(" + world + ", " + location + ", " + heading + ")";
    }
}