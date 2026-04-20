package xeliox.simplegate.config.core;

import org.simpleyaml.configuration.file.YamlFile;
import xeliox.simplegate.SimpleGate;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public class MessagesConfig {

    private final YamlFile yaml;
    private final Logger logger;

    public MessagesConfig(SimpleGate plugin, Logger logger) throws IOException {
        this.logger = logger;

        File dataFolder = plugin.getDataFolder();
        File messagesFile = new File(dataFolder, "messages.yml");

        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            throw new IOException("Failed to create plugin folder!");
        }

        if (!messagesFile.exists() || messagesFile.length() == 0) {
            if (plugin.getResource("messages.yml") != null) {
                plugin.saveResource("messages.yml", false);
            } else {
                messagesFile.createNewFile();
            }
        }

        this.yaml = new YamlFile(messagesFile);
        load();
    }

    public void load() throws IOException {
        try {
            yaml.loadWithComments();
        } catch (Exception e) {
            logger.severe("Failed to load messages.yml: " + e.getMessage());
            throw new IOException("Could not load messages.yml", e);
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


    public String getString(String path, String defaultValue) {
        return yaml.getString(path, defaultValue);
    }
}