package xeliox.simplegate.gate;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import xeliox.simplegate.config.ParticleData;
import xeliox.simplegate.config.PortalType;
import xeliox.simplegate.SimpleGate;
import xeliox.simplegate.State;
import xeliox.simplegate.managers.GateManager;
import xeliox.simplegate.managers.GatewayManager;
import xeliox.simplegate.teleport.BlockLocation;
import xeliox.simplegate.teleport.Destination;
import xeliox.simplegate.teleport.TeleporterException;
import xeliox.simplegate.utils.*;


import java.lang.reflect.Method;
import java.util.*;


@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id"
)
public class Gate {


    private static final Map<Gate, BukkitTask> activeTasks = new HashMap<>();

    private final SimpleGate plugin = SimpleGate.getInstance();
    private PortalType portalType;
    public Integer portalTypeId;
    public String networkId;
    public Destination exit;
    public String portalWorldName;
    public UUID creatorId;
    public long creationTimeMillis;
    public int id;
    private boolean fakeEndGateway = false;

    protected boolean restricted = false;
    protected boolean enterEnabled = true;
    protected boolean exitEnabled = true;

    private Set<BlockLocation> frameCoords = new HashSet<>();
    private Set<BlockLocation> portalCoords = new HashSet<>();
    public GateOrientation orientation;
    private final GatewayManager gatewayManager = plugin.getGatewayManager();

    public Gate() {

    }


    public Gate(String networkId,
                Destination exit,
                Set<BlockLocation> frameCoords,
                GateOrientation orientation,
                Set<BlockLocation> portalCoords,
                UUID creatorId) {

        this.networkId = networkId;
        this.exit = exit;
        this.portalWorldName = exit != null ? exit.getWorld() : null;
        this.orientation = orientation;
        this.creatorId = creatorId;
        this.creationTimeMillis = System.currentTimeMillis();
        setFrameCoords(frameCoords);
        setPortalCoords(portalCoords);
    }

    public String getPortalWorldName() {
        return portalWorldName != null ? portalWorldName : (exit != null ? exit.getWorld() : null);
    }

    public boolean containsMaterial(Material material) {
        List<Block> blocks = getPortalBlocks();
        if (blocks == null) return false;
        for (Block block : blocks) {
            if (block.getType() == material) return true;
        }
        return false;
    }


    private int calcId() {
        HashCodeBuilder builder = new HashCodeBuilder();
        frameCoords.stream()
                .sorted(Comparator
                        .comparingInt(BlockLocation::getBlockX)
                        .thenComparingInt(BlockLocation::getBlockY)
                        .thenComparingInt(BlockLocation::getBlockZ))
                .forEach(builder::append);
        return builder.hashCode();
    }


    public void setFrameCoords(Set<BlockLocation> coords) {
        this.frameCoords = new HashSet<>(coords);
        this.id = calcId();
    }

    public void setPortalCoords(Set<BlockLocation> coords) {
        this.portalCoords = new HashSet<>(coords);
        this.id = calcId();
    }

    public Set<BlockLocation> getFrameCoords() {
        return Collections.unmodifiableSet(frameCoords);
    }

    public Set<BlockLocation> getPortalCoords() {
        return Collections.unmodifiableSet(portalCoords);
    }

    public boolean isCreator(Player player) {
        return player.getUniqueId().equals(creatorId);
    }

    public boolean overlapsWith(Gate other) {
        Set<BlockLocation> allCoords = new HashSet<>(this.frameCoords);
        allCoords.addAll(this.portalCoords);
        return

        other.frameCoords.stream().anyMatch(allCoords::contains) ||
        other.portalCoords.stream().anyMatch(allCoords::contains);
    }

