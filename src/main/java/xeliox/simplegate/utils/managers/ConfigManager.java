package xeliox.simplegate.managers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.simpleyaml.configuration.ConfigurationSection;
import xeliox.simplegate.config.core.Config;
import xeliox.simplegate.config.Messages;
import xeliox.simplegate.config.core.MessagesConfig;
import xeliox.simplegate.config.PortalType;

import java.io.IOException;
import java.util.*;

public class ConfigManager {

    private final Config config;
    private final MessagesConfig messagesConfig;

    private List<String> disabledWorlds;
    private boolean soundTeleportEnabled;
    private int maxPortalSize;
    private boolean pigmanPortalSpawnEnabled;

    private Material blockRequired;
    private Number blockAmount;
    private Material itemRequisites;

    private boolean isRemovingCreateToolName;
    private boolean isRemovingCreateToolItem;

    private String inventoryTitle;
    private String inventoryType;
    private String portalSound;
    private String portalNoDestinationSound;
    private int inventorySlots;
    private boolean fillEmptySlots;
    private boolean updateCheckerEnabled;
    private boolean mobTeleportEnabled;
    private boolean vehicleTeleportEnabled;
    private boolean leashedMobTeleportEnabled;
    private boolean tamedMobTeleportEnabled;
    private int gateLimitPerNetwork;
    private ConfigurationSection section;

    private Map<Integer, PortalType> portalTypes;

    public ConfigManager(Config config, MessagesConfig messagesConfig) throws IOException {
        this.config = config;
        this.messagesConfig = messagesConfig;
        load();
    }

    public void load() throws IOException {
        boolean[] saveRequired = {false};
        boolean[] msgSaveRequired = {false};

        portalTypes = new LinkedHashMap<>();

        // ── GUI ───────────────────────────────────────────────────────────────
        inventoryType  = config.getOrSetDefault("Settings.GUISettings.InventoryType", "HOPPER", saveRequired);
        inventorySlots = config.getIntOrDefault("Settings.GUISettings.Slots", 9, saveRequired);
        inventoryTitle = config.getOrSetDefault("Settings.GUISettings.Title", "&eSelect Portal Type", saveRequired);
        fillEmptySlots = config.getOrSetDefault("Settings.GUISettings.FillEmptySlots", true, saveRequired);

       section = config.getSection("Settings.GUISettings.PortalsTypeItem");
        if (section == null) {
            section = config.createSection("Settings.GUISettings.PortalsTypeItem");
            saveRequired[0] = true;
        }

        for (String key : section.getKeys(false)) {
            ConfigurationSection s = section.getConfigurationSection(key);

            Material icon    = Material.matchMaterial(s.getString("Material"));
            Material content = Material.matchMaterial(s.getString("MaterialContent"));

            if (icon == null || content == null) {
                Bukkit.getLogger().warning("Invalid material in GUI portal type: " + key);
                continue;
            }

            int slot          = s.getInt("Slot");
            String name       = s.getString("Name");
            List<String> lore = s.getStringList("Lore");
            String particle;
            String particleColor = "WHITE";

            if (s.isConfigurationSection("ParticleEffect")) {
                particle      = s.getString("ParticleEffect.Type");
                particleColor = s.getString("ParticleEffect.Color", "WHITE");
            } else {
                particle = s.getString("ParticleEffect");
            }

            portalTypes.put(
                    Integer.parseInt(key),
                    new PortalType(icon, content, slot, name, lore, particle, particleColor)
            );
        }

        // ── Settings generales ────────────────────────────────────────────────
        disabledWorlds           = config.getOrSetDefault("Settings.DisabledWorlds", new ArrayList<>(), saveRequired);
        soundTeleportEnabled     = config.getOrSetDefault("Settings.SoundTeleportEnabled", true, saveRequired);
        maxPortalSize            = config.getIntOrDefault("Settings.MaxPortalSize", 200, saveRequired);
        pigmanPortalSpawnEnabled = config.getOrSetDefault("Settings.PigmanPortalSpawnEnabled", true, saveRequired);
        isRemovingCreateToolName = config.getOrSetDefault("Settings.RemovingCreateToolName", true, saveRequired);
        isRemovingCreateToolItem = config.getOrSetDefault("Settings.RemovingCreateToolItem", true, saveRequired);
        updateCheckerEnabled = config.getOrSetDefault("Settings.UpdateChecker", true, saveRequired);
        mobTeleportEnabled = config.getOrSetDefault("Settings.MobTeleportation", true, saveRequired);
        vehicleTeleportEnabled = config.getOrSetDefault("Settings.VehicleTeleportation", true, saveRequired);
        leashedMobTeleportEnabled = config.getOrSetDefault("Settings.LeashedMobTeleportation", true, saveRequired);
        tamedMobTeleportEnabled = config.getOrSetDefault("Settings.TamedMobTeleportation", false, saveRequired);
        portalSound = config.getOrSetDefault("Settings.PortalSound", "ENTITY_GHAST_SHOOT", saveRequired);
        portalNoDestinationSound = config.getOrSetDefault("Settings.PortalNoDestinationSound", "ENTITY_BAT_TAKEOFF", saveRequired);
        gateLimitPerNetwork = config.getIntOrDefault("Settings.GateLimitPerNetwork", 2, saveRequired);

        if (gateLimitPerNetwork > 10 || gateLimitPerNetwork < 2) {
            gateLimitPerNetwork = 2;
            Bukkit.getLogger().warning("GateLimitPerNetwork value '" + gateLimitPerNetwork + "' is invalid (must be 2-10). Resetting to 2.");
            config.set("Settings.GateLimitPerNetwork", 2);
            saveRequired[0] = true;
        }
        // ── Item requisite ────────────────────────────────────────────────────
        String itemStr = config.getOrSetDefault(
                "Settings.ItemRequiredToCreatePortal", "COMPASS", saveRequired
        ).toUpperCase();
        itemRequisites = Material.getMaterial(itemStr);
        if (itemRequisites == null) {
            itemRequisites = Material.COMPASS;
            config.set("Settings.ItemRequiredToCreatePortal", "COMPASS");
            saveRequired[0] = true;
        }

        // ── Block requisite ───────────────────────────────────────────────────
        String blockStr = config.getOrSetDefault(
                "Settings.BlockRequiredToCreatePortal.Material", "EMERALD_BLOCK", saveRequired
        ).toUpperCase();
        blockRequired = Material.getMaterial(blockStr);
        if (blockRequired == null) {
            blockRequired = Material.EMERALD_BLOCK;
            config.set("Settings.BlockRequiredToCreatePortal.Material", "EMERALD_BLOCK");
            saveRequired[0] = true;
        }

        blockAmount = config.getOrSetDefault(
                "Settings.BlockRequiredToCreatePortal.Amount", 2, saveRequired
        );

        // ── Messages (messages.yml) ───────────────────────────────────────────
        for (Messages message : Messages.values()) {
            if (!messagesConfig.contains(message.path)) {
                messagesConfig.set(message.path, message.defaultMessage);
                msgSaveRequired[0] = true;
            }
            message.loadMessage(messagesConfig);
        }

        if (saveRequired[0])    config.save();
        if (msgSaveRequired[0]) messagesConfig.save();
    }

