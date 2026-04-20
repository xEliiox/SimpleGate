package xeliox.simplegate.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import xeliox.simplegate.SimpleGate;
import xeliox.simplegate.gate.Gate;
import xeliox.simplegate.managers.GateManager;
import xeliox.simplegate.teleport.Destination;
import xeliox.simplegate.teleport.TeleporterException;

import java.util.*;

public class VehicleGateListener implements Listener {

    private static final long SCAN_INTERVAL_TICKS = 4L;
    private static final long COOLDOWN_MS = 2000L;
    private static final String METADATA_KEY = "simplegate_vehicle_tp";
    private final SimpleGate plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private BukkitTask scanTask;

    public VehicleGateListener(SimpleGate plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (scanTask != null) return;
        scanTask = Bukkit.getScheduler().runTaskTimer(plugin, this::scanVehiclesInPortals, 20L, SCAN_INTERVAL_TICKS);
    }

    public void stop() {
        if (scanTask != null) {
            scanTask.cancel();
            scanTask = null;
        }
        cooldowns.clear();
    }

    public void clearCooldownsForChunk(org.bukkit.Chunk chunk) {
        for (Entity entity : chunk.getEntities()) {
            cooldowns.remove(entity.getUniqueId());
        }
    }

    private void scanVehiclesInPortals() {
        if (!plugin.getConfigManager().isVehicleTeleportationEnabled()) return;

        for (Gate gate : GateManager.getAllGates()) {
            if (!gate.isIntact()) continue;
            if (gate.hasNoExitEnabled()) continue;

            List<org.bukkit.block.Block> portalBlocks = gate.getPortalBlocks();
            if (portalBlocks == null || portalBlocks.isEmpty()) continue;

            for (org.bukkit.block.Block block : portalBlocks) {
                for (Entity entity : block.getWorld().getNearbyEntities(
                        block.getLocation().add(0.5, 0.5, 0.5), 0.8, 0.8, 0.8)) {

                    if (!(entity instanceof Vehicle)) continue;
                    Vehicle vehicle = (Vehicle) entity;

                    if (isOnCooldown(vehicle)) continue;
                    if (!canVehicleTeleport(vehicle, gate)) continue;

                    tryTransportVehicle(vehicle, gate);
                }
            }
        }
    }

