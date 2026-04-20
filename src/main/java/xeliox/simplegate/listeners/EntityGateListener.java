package xeliox.simplegate.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scheduler.BukkitTask;
import xeliox.simplegate.SimpleGate;
import xeliox.simplegate.gate.Gate;
import xeliox.simplegate.managers.GateManager;
import xeliox.simplegate.teleport.Destination;
import xeliox.simplegate.teleport.TeleporterException;

import java.util.*;

public class EntityGateListener implements Listener {

    private static final long SCAN_INTERVAL_TICKS = 4L;
    private static final long COOLDOWN_MS = 2000L;
    private final SimpleGate plugin;
    private final Map<Integer, Long> cooldowns = new HashMap<>();
    private final Map<UUID, Gate> portalSpawnedIn = new HashMap<>();
    private BukkitTask scanTask;

    public EntityGateListener(SimpleGate plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (scanTask != null) return;
        scanTask = Bukkit.getScheduler().runTaskTimer(plugin, this::scanMobsInPortals, 20L, SCAN_INTERVAL_TICKS);
    }

    public void stop() {
        if (scanTask != null) {
            scanTask.cancel();
            scanTask = null;
        }
        cooldowns.clear();
        portalSpawnedIn.clear();
    }

    public void clearCooldownsForChunk(org.bukkit.Chunk chunk) {
        for (Entity entity : chunk.getEntities()) {
            cooldowns.remove(entity.getEntityId());
            portalSpawnedIn.remove(entity.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPortalSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NETHER_PORTAL) return;
        Gate gate = GateManager.Portals.get(event.getEntity().getLocation().getBlock());
        if (gate == null) return;
        portalSpawnedIn.put(event.getEntity().getUniqueId(), gate);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMobDeath(EntityDeathEvent event) {
        portalSpawnedIn.remove(event.getEntity().getUniqueId());
    }

    private void scanMobsInPortals() {
        if (!plugin.getConfigManager().isMobTeleportationEnabled()) return;

        for (Gate gate : GateManager.getAllGates()) {
            if (!gate.isIntact()) continue;
            if (gate.hasNoExitEnabled()) continue;

            List<org.bukkit.block.Block> portalBlocks = gate.getPortalBlocks();
            if (portalBlocks == null || portalBlocks.isEmpty()) continue;

            for (org.bukkit.block.Block block : portalBlocks) {
                for (Entity entity : block.getWorld().getNearbyEntities(
                        block.getLocation().add(0.5, 0.5, 0.5), 0.8, 0.8, 0.8)) {

                    if (!(entity instanceof LivingEntity)) continue;
                    if (entity instanceof Player) continue;
                    LivingEntity living = (LivingEntity) entity;

                    // VehicleGateListener
                    if (living.isInsideVehicle()) continue;
                    // LeashUtil
                    if (living.isLeashed()) {
                        continue;
                    }

                    if (isOnCooldown(living)) continue;

                    if (living instanceof Tameable) {
                        Tameable tameable = (Tameable) living;
                        if (tameable.isTamed()) {
                            if (!plugin.getConfigManager().isTamedMobTeleportationEnabled()) continue;
                            if (!isOwnerCrossingPortal(tameable, gate)) continue;
                        }
                    }

                    Gate spawnedGate = portalSpawnedIn.get(living.getUniqueId());
                    if (spawnedGate != null && spawnedGate == gate) continue;

                    tryTransportMob(living, gate);
                }
            }
        }
    }

    private void tryTransportMob(LivingEntity entity, Gate gate) {
        try {
            transportMob(entity, gate);
        } catch (TeleporterException e) {
            plugin.getLogger().warning("Failed to teleport mob " + entity.getType() +
                    " (ID: " + entity.getEntityId() + "): " + e.getMessage());
        }
    }

    private void transportMob(LivingEntity entity, Gate gate) throws TeleporterException {
        for (Gate exitGate : gate.calcGatesInChainAfterThis()) {
            if (!exitGate.isExitEnabled()) continue;

            Location target = buildLocation(exitGate.exit);

            entity.eject();
            if (entity.getVehicle() != null) entity.getVehicle().eject();
            entity.teleport(target);

            portalSpawnedIn.remove(entity.getUniqueId());
            setCooldown(entity);

            Bukkit.getScheduler().runTaskLater(plugin, () -> entity.setFireTicks(0), 2L);

            gate.fxKitUse();
            return;
        }
    }

    private Location buildLocation(Destination destination) throws TeleporterException {
        try {
            World world = destination.getBukkitWorld();
            double x = destination.getLocation().getBlockX() + 0.5;
            double y = destination.getLocation().getBlockY();
            double z = destination.getLocation().getBlockZ() + 0.5;
            float pitch = destination.getHeading().getPitch();
            float yaw   = destination.getHeading().getYaw();
            return new Location(world, x, y, z, yaw, pitch);
        } catch (Exception e) {
            throw new TeleporterException("Invalid mob teleport destination: " + e.getMessage());
        }
    }

    private boolean isOwnerCrossingPortal(Tameable tameable, Gate gate) {
        if (!(tameable.getOwner() instanceof Player)) return false;
        Player owner = (Player) tameable.getOwner();
        return GateManager.Portals.get(owner.getLocation().getBlock()) == gate;
    }

    private boolean isOnCooldown(Entity entity) {
        Long until = cooldowns.get(entity.getEntityId());
        return until != null && until > System.currentTimeMillis();
    }

    public void setCooldown(Entity entity) {
        cooldowns.put(entity.getEntityId(), System.currentTimeMillis() + COOLDOWN_MS);
    }
}