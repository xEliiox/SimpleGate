package xeliox.simplegate;

import api.xeliox.colorapi.ColorAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import xeliox.simplegate.config.ConfigManager;
import xeliox.simplegate.config.Messages;
import xeliox.simplegate.gate.GateManager;
import xeliox.simplegate.gate.GatewayManager;
import xeliox.simplegate.listeners.GatePreventEventsListener;
import xeliox.simplegate.listeners.GateListener;
import xeliox.simplegate.listeners.PortalSelectorListener;
import xeliox.simplegate.utils.UpdateChecker;
import xeliox.simplegate.utils.VersionUtils;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;


public final class SimpleGate extends JavaPlugin {

    private static SimpleGate instance;
    private ConfigManager configManager;
    private final ConsoleCommandSender console = Bukkit.getConsoleSender();
    private final PluginDescriptionFile pdfFile = getDescription();
    private final String version = pdfFile.getVersion();
    public static VersionUtils versionUtils;
    private PortalSelectorListener portalSelectorListener;
    private GatewayManager gatewayManager;
    private final String author = getDescription().getAuthors().isEmpty() ? "Unknown" : getDescription().getAuthors().get(0);


    @Override
    public void onEnable() {
        setVersion();
        instance = this;
        Logger logger = getLogger();
        try {
            configManager = new ConfigManager(this,logger);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        gatewayManager = new GatewayManager();
        GateManager.loadAll(new File("plugins/SimpleGate/gates"));
        console.sendMessage(ColorAPI.translate(Messages.PREFIX.getMessage() + "&aHas been enabled! &fVersion: " + version));
        console.sendMessage(ColorAPI.translate(Messages.PREFIX.getMessage() + "&fPlugin Creator &c" + author));
        console.sendMessage(ColorAPI.translate(Messages.PREFIX.getMessage() + "&fDiscord Contact: &e.zlex_"));
        console.sendMessage(ColorAPI.translate(Messages.PREFIX.getMessage() + "&fBased on &bCreativeGatez &fby &cmarcotama &fand &bCreativeGates &fby &cMassiveCraft"));

        this.portalSelectorListener = new PortalSelectorListener(this);
        GatePreventEventsListener gatePreventEventsListener = new GatePreventEventsListener();

        registerEvents(new GateListener());
        registerEvents(portalSelectorListener);
        registerEvents(new xeliox.simplegate.listeners.ChunkListener());
        registerEvents(gatePreventEventsListener);
        PluginCommand command = this.getCommand("simplegate");
        if (command != null) {
            command.setExecutor(new xeliox.simplegate.commands.MainCommand(this));
        }

        Bukkit.getScheduler().runTask(this, GateManager::startParticlesForLoadedChunks);
        UpdateChecker.checkForUpdates(this);
    }

    @Override
    public void onDisable() {
        console.sendMessage(ColorAPI.translate(Messages.PREFIX.getMessage() + "&cHas been disabled! &fVersion: " + version));
        console.sendMessage(ColorAPI.translate(Messages.PREFIX.getMessage() + "&fPlugin Creator &c" + author));
        console.sendMessage(ColorAPI.translate(Messages.PREFIX.getMessage() + "&fDiscord Contact: &e.zlex_"));
        console.sendMessage(ColorAPI.translate(Messages.PREFIX.getMessage() + "&fBased on &bCreativeGatez &fby &cmarcotama &fand &bCreativeGates &fby &cMassiveCraft"));
        GateManager.stopAllParticles();
        GateManager.saveAll(new File("plugins/SimpleGate/gates"));
    }

    public void setVersion(){
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        String bukkitVersion = Bukkit.getServer().getBukkitVersion().split("-")[0];
        switch(bukkitVersion) {
            case "1.20.5":
            case "1.20.6":
                versionUtils = VersionUtils.v1_20_R4;
                break;
            case "1.21":
            case "1.21.1":
                versionUtils = VersionUtils.v1_21_R1;
                break;
            case "1.21.2":
            case "1.21.3":
                versionUtils = VersionUtils.v1_21_R2;
                break;
            case "1.21.4":
                versionUtils = VersionUtils.v1_21_R3;
                break;
            case "1.21.5":
                versionUtils = VersionUtils.v1_21_R4;
                break;
            case "1.21.6":
            case "1.21.7":
            case "1.21.8":
                versionUtils = VersionUtils.v1_21_R5;
                break;
            case "1.21.9":
            case "1.21.10":
                versionUtils = VersionUtils.v1_21_R6;
                break;
            case "1.21.11":
                versionUtils = VersionUtils.v1_21_R7;
                break;
            default:
                try {
                    versionUtils = VersionUtils.valueOf(packageName.replace("org.bukkit.craftbukkit.", ""));
                } catch (IllegalArgumentException e) {
                    Bukkit.getLogger().warning("Unrecognized version: " + bukkitVersion);
                    versionUtils = null;
                    Bukkit.getPluginManager().disablePlugin(this);
                }
        }
    }

    private void registerEvents(Listener Listener) {
        Bukkit.getPluginManager().registerEvents(Listener, this);
    }

    public static SimpleGate getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public GatewayManager getGatewayManager() {
        return gatewayManager;
    }

    public PortalSelectorListener getPortalSelectorListener() {
        return portalSelectorListener;
    }
}
