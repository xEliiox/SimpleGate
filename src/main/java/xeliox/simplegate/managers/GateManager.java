package xeliox.simplegate.managers;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import xeliox.simplegate.SimpleGate;
import xeliox.simplegate.gate.Gate;
import xeliox.simplegate.gate.GateMapper;
import xeliox.simplegate.config.PortalType;
import xeliox.simplegate.gate.dto.GateDTO;
import xeliox.simplegate.gate.dto.GateListDTO;
import xeliox.simplegate.teleport.BlockLocation;
import xeliox.simplegate.utils.JsonFileUtil;

import java.io.File;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public final class GateManager {

    public static final Map<UUID, Set<Gate>> gatesByPlayer = new HashMap<>();
    private static final Map<String, Map<Long, Set<Gate>>> gatesByChunk = new HashMap<>();

    /* ==============================
       World -> Location -> Gate
       ============================== */
    public static class WorldToLocationToGateMap {

        protected final Map<String, Map<BlockLocation, Gate>> map = new LinkedHashMap<>();

        protected Map<BlockLocation, Gate> getWorld(String world) {
            return map.computeIfAbsent(world, w -> new LinkedHashMap<>());
        }

        public Gate get(Block block) {
            return get(block.getLocation());
        }

        public Gate get(Location location) {
            String world = location.getWorld().getName();
            BlockLocation blockLocation = BlockLocation.fromLocation(location);
            return getWorld(world).get(blockLocation);
        }

        public void put(String world, BlockLocation location, Gate gate) {
            getWorld(world).put(location, gate);
        }

        public void remove(String world, BlockLocation location) {
            getWorld(world).remove(location);
        }
    }

    /* ==============================
       World -> Location -> Gates
       ============================== */
    public static class WorldToLocationToGatesMap {

        protected final Map<String, Map<BlockLocation, Set<Gate>>> map = new LinkedHashMap<>();

        public Set<Gate> get(Block block) {
            return get(block.getLocation());
        }

        public Set<Gate> get(Location location) {
            return get(location.getWorld().getName(), BlockLocation.fromLocation(location));
        }

        protected Set<Gate> getRaw(String world, BlockLocation location) {
            return map
                    .computeIfAbsent(world, w -> new LinkedHashMap<>())
                    .computeIfAbsent(location, l -> new LinkedHashSet<>());
        }

        public Set<Gate> get(String world, BlockLocation location) {
            return map.containsKey(world)
                    ? map.get(world).getOrDefault(location, Collections.emptySet())
                    : Collections.emptySet();
        }

        public void add(String world, BlockLocation location, Gate gate) {
            getRaw(world, location).add(gate);
        }

        public void remove(String world, BlockLocation location, Gate gate) {
            getRaw(world, location).remove(gate);
        }
    }

    /* ==============================
       Instancias globales
       ============================== */
    public static final WorldToLocationToGatesMap Frames = new WorldToLocationToGatesMap();
    public static final WorldToLocationToGateMap Portals = new WorldToLocationToGateMap();

    private static long chunkKey(int chunkX, int chunkZ) {
        return (((long) chunkX) << 32) ^ (chunkZ & 0xffffffffL);
    }

    private static long chunkKey(Chunk chunk) {
        return chunkKey(chunk.getX(), chunk.getZ());
    }

    private static long chunkKey(BlockLocation location) {
        return chunkKey(location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }

    private static Set<Gate> scanGatesInChunk(String worldName, int chunkX, int chunkZ) {
        long key = chunkKey(chunkX, chunkZ);
        Set<Gate> result = new HashSet<>();

        for (Set<Gate> set : gatesByPlayer.values()) {
            for (Gate gate : set) {
                if (!worldName.equals(gate.getPortalWorldName())) continue;

                Set<BlockLocation> coords = gate.getPortalCoords();
                if (coords == null || coords.isEmpty()) continue;

                for (BlockLocation loc : coords) {
                    if (chunkKey(loc) == key) {
                        result.add(gate);
                        break;
                    }
                }
            }
        }

        return result.isEmpty() ? null : result;
    }

    private static boolean isAir(org.bukkit.Material material) {
        try {
            Method m = org.bukkit.Material.class.getMethod("isAir");
            return (boolean) m.invoke(material);
        } catch (Exception e) {
            return material == org.bukkit.Material.AIR
                    || material == org.bukkit.Material.CAVE_AIR
                    || material == org.bukkit.Material.VOID_AIR;
        }
    }

    private static String inferPortalWorldName(Gate gate) {
        if (gate == null) return null;
        if (gate.portalWorldName != null) return gate.portalWorldName;

        Set<BlockLocation> sample = gate.getFrameCoords();
        if (sample == null || sample.isEmpty()) {
            sample = gate.getPortalCoords();
        }
        if (sample == null || sample.isEmpty()) {
            return gate.exit != null ? gate.exit.getWorld() : null;
        }

        List<BlockLocation> locs = new ArrayList<>(sample);
        int limit = Math.min(25, locs.size());

        String bestWorld = null;
        int bestHits = 0;

        for (org.bukkit.World world : Bukkit.getWorlds()) {
            int hits = 0;
            for (int i = 0; i < limit; i++) {
                BlockLocation loc = locs.get(i);
                org.bukkit.Material type = world.getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()).getType();
                if (!isAir(type)) hits++;
            }
            if (hits > bestHits) {
                bestHits = hits;
                bestWorld = world.getName();
            }
        }

        if (bestWorld != null && bestHits > 0) {
            return bestWorld;
        }

        return gate.exit != null ? gate.exit.getWorld() : null;
    }

    private static void addToChunkIndex(Gate gate) {
        Set<BlockLocation> coords = gate.getPortalCoords();
        if (coords == null || coords.isEmpty()) return;

        String worldName = gate.getPortalWorldName();
        if (worldName == null) return;
        Map<Long, Set<Gate>> byKey = gatesByChunk.computeIfAbsent(worldName, w -> new HashMap<>());

        Set<Long> keys = new HashSet<>();
        for (BlockLocation loc : coords) {
            keys.add(chunkKey(loc));
        }

        for (Long key : keys) {
            byKey.computeIfAbsent(key, k -> new HashSet<>()).add(gate);
        }
    }

    private static void removeFromChunkIndex(Gate gate) {
        Set<BlockLocation> coords = gate.getPortalCoords();
        if (coords == null || coords.isEmpty()) return;

        String worldName = gate.getPortalWorldName();
        if (worldName == null) return;
        Map<Long, Set<Gate>> byKey = gatesByChunk.get(worldName);
        if (byKey == null) return;

        Set<Long> keys = new HashSet<>();
        for (BlockLocation loc : coords) {
            keys.add(chunkKey(loc));
        }

        for (Long key : keys) {
            Set<Gate> set = byKey.get(key);
            if (set == null) continue;
            set.remove(gate);
            if (set.isEmpty()) {
                byKey.remove(key);
            }
        }

        if (byKey.isEmpty()) {
            gatesByChunk.remove(worldName);
        }
    }

    /* ==============================
       Registro y eliminación
       ============================== */
    public static synchronized void register(UUID owner, Gate gate, boolean save) {
        gatesByPlayer
                .computeIfAbsent(owner, u -> new HashSet<>())
                .add(gate);

        String world = gate.getPortalWorldName();
        if (world == null) return;

        gate.getFrameCoords().forEach(loc ->
                Frames.add(world, loc, gate)
        );

        gate.getPortalCoords().forEach(loc ->
                Portals.put(world, loc, gate)
        );

        addToChunkIndex(gate);

        if (save) {
            savePlayerGates(owner, new File("plugins/SimpleGate/gates"));
        }
    }

    public static synchronized void remove(UUID owner, Gate gate) {
        Set<Gate> gates = gatesByPlayer.get(owner);
        if (gates == null || !gates.remove(gate)) return;

        if (gates.isEmpty()) {
            gatesByPlayer.remove(owner);
        }

        String world = gate.getPortalWorldName();
        if (world == null) return;

        gate.getFrameCoords().forEach(loc ->
                Frames.remove(world, loc, gate)
        );

        gate.getPortalCoords().forEach(loc ->
                Portals.remove(world, loc)
        );

        removeFromChunkIndex(gate);

        savePlayerGates(owner, new File("plugins/SimpleGate/gates"));
    }

    /* ==============================
       Consultas
       ============================== */

    private static List<Gate> getGates(UUID owner) {
        Set<Gate> gates = gatesByPlayer.get(owner);
        return gates != null ? new ArrayList<>(gates) : Collections.emptyList();
    }

    public static List<Gate> getByNetworkId(UUID owner, String networkId) {
        return getGates(owner).stream()
                .filter(g -> g.networkId.equals(networkId))
                .distinct()
                .sorted(Comparator.comparingLong(g -> g.creationTimeMillis))
                .collect(Collectors.toList());
    }

    public static Set<Gate> getGatesNear(Location location) {
        Chunk chunk = location.getChunk();
        return getGatesInChunk(chunk);
    }

    public static Collection<Gate> getAllGates() {
        Set<Gate> all = new HashSet<>();
        for (Set<Gate> set : gatesByPlayer.values()) {
            all.addAll(set);
        }
        return all;
    }

    public static List<Gate> getGatesByChunk(Chunk chunk) {
        Set<Gate> gates = getGatesInChunk(chunk);
        return gates != null ? new ArrayList<>(gates) : Collections.emptyList();
    }
    /* ==============================
       Carga y guardado
       ============================== */
    public static synchronized void loadAll(File folder) {
        gatesByPlayer.clear();
        gatesByChunk.clear();
        Frames.map.clear();
        Portals.map.clear();
        if (!folder.exists()) return;

        File[] files = folder.listFiles(f -> f.getName().endsWith(".json"));
        if (files == null) return;

        for (File file : files) {
            try {
                UUID uuid = UUID.fromString(file.getName().replace(".json", ""));
                GateListDTO dto = JsonFileUtil.read(file, GateListDTO.class);

                Set<Gate> gates = new HashSet<>();
                dto.getGates().forEach(g ->
                        gates.add(GateMapper.toGate(g))
                );

                gatesByPlayer.put(uuid, gates);

                for (Gate gate : gates) {
                    if (gate.portalWorldName == null) {
                        gate.portalWorldName = inferPortalWorldName(gate);
                    }

                    if (gate.getPortalType() == null && gate.portalTypeId != null) {
                        ConfigManager config = SimpleGate.getInstance() != null ? SimpleGate.getInstance().getConfigManager() : null;
                        if (config != null) {
                            PortalType type = config.getPortalTypes().get(gate.portalTypeId);
                            if (type != null) {
                                gate.setPortalType(type);
                            }
                        }
                    }

                    String world = gate.getPortalWorldName();
                    if (world == null) continue;

                    gate.getFrameCoords().forEach(loc ->
                            Frames.add(world, loc, gate)
                    );
                    gate.getPortalCoords().forEach(loc ->
                            Portals.put(world, loc, gate)
                    );

                    addToChunkIndex(gate);

                    if (gate.isFakeEndGateway()) {
                        SimpleGate plugin = SimpleGate.getInstance();
                        if (plugin != null) {
                            GatewayManager gm = plugin.getGatewayManager();
                            org.bukkit.World bukkitWorld = Bukkit.getWorld(world);
                            if (bukkitWorld != null) {
                                for (BlockLocation loc : gate.getPortalCoords()) {
                                    gm.addFakeGateway(new org.bukkit.Location(
                                            bukkitWorld,
                                            loc.getBlockX(),
                                            loc.getBlockY(),
                                            loc.getBlockZ()
                                    ));
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Bukkit.getLogger().severe("Error loading gates from file "
                        + file.getName() + ": " + e.getMessage());
            }
        }
    }

    public static synchronized void saveAll(File folder) {
        if (!folder.exists()) folder.mkdirs();
        for (UUID owner : gatesByPlayer.keySet()) {
            savePlayerGates(owner, folder);
        }
    }

    private static void savePlayerGates(UUID owner, File folder) {
        try {
            if (!folder.exists()) folder.mkdirs();

            File file = new File(folder, owner + ".json");
            File tmp  = new File(folder, owner + ".json.tmp");

            List<GateDTO> gates = gatesByPlayer
                    .getOrDefault(owner, Collections.emptySet())
                    .stream()
                    .map(GateMapper::toDTO)
                    .collect(Collectors.toList());

            JsonFileUtil.write(tmp, new GateListDTO(gates));

            java.nio.file.Files.move(
                    tmp.toPath(),
                    file.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                    java.nio.file.StandardCopyOption.ATOMIC_MOVE
            );

        } catch (Exception e) {
            Bukkit.getLogger().severe("Error saving gates for player "
                    + owner + ": " + e.getMessage());
        }
    }

    public static Set<Gate> getGatesInChunk(Chunk chunk) {
        String worldName = chunk.getWorld().getName();
        long key = chunkKey(chunk);

        Map<Long, Set<Gate>> byKey = gatesByChunk.get(worldName);
        if (byKey != null) {
            Set<Gate> gates = byKey.get(key);
            if (gates != null && !gates.isEmpty()) return gates;
        }

        Set<Gate> scanned = scanGatesInChunk(worldName, chunk.getX(), chunk.getZ());
        if (scanned == null) {
            return Collections.emptySet();
        }

        gatesByChunk
                .computeIfAbsent(worldName, w -> new HashMap<>())
                .put(key, scanned);

        return scanned;
    }

    public static void startParticlesForLoadedChunks() {
        Bukkit.getWorlds().forEach(world -> {
            for (Chunk chunk : world.getLoadedChunks()) {
                Set<Gate> gates = getGatesInChunk(chunk);
                if (gates == null) continue;
                for (Gate gate : gates) {
                    if (gate.isIntact()) {
                        gate.startPortalParticles();
                    }
                }
            }
        });
    }

    public static void stopAllParticles() {
        gatesByPlayer.values().forEach(set -> {
            for (Gate gate : set) {
                gate.stopPortalParticles();
            }
        });
    }
}