package xeliox.simplegate.config.core;

import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.file.YamlFile;
import xeliox.simplegate.SimpleGate;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public class Config {

    private final YamlFile yaml;
    private final Logger logger;

    public Config(SimpleGate plugin, Logger logger) throws IOException {
        this.logger = logger;

        File dataFolder = plugin.getDataFolder();
        File configFile = new File(dataFolder, "config.yml");

        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            throw new IOException("Failed to create plugin folder!");
        }

        if (!configFile.exists() || configFile.length() == 0) {
            plugin.saveResource("config.yml", false);
        }

        this.yaml = new YamlFile(configFile);
        load();
    }

    public void load() throws IOException {
        try {
            yaml.loadWithComments();
        } catch (Exception e) {
            logger.severe("Failed to load config.yml: " + e.getMessage());
            throw new IOException("Could not load config.yml", e);
        }
    }

    public void save() throws IOException {
        yaml.save();
    }

    public void reload() throws IOException {
        if (!yaml.exists()) {
            yaml.createNewFile();
        }
        load();
    }


    public boolean contains(String path) {
        return yaml.contains(path);
    }

    public void set(String path, Object value) {
        yaml.set(path, value);
    }

    public Object get(String path) {
        return yaml.get(path);
    }

    public ConfigurationSection getSection(String path) {
        return yaml.getConfigurationSection(path);
    }

    public ConfigurationSection createSection(String path) {
        return yaml.createSection(path);
    }

    @SuppressWarnings("unchecked")
    public <T> T getOrSetDefault(String path, T defaultValue, boolean[] saveFlag) {
        if (!contains(path)) {
            set(path, defaultValue);
            saveFlag[0] = true;
            return defaultValue;
        }
        return (T) get(path);
    }

    public int getIntOrDefault(String path, int defaultValue, boolean[] saveFlag) {
        if (!contains(path)) {
            set(path, defaultValue);
            saveFlag[0] = true;
            return defaultValue;
        }
        Object value = get(path);
        return (value instanceof Number) ? ((Number) value).intValue() : defaultValue;
    }
}
