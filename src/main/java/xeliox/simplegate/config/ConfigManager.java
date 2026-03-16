package xeliox.simplegate.config;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.file.YamlFile;
import xeliox.simplegate.SimpleGate;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class ConfigManager {

    protected YamlFile config;

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
    private int inventorySlots;
    private boolean fillEmptySlots;
    private ConfigurationSection section;

    private Map<Integer, PortalType> portalTypes;

    public ConfigManager(@NotNull SimpleGate plugin, Logger logger) throws IOException {
        File dataFolder = plugin.getDataFolder();
        File configFile = new File(dataFolder, "config.yml");

        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            logger.severe("Failed to create plugin folder!");
            return;
        }

        if (!configFile.exists() || configFile.length() == 0) {
            plugin.saveResource("config.yml", false);
        }

        config = new YamlFile(configFile);

        try {
            config.loadWithComments();
        } catch (Exception e) {
            logger.severe("Failed to load config.yml: " + e.getMessage());
        }

        loadConfiguration(config);
    }

    public void loadConfiguration(YamlFile config) throws IOException {
        boolean[] saveRequired = new boolean[]{false};

        portalTypes = new LinkedHashMap<>();

        section = config.getConfigurationSection("Settings.GUISettings.PortalsTypeItem");
        if (section == null) {
            section = config.createSection("Settings.GUISettings.PortalsTypeItem");
            saveRequired[0] = true;
        }

        inventoryType = getOrSetDefault("Settings.GUISettings.InventoryType", "HOPPER", saveRequired);
        inventorySlots = getIntOrDefault("Settings.GUISettings.Slots", 9, saveRequired);
        inventoryTitle = getOrSetDefault("Settings.GUISettings.Title", "&eSelect Portal Type", saveRequired);
        fillEmptySlots = getOrSetDefault("Settings.GUISettings.FillEmptySlots", true, saveRequired);

        for (String key : section.getKeys(false)) {
            ConfigurationSection s = section.getConfigurationSection(key);

            Material icon = Material.matchMaterial(s.getString("Material"));
            Material content = Material.matchMaterial(s.getString("MaterialContent"));

            if (icon == null || content == null) {
                Bukkit.getLogger().warning("Invalid material in GUI portal type: " + key);
                continue;
            }

            int slot = s.getInt("Slot");
            String name = s.getString("Name");
            List<String> lore = s.getStringList("Lore");
            String particle;
            String particleColor = "WHITE";

            if (s.isConfigurationSection("ParticleEffect")) {
                particle = s.getString("ParticleEffect.Type");
                particleColor = s.getString("ParticleEffect.Color", "WHITE");
            } else {
                particle = s.getString("ParticleEffect");
            }


            portalTypes.put(
                    Integer.parseInt(key),
                    new PortalType(icon, content, slot, name, lore, particle, particleColor)
            );
        }

        disabledWorlds = getOrSetDefault("Settings.DisabledWorlds", new ArrayList<>(), saveRequired);
        soundTeleportEnabled = getOrSetDefault("Settings.SoundTeleportEnabled", true, saveRequired);
        maxPortalSize = getIntOrDefault("Settings.MaxPortalSize", 200, saveRequired);
        pigmanPortalSpawnEnabled = getOrSetDefault("Settings.PigmanPortalSpawnEnabled", true, saveRequired);

        itemRequisites = Material.getMaterial(
                getOrSetDefault("Settings.ItemRequiredToCreatePortal", "COMPASS", saveRequired).toUpperCase()
        );

        if (itemRequisites == null) {
            itemRequisites = Material.COMPASS;
            config.set("Settings.ItemRequiredToCreatePortal", "COMPASS");
            saveRequired[0] = true;
        }

        isRemovingCreateToolName = getOrSetDefault("Settings.RemovingCreateToolName", true, saveRequired);
        isRemovingCreateToolItem = getOrSetDefault("Settings.RemovingCreateToolItem", true, saveRequired);

        blockRequired = Material.getMaterial(
                getOrSetDefault(
                        "Settings.BlockRequiredToCreatePortal.Material",
                        "EMERALD_BLOCK",
                        saveRequired
                ).toUpperCase()
        );

        if (blockRequired == null) {
            blockRequired = Material.EMERALD_BLOCK;
            config.set("Settings.BlockRequiredToCreatePortal.Material", "EMERALD_BLOCK");
            saveRequired[0] = true;
        }

        blockAmount = getOrSetDefault(
                "Settings.BlockRequiredToCreatePortal.Amount",
                2,
                saveRequired
        );

        for (Messages message : Messages.values()) {
            if (!config.contains(message.path)) {
                config.set(message.path, message.defaultMessage);
                saveRequired[0] = true;
            }
            message.loadMessage(config);
        }

        if (saveRequired[0]) {
            config.save();
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T getOrSetDefault(String path, T defaultValue, boolean[] saveFlag) {
        if (!config.contains(path)) {
            config.set(path, defaultValue);
            saveFlag[0] = true;
            return defaultValue;
        }
        return (T) config.get(path);
    }

    private int getIntOrDefault(String path, int defaultValue, boolean[] saveFlag) {
        if (!config.contains(path)) {
            config.set(path, defaultValue);
            saveFlag[0] = true;
            return defaultValue;
        }

        Object value = config.get(path);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }

        return defaultValue;
    }


    public String getInventoryTitle() {
        return inventoryTitle;
    }

    public String getInventoryType() {
        return inventoryType;
    }

    public int getInventorySlots() {
        return inventorySlots;
    }

    public boolean isFillEmptySlots() {
        return fillEmptySlots;
    }

    public Map<Integer, PortalType> getPortalTypes() {
        return portalTypes;
    }

    public List<String> getDisabledWorlds() {
        return disabledWorlds;
    }

    public Map<Material, Long> getBlockRequiredToCreatePortal() {
        long blockLongAmount = blockAmount.longValue();
        return Collections.singletonMap(blockRequired, blockLongAmount);
    }

    public Material getItemRequisites() {
        return itemRequisites;
    }

    public boolean isRemovingCreateToolName() {
        return isRemovingCreateToolName;
    }

    public boolean isRemovingCreateToolItem() {
        return isRemovingCreateToolItem;
    }

    public boolean isPigmanPortalSpawnEnabled() {
        return pigmanPortalSpawnEnabled;
    }

    public boolean isSoundTeleportEnabled() {
        return soundTeleportEnabled;
    }

    public int getMaxPortalSize() {
        return maxPortalSize;
    }

    public ConfigurationSection getPortalTypeSection() {
        return section;
    }

    public void reloadConfig() throws IOException {
        try {
            if (!config.exists()) {
                config.createNewFile();
            }

            this.config.loadWithComments();
            loadConfiguration(this.config);
        } catch (Exception e) {
            Bukkit.getLogger().severe("Error reloading configuration: " + e.getMessage());
            throw new IOException("Could not reload configuration", e);
        }
    }
}