    public void reload() throws IOException {
        config.reload();
        messagesConfig.reload();
        load();
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public ConfigurationSection getPortalTypeSection() {
        return section;
    }
    public String getInventoryTitle()    { return inventoryTitle; }
    public String getInventoryType()     { return inventoryType; }
    public int getInventorySlots()       { return inventorySlots; }
    public boolean isFillEmptySlots()    { return fillEmptySlots; }

    public Map<Integer, PortalType> getPortalTypes() { return portalTypes; }
    public List<String> getDisabledWorlds()          { return disabledWorlds; }

    public Map<Material, Long> getBlockRequiredToCreatePortal() {
        return Collections.singletonMap(blockRequired, blockAmount.longValue());
    }

    public Material getItemRequisites()          { return itemRequisites; }
    public boolean isRemovingCreateToolName()    { return isRemovingCreateToolName; }
    public boolean isRemovingCreateToolItem()    { return isRemovingCreateToolItem; }
    public boolean isPigmanPortalSpawnEnabled()  { return pigmanPortalSpawnEnabled; }
    public boolean isSoundTeleportEnabled()      { return soundTeleportEnabled; }
    public boolean isUpdateCheckerEnabled() { return updateCheckerEnabled; }
    public boolean isMobTeleportationEnabled() { return mobTeleportEnabled; }
    public boolean isVehicleTeleportationEnabled() { return vehicleTeleportEnabled; }
    public boolean isLeashedMobTeleportationEnabled() { return leashedMobTeleportEnabled; }
    public boolean isTamedMobTeleportationEnabled() { return tamedMobTeleportEnabled; }
    public String getPortalSound() { return portalSound; }
    public String getPortalNoDestinationSound() { return portalNoDestinationSound; }
    public int getMaxPortalSize()                { return maxPortalSize; }
    public int getGateLimitPerNetwork() { return gateLimitPerNetwork; }

}