    public void destroy() {
        GateManager.remove(creatorId, this);
        empty();
        if (isFakeEndGateway()) {
            List<Block> blocks = getPortalBlocks();
            if (blocks != null) {
                for (Block block : blocks) {
                    gatewayManager.removeFakeGateway(block.getLocation());
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        removeFakeEndGateway(player,block.getLocation());
                    }
                }
            }
            setFakeEndGateway(false);
        }
        stopPortalParticles();
    }

    public void toggleMode() {
        if (enterEnabled) {
            enterEnabled = false;
        } else {
            enterEnabled = true;
            exitEnabled = !exitEnabled;
        }
    }

    public boolean hasNoExitEnabled() {
        return calcGatesInChainAfterThis().stream().noneMatch(gate -> gate.exitEnabled);
    }

    public void transport(Player player) throws TeleporterException {
        for (Gate gate : calcGatesInChainAfterThis()) {
            if (!gate.exitEnabled) continue;
            fxKitUse(player);
            TeleportUtil.teleport(player, gate.exit);
            return;
        }
    }

    public void playerKb(Player player, double s, double h) {
        Vector direction = player.getLocation().getDirection().multiply(-1);

        if (direction.getY() < -0.7) {
            direction.setY(0);
        } else {
            direction.setY(h);
        }

        direction.multiply(s);
        player.setVelocity(direction);

        Sound sound = resolveNoDestinationSound();
        if (sound == null) return;

        player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
    }

    public List<Gate> calcGatesInChainAfterThis() {
        List<Gate> chain = GateManager.getByNetworkId(creatorId, networkId);
        chain.sort(Comparator.comparingInt(g -> g.id));

        int index = chain.indexOf(this);
        if (index < 0) {
            throw new IllegalStateException("Gate not found in its own network chain");
        }

        List<Gate> result = new ArrayList<>();
        result.addAll(chain.subList(index + 1, chain.size()));
        result.addAll(chain.subList(0, index));
        return result;
    }

    @JsonIgnore
    public List<Block> getFrameBlocks() {
        return findBlocks(frameCoords);
    }

    @JsonIgnore
    public List<Block> getPortalBlocks() {
        return findBlocks(portalCoords);
    }



    private List<Block> findBlocks(Set<BlockLocation> coords) {
        String worldName = getPortalWorldName();
        if (worldName == null) return null;

        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;

        List<Block> blocks = new ArrayList<>();
        for (BlockLocation loc : coords) {
            blocks.add(world.getBlockAt(
                    loc.getBlockX(),
                    loc.getBlockY(),
                    loc.getBlockZ()
            ));
        }
        return blocks;
    }



    @JsonIgnore
    public Block getCenterBlock() {
        List<Block> blocks = getPortalBlocks();
        if (blocks == null || blocks.isEmpty()) return null;
        return blocks.get(blocks.size() / 2);
    }

    @JsonIgnore
    public boolean isIntact() {
        List<Block> blocks = getFrameBlocks();
        if (blocks == null) return true;
        for (Block block : blocks) {
            if (VoidUtil.isVoid(block)) return false;
        }
        return true;
    }

    private boolean isPortalMaterial(Material material) {
        if (VersionCheck.serverIsLegacy()) {
            return material.name().equals("PORTAL");
        } else {
            return material == Material.NETHER_PORTAL;
        }
    }



    public void setContent(Material material) {
        List<Block> blocks = getPortalBlocks();
        if (blocks == null || blocks.isEmpty()) return;

        boolean isPortal = isPortalMaterial(material);

        if (!isPortal) {
            for (Block block : blocks) {
                block.setType(material);
            }
            return;
        }

        if (!VersionCheck.serverIsNew()) {
            for (Block block : blocks) {
                block.setType(material);
            }
            return;
        }

        try {
            Class<?> blockDataClass = Class.forName("org.bukkit.block.data.BlockData");
            Class<?> orientableClass = Class.forName("org.bukkit.block.data.Orientable");
            Class<?> axisClass = Class.forName("org.bukkit.Axis");

            String axisName = getOrientation() == GateOrientation.NS ? "Z" : "X";
            Object axis = Enum.valueOf(axisClass.asSubclass(Enum.class), axisName);

            Method createBlockData = material.getClass().getMethod("createBlockData");
            Method setBlockData = Block.class.getMethod("setBlockData", blockDataClass, boolean.class);
            Method setAxis = orientableClass.getMethod("setAxis", axisClass);

            for (Block block : blocks) {
                try {
                    Object blockData = createBlockData.invoke(material);
                    
                    if (orientableClass.isInstance(blockData)) {
                        setAxis.invoke(blockData, axis);
                        setBlockData.invoke(block, blockData, false);
                    } else {
                        block.setType(material);
                    }
                } catch (Exception e) {
                    block.setType(material);
                }
            }
        } catch (Exception e) {
            for (Block block : blocks) {
                block.setType(material);
            }
        }
    }

    public void sendFakeEndGateway(Player player, Location loc) {

        ProtocolManager manager = ProtocolLibrary.getProtocolManager();

        PacketContainer packet = manager.createPacket(PacketType.Play.Server.BLOCK_CHANGE);

        packet.getBlockPositionModifier().write(0,
                new BlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));

        packet.getBlockData().write(0,
                WrappedBlockData.createData(Material.END_GATEWAY));

        try {
            manager.sendServerPacket(player, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeFakeEndGateway(Player player, Location loc) {

        ProtocolManager manager = ProtocolLibrary.getProtocolManager();

        PacketContainer packet = manager.createPacket(PacketType.Play.Server.BLOCK_CHANGE);

        packet.getBlockPositionModifier().write(0,
                new BlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));

        packet.getBlockData().write(0, WrappedBlockData.createData(Material.AIR));

        try {
            manager.sendServerPacket(player, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isMaterialAir(Material material) {
        try {
            Method m = Material.class.getMethod("isAir");
            return (boolean) m.invoke(material);
        } catch (Throwable ignored) {
            String name = material.name();
            return name.equals("AIR")
                    || name.equals("CAVE_AIR")
                    || name.equals("VOID_AIR");
        }
    }


    public boolean isEmpty() {
        List<Block> blocks = getPortalBlocks();
        if (blocks == null) return true;
        for (Block block : blocks) {
            if (!isMaterialAir(block.getType())) return false;
        }
        return true;
    }


    public void customFill(Material material) {
        State.isFilling = true;
        if (material.name().equals("END_GATEWAY")) {
            setFakeEndGateway(true);
            List<Block> blocks = getPortalBlocks();
            if (blocks != null) {
                for (Block block : blocks) {
                    block.setType(Material.AIR);
                    gatewayManager.addFakeGateway(block.getLocation());
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        sendFakeEndGateway(p, block.getLocation());
                    }
                }
            }
            State.isFilling = false;
            return;
        }
        setFakeEndGateway(false);
        setContent(material);
        State.isFilling = false;
    }


    public Material getCurrentMaterial() {
        List<Block> blocks = getPortalBlocks();
        return (blocks != null && !blocks.isEmpty()) ? blocks.get(0).getType() : Material.AIR;
    }

    public void empty() {
        setContent(Material.AIR);
    }

    public void fxKitCreate() {
        fxKitUse();
        startPortalParticles();
    }


    public void startPortalParticles() {
        if (activeTasks.containsKey(this)) return;

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isIntact()) {
                    stopPortalParticles();
                    return;
                }
                fxPortalParticles();
            }
        }.runTaskTimer(plugin, 0L, 5L);

        activeTasks.put(this, task);
    }



    public void stopPortalParticles() {
        BukkitTask task = activeTasks.remove(this);
        if (task != null) task.cancel();
    }



    private ParticleData resolveParticle(PortalType portalType) {
        Particle configParticle = portalType.getParticleFromConfig();
        Color color = portalType.getParticleColorFromConfig();

        if (configParticle != null) {

            if (configParticle.getDataType() == Particle.DustOptions.class) {
                if (color == null) return null;
                return new ParticleData(
                        configParticle,
                        new Particle.DustOptions(color, 1.0f)
                );
            }

            if (configParticle.getDataType() == Color.class) {
                if (color == null) return null;
                return new ParticleData(configParticle, color);
            }

            return new ParticleData(configParticle);
        }

        // Fallback por material
        switch (portalType.getContentMaterial()) {
            case WATER:
                return new ParticleData(ParticleType.WATER.get());
            case LAVA:
                return new ParticleData(ParticleType.LAVA.get());
            case POWDER_SNOW:
                return new ParticleData(ParticleType.SNOW.get());
            case SCULK_VEIN:
                return new ParticleData(ParticleType.SCULK.get());
            default:
                return new ParticleData(ParticleType.PORTAL.get());
        }
    }


    public void fxPortalParticles() {
        Block block = getCenterBlock();
        if (block == null) return;

        Location loc = block.getLocation().add(0.5, 0.5, 0.5);
        World world = loc.getWorld();
        if (world == null) return;

        PortalType portalType = getPortalType();
        ParticleData particleData;

        if (portalType != null) {
            particleData = resolveParticle(portalType);
        } else {
            // Fallback directo si no hay PortalType
            Material material = getCurrentMaterial();
            Particle fallbackParticle;
            switch (material) {
                case WATER:
                    fallbackParticle = ParticleType.WATER.get();
                    break;
                case LAVA:
                    fallbackParticle = ParticleType.LAVA.get();
                    break;
                case POWDER_SNOW:
                    fallbackParticle = ParticleType.SNOW.get();
                    break;
                case SCULK_VEIN:
                    fallbackParticle = ParticleType.SCULK.get();
                    break;
                default:
                    fallbackParticle = ParticleType.PORTAL.get();
                    break;
            }
            particleData = new ParticleData(fallbackParticle);
        }

        int count = 10;
        double offset = 0.8;

        if (particleData != null) {

            Particle particle = particleData.getParticle();

            if (particleData.getDustOptions() != null) {
                world.spawnParticle(
                        particle,
                        loc,
                        count,
                        offset, offset, offset,
                        0.0,
                        particleData.getDustOptions()
                );
                return;
            }

            if (particleData.getColor() != null) {
                world.spawnParticle(
                        particle,
                        loc,
                        count,
                        offset, offset, offset,
                        0.0,
                        particleData.getColor()
                );
                return;
            }

            if (particle.getDataType() == Void.class) {
                world.spawnParticle(
                        particle,
                        loc,
                        count,
                        offset, offset, offset,
                        0.0
                );
            }
        }

    }

    public PortalType getPortalType() {
        return portalType;
    }

    public GateOrientation getOrientation() {
        return orientation;
    }


    public boolean isFakeEndGateway() {
        return fakeEndGateway;
    }

    public boolean isExitEnabled() {
        return exitEnabled;
    }

    public boolean isEnterEnabled() {
        return enterEnabled;
    }

    public boolean isRestricted() {
        return restricted;
    }

    public void setFakeEndGateway(boolean fakeEndGateway) {
        this.fakeEndGateway = fakeEndGateway;
    }

    public void setPortalType(PortalType portalType) {
        this.portalType = portalType;
    }

    public void fxKitUse(Player player) {
        if (plugin.getConfigManager().isSoundTeleportEnabled()) return;
        playPortalSound(player);
    }

    public void fxKitUse() {
        if (plugin.getConfigManager().isSoundTeleportEnabled()) return;
        Block block = getCenterBlock();
        if (block == null) return;
        Sound sound = resolvePortalSound();
        if (sound == null) return;
        block.getWorld().playSound(block.getLocation(), sound, 1.0f, 1.0f);
    }

    private static final Map<String, Effect> SOUND_TO_EFFECT = new HashMap<>();
    static {
        SOUND_TO_EFFECT.put("ENTITY_GHAST_SHOOT", Effect.GHAST_SHOOT);
        SOUND_TO_EFFECT.put("ENTITY_GHAST_SHRIEK", Effect.GHAST_SHRIEK);
        SOUND_TO_EFFECT.put("ENTITY_BLAZE_SHOOT", Effect.BLAZE_SHOOT);
    }

    private void playPortalSound(Player player) {
        String configuredSound = plugin.getConfigManager().getPortalSound();

        boolean played = false;

        Sound sound = Util.getSoundByName(configuredSound);
        if (sound != null) {
            player.getWorld().playSound(player.getLocation(), sound, 1.0f, 1.0f);
            played = true;
        }

        Effect effect = SOUND_TO_EFFECT.get(configuredSound.toUpperCase());
        if (effect != null) {
            player.getWorld().playEffect(player.getLocation(), effect, 0);
            played = true;
        }

        if (!played) {
            Bukkit.getLogger().warning("[SimpleGate] No valid sound or effect found: " + configuredSound);
        }
    }

    private Sound resolvePortalSound() {
        String configuredSound = plugin.getConfigManager().getPortalSound();
        Sound sound = Util.getSoundByName(configuredSound);
        if (sound == null) sound = Util.getSoundByName("ENTITY_GHAST_SHOOT");
        if (sound == null) sound = Util.getSoundByName("GHAST_SHOOT");
        if (sound == null) Bukkit.getLogger().warning("No valid sound found for portal sound.");
        return sound;
    }

    private Sound resolveNoDestinationSound() {
        String configuredSound = plugin.getConfigManager().getPortalNoDestinationSound();
        Sound sound = Util.getSoundByName(configuredSound);
        if (sound == null) sound = Util.getSoundByName("ENTITY_BAT_TAKEOFF");
        if (sound == null) sound = Util.getSoundByName("BAT_TAKEOFF");
        if (sound == null) Bukkit.getLogger().warning("No valid sound found for portal no destination sound.");
        return sound;
    }
}
