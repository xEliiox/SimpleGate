package xeliox.simplegate.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xeliox.simplegate.SimpleGate;
import xeliox.simplegate.State;
import xeliox.simplegate.config.ConfigManager;
import xeliox.simplegate.config.Messages;
import xeliox.simplegate.gate.Gate;
import xeliox.simplegate.gate.GateManager;
import xeliox.simplegate.gate.GateOrientation;
import xeliox.simplegate.teleport.BlockLocation;
import xeliox.simplegate.teleport.Destination;
import xeliox.simplegate.teleport.Heading;
import xeliox.simplegate.teleport.TeleporterException;
import xeliox.simplegate.utils.*;

import java.util.*;
import java.util.stream.Collectors;

public class GateListener implements Listener {

    final SimpleGate plugin = SimpleGate.getInstance();
    final ConfigManager config = plugin.getConfigManager();

    private void clearFire(Player player) {
        player.setFireTicks(0);
        player.setVisualFire(false);
    }
    /* ---------------- PORTAL STABILITY ---------------- */

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBreakPortalContent(BlockBreakEvent event) {
        Gate gate = GateManager.Portals.get(event.getBlock());
        if (gate == null) return;

        if (gate.isFakeEndGateway()) {
            event.setCancelled(true);
            Bukkit.getScheduler().runTaskLater(plugin, () -> gate.sendFakeEndGateway(event.getPlayer(),event.getBlock().getLocation()), 2L);
            return;
        }

        if (gate.getPortalCoords().contains(BlockLocation.fromBlock(event.getBlock()))) {
            event.setCancelled(true);
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerCombust(EntityCombustEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        Gate gate = GateManager.Portals.get(player.getLocation().getBlock());
        if (gate == null) return;

        event.setCancelled(true);
        clearFire(player);
    }


    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void stabilizePortalContent(BlockPhysicsEvent event) {
        Block block = event.getBlock();
        Material portalMaterial = VersionCheck.serverIsLegacy()  ? Material.valueOf("PORTAL") : Material.valueOf("NETHER_PORTAL");
        if (block.getType() != portalMaterial) return;

        if (!State.isFilling && !isPortalBlockStable(block)) return;

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)

    public void onLavaFlow(BlockFromToEvent event) {
        if (event.getBlock().getType() != Material.LAVA) return;

        if (isNearPortal(event.getToBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)

    public void onIgnite(BlockIgniteEvent event) {
        if (isNearPortal(event.getBlock())) {
            event.setCancelled(true);
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)

    public void onBurn(BlockBurnEvent event) {
        if (isNearPortal(event.getBlock())) {
            event.setCancelled(true);
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFireSpread(BlockSpreadEvent event) {
        if (event.getNewState().getType() != Material.FIRE) return;

        if (isNearPortal(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void stabilizePortalContent(BlockPlaceEvent event) {
        stabilizePortalContentBlock(event.getBlock(), event);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void stabilizePortalContent(PlayerBucketFillEvent event) {
        stabilizePortalContentBlock(event.getBlockClicked(), event);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void stabilizePortalContent(PlayerBucketEmptyEvent event) {
        stabilizePortalContentBlock(event.getBlockClicked(), event);
    }

    private boolean isNearPortal(Block block) {
        Gate gate = GateManager.Portals.get(block);
        if (gate != null) return true;

        for (BlockFace face : BlockFace.values()) {
            if (GateManager.Portals.get(block.getRelative(face)) != null) {
                return true;
            }
        }
        return false;
    }

    /* ---------------- VANILLA PORTALS ---------------- */

    @EventHandler(priority = EventPriority.NORMAL)
    public void disableVanillaGates(PlayerPortalEvent event) {
        disableVanillaGates(event.getFrom(), event);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void disableVanillaGates(EntityPortalEvent event) {
        disableVanillaGates(event.getFrom(), event);
    }

    /* ---------------- SPAWN CONTROL ---------------- */

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void noZombiePigmanPortalSpawn(CreatureSpawnEvent event) {
        if (event.getEntityType() != EntityType.ZOMBIFIED_PIGLIN) return;
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NETHER_PORTAL) return;
        if (!isGateNearby(event.getLocation().getBlock())) return;
        if (config.isPigmanPortalSpawnEnabled()) return;

        event.setCancelled(true);
    }

    /* ---------------- GATE USE ---------------- */

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void useGate(PlayerMoveEvent event) throws TeleporterException {

        if (LocationComparatorUtil.isSameBlock(event)) return;

        Player player = event.getPlayer();
        Location to = event.getTo();
        Material endGatewayMaterial = Material.valueOf("END_GATEWAY");
        if (to == null) return;

        Gate gate = getGateAtOrNear(to);
        if (gate == null) return;

        if (!gate.isIntact()) {
            gate.destroy();
            return;
        }

        if (player.isDead()) return;

        if (gate.hasNoExitEnabled()) {
            if (gate.containsMaterial(Material.LAVA)) {
                player.sendMessage(Messages.PREFIX.getMessage() + Messages.GATE_DESTINATION_NOT_FOUND.getMessage());
                return;
            }
            player.sendMessage(Messages.PREFIX.getMessage() + Messages.GATE_DESTINATION_NOT_FOUND.getMessage());
            gate.playerKb(player,0.6,0.4);
            return;
        }

        gate.transport(player);
    }

    /* ---------------- HELPERS ---------------- */

    private Gate getGateAtOrNear(Location location) {
        Block feet = location.getBlock();

        Gate gate = GateManager.Portals.get(feet);
        if (gate != null) return gate;

        Block head = feet.getRelative(0, 1, 0);
        return GateManager.Portals.get(head);
    }


    /* ---------------- GATE DESTRUCTION ---------------- */

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void destroyGate(BlockBreakEvent event) {

        boolean destroyed = destroyGate(event.getBlock());
        if (!destroyed) return;

        event.getPlayer().sendMessage(Messages.PREFIX.getMessage() + Messages.GATE_DESTROYED.getMessage());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void destroyGate(EntityChangeBlockEvent event) {
        destroyGate(event.getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void destroyGate(BlockFadeEvent event) {
        destroyGate(event.getBlock());

    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void destroyGate(BlockBurnEvent event) {
        destroyGate(event.getBlock());
    }

    /* ---------------- TOOLS ---------------- */

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void tools(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block clickedBlock = event.getClickedBlock();
        Material materialCreate = config.getItemRequisites();
        if (clickedBlock == null || event.getItem() == null) return;

        ItemStack currentItem = event.getItem();
        Material material = currentItem.getType();

        if (material != materialCreate) {
            return;
        }


        Set<Gate> currentGates = new HashSet<>(GateManager.Frames.get(clickedBlock));
        Gate portalGate = GateManager.Portals.get(clickedBlock);


        if (portalGate != null) {
            currentGates.add(portalGate);
        }


        if (material == materialCreate && event.getAction() == Action.RIGHT_CLICK_BLOCK) {



            if (!currentItem.getItemMeta().hasDisplayName()) {
                player.sendMessage(Messages.PREFIX.getMessage() + Messages.ITEM_RENAMED_TO_CREATE.getMessage().replace(
                        "{0}", Util.getMaterialName(material)
                ));
                return;
            }


            String worldName = player.getWorld().getName();

            if (!player.hasPermission("simplegate.worldbypass")
                    && config.getDisabledWorlds().stream().anyMatch(w -> w.equalsIgnoreCase(worldName))) {
                player.sendMessage(Messages.PREFIX.getMessage() + Messages.WORLD_DISABLED.getMessage());
                return;
            }


            String networkId = ChatColor.stripColor(currentItem.getItemMeta().getDisplayName());

            BlockFace face = event.getBlockFace();

            FloodUtil.FloodInfo floodInfo =
                    FloodUtil.getGateFloodInfo(clickedBlock, face);

            if (floodInfo == null) {
                player.sendMessage(Messages.PREFIX.getMessage() + Messages.FRAME_BIG.getMessage());
                return;
            }

            GateOrientation orientation = floodInfo.getGateOrientation();
            Set<Block> frameBlocks = floodInfo.getFrameBlocks();
            Set<Block> portalBlocks = floodInfo.getPortalBlocks();


            Set<Block> solidFrameBlocks = frameBlocks.stream()
                    .filter(b -> !VoidUtil.isVoid(b))
                    .collect(Collectors.toSet());


            Map<Material, Long> materialCounts = MaterialCountUtil.count(solidFrameBlocks);
            Map<Material,Long> required = config.getBlockRequiredToCreatePortal();

            if (!player.hasPermission("simplegate.framebypass")) {
                if (!MaterialCountUtil.has(materialCounts, required)) {

                    String reqBlocks = MaterialCountUtil.desc(required);

                    player.sendMessage(
                            Messages.PREFIX.getMessage() +
                                    Messages.FRAME_BLOCK_REQUIRED.getMessage()
                                            .replace("{0}", reqBlocks)
                    );
                    return;
                }
            }

            BlockLocation playerLoc = BlockLocation.fromLocation(player.getLocation());
            BlockLocation gateLoc = BlockLocation.fromBlock(portalBlocks.iterator().next());

            Heading heading = new Heading(0f,
                    orientation.getExitYaw(playerLoc, gateLoc));

            Destination exit = new Destination(
                    player.getWorld().getName(),
                    playerLoc,
                    heading
            );

            Set<BlockLocation> frameCoords = frameBlocks.stream()
                    .map(BlockLocation::fromBlock)
                    .collect(Collectors.toSet());

            Set<BlockLocation> portalCoords = portalBlocks.stream()
                    .map(BlockLocation::fromBlock)
                    .collect(Collectors.toSet());

            Gate gate = new Gate(
                    networkId,
                    exit,
                    frameCoords,
                    orientation,
                    portalCoords,
                    player.getUniqueId()
            );


            gate.portalWorldName = player.getWorld().getName();


            int existing = GateManager.getByNetworkId(gate.creatorId, networkId).size();

            if (existing >= 2) {
                player.sendMessage(
                        Messages.PREFIX.getMessage()
                                + Messages.GATE_LIMIT_REACHED.getMessage()
                                .replace("{0}", networkId)
                );
                return;
            }

            for (Gate existingGate : GateManager.getGatesByChunk(gate.getCenterBlock().getChunk())) {
                if(!existingGate.portalWorldName.equals(gate.portalWorldName)) continue;

                if (gate.overlapsWith(existingGate)) {
                    player.sendMessage(
                            Messages.PREFIX.getMessage()
                                    + Messages.GATE_OVERLAP.getMessage()
                                    .replace("{0}", existingGate.networkId)
                    );
                    return;
                }
            }


            GateManager.register(player.getUniqueId(), gate, true);

            boolean openSelector =
                    config.getPortalTypeSection() != null
                            && !config.getPortalTypeSection().getKeys(false).isEmpty();

            if (openSelector) {
                plugin.getPortalSelectorListener().open(player, gate, () -> {
                    gate.fxKitCreate();

                    player.sendMessage(
                            Messages.PREFIX.getMessage()
                                    + Messages.GATE_CREATED.getMessage().replace("{0}", networkId)
                    );
                });
            } else {
                Material material1 = VersionCheck.serverIsLegacy() ? Material.valueOf("PORTAL") : Material.valueOf("NETHER_PORTAL");
                gate.customFill(material1);
                gate.fxKitCreate();

                player.sendMessage(
                        Messages.PREFIX.getMessage()
                                + Messages.GATE_CREATED.getMessage().replace("{0}", networkId)
                );
            }

            if (config.isRemovingCreateToolItem()) {
                decreaseOne(event);
            } else if (config.isRemovingCreateToolName()) {
                decreaseOne(event);

                ItemStack newItem = new ItemStack(materialCreate);
                ItemMeta itemMeta = newItem.getItemMeta();
                assert itemMeta != null;
                itemMeta.setDisplayName(null);
                newItem.setItemMeta(itemMeta);
                newItem.setAmount(1);
                player.getInventory().addItem(newItem);

                InventoryUtil.updateSoon(player);
            }
        }
    }

    /* ---------------- STATIC HELPERS ---------------- */

    public static boolean isGateNearby(Block block) {
        for (int x = -3; x <= 3; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -3; z <= 3; z++) {
                    if (GateManager.Portals.get(block.getRelative(x, y, z)) != null) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isPortalBlockStable(Block block) {
        if (VoidUtil.isVoid(block.getRelative(0, 1, 0))) return false;
        if (VoidUtil.isVoid(block.getRelative(0, -1, 0))) return false;

        if (!VoidUtil.isVoid(block.getRelative(1, 0, 0))
                && !VoidUtil.isVoid(block.getRelative(-1, 0, 0))) {
            return true;
        }

        return !VoidUtil.isVoid(block.getRelative(0, 0, 1))
                && !VoidUtil.isVoid(block.getRelative(0, 0, -1));
    }

    public static void stabilizePortalContentBlock(Block block, Cancellable cancellable) {
        if (block == null) return;
        if (GateManager.Portals.get(block) == null) return;
        cancellable.setCancelled(true);
    }

    public static void disableVanillaGates(Location location, Cancellable cancellable) {
        if (isGateNearby(location.getBlock())) {
            cancellable.setCancelled(true);
        }
    }

    public static boolean destroyGate(Block block) {
        if (block == null) return false;

        Set<Gate> gates = new HashSet<>(GateManager.Frames.get(block));
        if (gates.isEmpty()) return false;

        gates.forEach(Gate::destroy);
        return true;
    }


    /* ---------------- INVENTORY ---------------- */

    private void decreaseOne(PlayerInteractEvent event) {
        ItemStack currentItem = event.getItem();
        ItemStack newItem = new ItemStack(currentItem);
        newItem.setAmount(newItem.getAmount() - 1);

        if (currentItem.equals(InventoryUtil.getWeapon(event.getPlayer()))) {
            InventoryUtil.setWeapon(event.getPlayer(), newItem);
            return;
        }

        if (currentItem.equals(InventoryUtil.getShield(event.getPlayer()))) {
            InventoryUtil.setShield(event.getPlayer(), newItem);
            return;
        }

        ListIterator<ItemStack> it = event.getPlayer().getInventory().iterator();
        while (it.hasNext()) {
            if (it.next() == currentItem) {
                it.set(newItem);
                return;
            }
        }

        throw new RuntimeException("Failed to decrease item amount");
    }
}
