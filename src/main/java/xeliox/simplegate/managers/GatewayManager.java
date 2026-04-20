package xeliox.simplegate.managers;

import org.bukkit.Location;

import java.util.HashSet;
import java.util.Set;

public class GatewayManager {

    private final Set<Location> fakeGateways = new HashSet<>();

    public void addFakeGateway(Location location) {
        fakeGateways.add(normalizeLocation(location));
    }

    public Set<Location> getFakeGateways() {
        return fakeGateways;
    }

    public void removeFakeGateway(Location location) {
        fakeGateways.remove(normalizeLocation(location));
    }

    private Location normalizeLocation(Location location) {
        return new Location(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }
}
