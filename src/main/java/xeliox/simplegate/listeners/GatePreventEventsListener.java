package xeliox.simplegate.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import xeliox.simplegate.SimpleGate;
import xeliox.simplegate.gate.Gate;
import xeliox.simplegate.gate.GateManager;
import xeliox.simplegate.gate.GatewayManager;
import xeliox.simplegate.utils.BoundingBoxFactory;
import xeliox.simplegate.utils.IBoundingBox;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class GatePreventEventsListener implements Listener {


    /**
     * Short protection window to absorb the exit tick damage
     * (~4–5 ticks is enough)
     */
    private static final long PROTECTION_MS = 250L;

    protected SimpleGate plugin = SimpleGate.getInstance();
    private final Map<Integer, Long> recentPortalContact = new ConcurrentHashMap<>();
    private final GatewayManager gatewayManager = plugin.getGatewayManager();

    private static final BlockFace[] SIDES = {
            BlockFace.NORTH, BlockFace.SOUTH,
            BlockFace.EAST, BlockFace.WEST,
            BlockFace.UP, BlockFace.DOWN
    };

    /* --------------------------------------------------
     * MAIN DAMAGE HANDLER
     * -------------------------------------------------- */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerFireDamagePortalContent(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        Gate gate = getGateAtOrNearBorder(player.getLocation());
        if (gate == null) return;

        if (event.getCause() == EntityDamageEvent.DamageCause.FIRE
                || event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK) {
            event.setCancelled(true);
            clearFire(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onGateDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();

        // 1) Check if player is touching a portal RIGHT NOW
        Gate gate = getGateAtOrNearBorder(player.getLocation());
        boolean touching = false;

        if (gate != null) {
            for (Block portalBlock : gate.getPortalBlocks()) {
                if (isPlayerActuallyTouchingGate(player, portalBlock)) {
                    touching = true;
                    break;
                }
            }
        }

        // 2) Refresh protection window if touching
        if (touching) {
            recentPortalContact.put(
                    player.getEntityId(),
                    System.currentTimeMillis() + PROTECTION_MS
            );
        }

        // 3) Check if still protected
        Long until = recentPortalContact.get(player.getEntityId());
        if (until == null || until < System.currentTimeMillis()) return;

        // 4) Cancel only relevant damage types
        EntityDamageEvent.DamageCause cause = event.getCause();

        if (isFreeze(cause)) {
            event.setCancelled(true);
            clearFreeze(player);
            return;
        }

        if (cause == EntityDamageEvent.DamageCause.DROWNING) {
            event.setCancelled(true);
            return;
        }

        if (isHeatDamageCause(cause)) {
            event.setCancelled(true);
            clearFire(player);
        }
    }


    private boolean isFreeze(EntityDamageEvent.DamageCause cause) {
        return cause.name().equals("FREEZE");
    }

    private boolean isHeatDamageCause(EntityDamageEvent.DamageCause cause) {
        return cause == EntityDamageEvent.DamageCause.FIRE
                || cause == EntityDamageEvent.DamageCause.FIRE_TICK
                || cause == EntityDamageEvent.DamageCause.LAVA
                || cause == EntityDamageEvent.DamageCause.HOT_FLOOR;
    }

    private void clearFire(Player player) {
        player.setFireTicks(0);
        try {
            player.setVisualFire(false);
        } catch (NoSuchMethodError ignored) {
        }
    }

    private void clearFreeze(Player player) {
        try {
            player.setFreezeTicks(0);
        } catch (NoSuchMethodError ignored) {
        }
    }

    /* --------------------------------------------------
     * GATE DETECTION
     * -------------------------------------------------- */
    public static Gate getGateAtOrNearBorder(Location location) {
        Block block = location.getBlock();

        Gate gate = GateManager.Portals.get(block);
        if (gate != null) return gate;

        for (BlockFace face : SIDES) {
            gate = GateManager.Portals.get(block.getRelative(face));
            if (gate != null) return gate;
        }

        return null;
    }

    /* --------------------------------------------------
     * FAKE GATEWAY UPDATER
     * -------------------------------------------------- */

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMoveChunk(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) return;

        if ((from.getBlockX() >> 4) == (to.getBlockX() >> 4)
                && (from.getBlockZ() >> 4) == (to.getBlockZ() >> 4)) return;

        sendNearbyFakeGateways(event.getPlayer(), to);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            sendNearbyFakeGateways(player, player.getLocation());
        }, 10L);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Location to = event.getTo();
        if (to == null) return;
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            sendNearbyFakeGateways(player, player.getLocation());
        }, 5L);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            sendNearbyFakeGateways(player, player.getLocation());
        }, 20L);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private void refreshFakeEndGatewayOnJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            sendNearbyFakeGateways(player, player.getLocation());
        }, 20L);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private void refreshFakeEndGatewayOnInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        Player player = event.getPlayer();
        Location location = event.getClickedBlock().getLocation();
        if (gatewayManager.getFakeGateways().contains(location)) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!player.isOnline()) return;
                Gate gate = GateManager.Portals.get(location.getBlock());
                if (gate != null && gate.isFakeEndGateway()) {
                    gate.sendFakeEndGateway(player, location);
                }
            }, 2L); // Delay to ensure the player has fully processed the interaction
        }
    }

    /* --------------------------------------------------
     * BOUNDING CHECK (LEGACY SAFE)
     * -------------------------------------------------- */
    public static boolean isPlayerActuallyTouchingGate(Player player, Block portalBlock) {
        IBoundingBox playerBox = BoundingBoxFactory.of(player);
        IBoundingBox blockBox = BoundingBoxFactory.of(portalBlock);
        return playerBox.intersects(blockBox);
    }

    /* --------------------------------------------------
     * NEARBY FAKE GATEWAY UPDATER
     * -------------------------------------------------- */
    private void sendNearbyFakeGateways(Player player, Location location) {
        World world = location.getWorld();
        Set<Location> snapshot = new java.util.HashSet<>(gatewayManager.getFakeGateways());

        for (Location loc : snapshot) {
            World locWorld = loc.getWorld();
            if (locWorld == null || !locWorld.equals(world)) continue;
            if (location.distanceSquared(loc) > 64 * 64) continue;

            Gate gate = GateManager.Portals.get(loc.getBlock());
            if (gate != null && gate.isFakeEndGateway()) {
                gate.sendFakeEndGateway(player, loc);
            }
        }
    }
}