    private boolean canVehicleTeleport(Vehicle vehicle, Gate gate) {
        for (Entity passenger : vehicle.getPassengers()) {
            if (passenger instanceof Tameable) {
                Tameable tameable = (Tameable) passenger;
                if (tameable.isTamed()) {
                    if (!plugin.getConfigManager().isTamedMobTeleportationEnabled()) {
                        return false;
                    }
                    if (!isOwnerCrossingPortal(tameable, gate)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean isOwnerCrossingPortal(Tameable tameable, Gate gate) {
        if (!(tameable.getOwner() instanceof Player)) return false;
        Player owner = (Player) tameable.getOwner();
        return GateManager.Portals.get(owner.getLocation().getBlock()) == gate;
    }

    private void tryTransportVehicle(Vehicle vehicle, Gate gate) {
        try {
            transportVehicle(vehicle, gate);
        } catch (TeleporterException e) {
            plugin.getLogger().warning("Failed to teleport vehicle " + vehicle.getType() +
                    " (UUID: " + vehicle.getUniqueId() + "): " + e.getMessage());
        }
    }

    private void transportVehicle(Vehicle vehicle, Gate gate) throws TeleporterException {
        for (Gate exitGate : gate.calcGatesInChainAfterThis()) {
            if (!exitGate.isExitEnabled()) continue;

            Location target = buildLocation(exitGate.exit);

            List<Entity> passengers = new ArrayList<>(vehicle.getPassengers());
            Vector originalVelocity = vehicle.getVelocity().clone();

            target.getChunk().load();

            vehicle.setMetadata(METADATA_KEY, new FixedMetadataValue(plugin, originalVelocity));

            boolean success = vehicle.teleport(target);
            if (!success) {
                target.getChunk().load(true);
                vehicle.teleport(target);
            }

            new PassengerReattachTask(vehicle, passengers, originalVelocity).runTaskTimer(plugin, 2L, 3L);

            setCooldown(vehicle);
            gate.fxKitUse();
            return;
        }
    }

    private class PassengerReattachTask extends BukkitRunnable {
        private final Vehicle vehicle;
        private final List<Entity> expectedPassengers;
        private final Vector originalVelocity;
        private int attempts = 0;
        private boolean impulseApplied = false;

        public PassengerReattachTask(Vehicle vehicle, List<Entity> passengers, Vector originalVelocity) {
            this.vehicle = vehicle;
            this.expectedPassengers = new ArrayList<>(passengers);
            this.originalVelocity = originalVelocity.clone();
        }

        @Override
        public void run() {
            attempts++;

            if (!vehicle.isValid()) {
                cancel();
                return;
            }

            Location currentLoc = vehicle.getLocation();
            if (!currentLoc.getChunk().isLoaded()) {
                currentLoc.getChunk().load();
            }

            List<Entity> currentPassengers = vehicle.getPassengers();
            for (Entity expected : expectedPassengers) {
                if (!expected.isValid()) continue;
                if (!currentPassengers.contains(expected)) {
                    expected.teleport(currentLoc);
                    vehicle.addPassenger(expected);
                }
            }

            if (!impulseApplied && vehicle instanceof Minecart && attempts >= 2) {
                Minecart minecart = (Minecart) vehicle;
                double speed = Math.max(originalVelocity.length(), 0.4);
                Vector fixedDirection;
                if (originalVelocity.lengthSquared() > 0.001) {
                    fixedDirection = originalVelocity.clone().normalize().multiply(speed);
                } else {
                    fixedDirection = minecart.getLocation().getDirection().multiply(speed);
                }
                minecart.setVelocity(fixedDirection);
                minecart.setMaxSpeed(0.6D);
                minecart.setSlowWhenEmpty(false);
                minecart.setFireTicks(0);
                impulseApplied = true;
            } else {
                vehicle.setFireTicks(0);
            }

            boolean allPassengersOnboard = expectedPassengers.stream().allMatch(e -> !e.isValid() || vehicle.getPassengers().contains(e));
            boolean done = allPassengersOnboard && (!(vehicle instanceof Minecart) || impulseApplied);

            if (done || attempts >= 10) {
                if (attempts >= 10) {
                    plugin.getLogger().warning("Passenger reattachment for vehicle " + vehicle.getType() + " (UUID: " + vehicle.getUniqueId() + ") took too long, giving up.");
                }
                vehicle.removeMetadata(METADATA_KEY, plugin);
                cancel();
            }
        }
    }

    @EventHandler
    public void onEntityTeleport(EntityTeleportEvent event) {
        if (event.getEntity() instanceof Vehicle) {
            Vehicle vehicle = (Vehicle) event.getEntity();
            if (vehicle.hasMetadata(METADATA_KEY)) {
                // No Use case for now, but could be used for debugging or future features
            }
        }
    }

    private Location buildLocation(Destination destination) throws TeleporterException {
        try {
            World world = destination.getBukkitWorld();
            double x = destination.getLocation().getBlockX() + 0.5;
            double y = destination.getLocation().getBlockY() + 0.0625;
            double z = destination.getLocation().getBlockZ() + 0.5;
            float pitch = destination.getHeading().getPitch();
            float yaw   = destination.getHeading().getYaw();
            return new Location(world, x, y, z, yaw, pitch);
        } catch (Exception e) {
            throw new TeleporterException("Invalid vehicle teleport destination: " + e.getMessage());
        }
    }

    private boolean isOnCooldown(Vehicle vehicle) {
        Long until = cooldowns.get(vehicle.getUniqueId());
        return until != null && until > System.currentTimeMillis();
    }

    private void setCooldown(Vehicle vehicle) {
        cooldowns.put(vehicle.getUniqueId(), System.currentTimeMillis() + COOLDOWN_MS);
    }
}