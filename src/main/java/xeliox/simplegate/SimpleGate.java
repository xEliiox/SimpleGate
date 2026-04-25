package xeliox.simplegate;

import io.github.xeliiox.colorapi.ColorAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import xeliox.simplegate.config.core.Config;
import xeliox.simplegate.config.core.MessagesConfig;
import xeliox.simplegate.listeners.*;
import xeliox.simplegate.managers.ConfigManager;
import xeliox.simplegate.config.Messages;
import xeliox.simplegate.managers.GateManager;
import xeliox.simplegate.managers.GatewayManager;
import xeliox.simplegate.managers.UpdateCheckerManager;
import xeliox.simplegate.utils.UpdateCheckerResult;
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
    private EntityGateListener entityGateListener;
    private VehicleGateListener vehicleGateListener;
    private final String author = getDescription().getAuthors().isEmpty() ? "Unknown" : getDescription().getAuthors().get(0);

    @Override
    public void onEnable() {
        setVersion();
        instance = this;
        Logger logger = getLogger();
        try {
            Config config = new Config(this, logger);
            MessagesConfig messagesConfig = new MessagesConfig(this, logger);
            configManager = new ConfigManager(config, messagesConfig);
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
        this.entityGateListener = new EntityGateListener(this);
        this.vehicleGateListener = new VehicleGateListener(this);
        registerEvents(entityGateListener);
        registerEvents(vehicleGateListener);
        entityGateListener.start();
        vehicleGateListener.start();
        registerEvents(new GateListener());
        registerEvents(portalSelectorListener);
        registerEvents(new xeliox.simplegate.listeners.ChunkListener());
        registerEvents(gatePreventEventsListener);
        PluginCommand command = this.getCommand("simplegate");
        if (command != null) {
            command.setExecutor(new xeliox.simplegate.commands.MainCommand(this));
        }

        Bukkit.getScheduler().runTask(this, GateManager::startParticlesForLoadedChunks);
        if (configManager.isUpdateCheckerEnabled()) {
            runUpdateChecker();
        }
    }

    @Override
    public void onDisable() {
        console.sendMessage(ColorAPI.translate(Messages.PREFIX.getMessage() + "&cHas been disabled! &fVersion: " + version));
        console.sendMessage(ColorAPI.translate(Messages.PREFIX.getMessage() + "&fPlugin Creator &c" + author));
        console.sendMessage(ColorAPI.translate(Messages.PREFIX.getMessage() + "&fDiscord Contact: &e.zlex_"));
        console.sendMessage(ColorAPI.translate(Messages.PREFIX.getMessage() + "&fBased on &bCreativeGatez &fby &cmarcotama &fand &bCreativeGates &fby &cMassiveCraft"));
        GateManager.stopAllParticles();
        GateManager.saveAll(new File("plugins/SimpleGate/gates"));
        entityGateListener.stop();
        vehicleGateListener.stop();
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
            case "26.1":
            case "26.1.1":
            case "26.1.2":
                versionUtils = VersionUtils.v26_1_R1;
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

    private void runUpdateChecker() {
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            UpdateCheckerManager checker = new UpdateCheckerManager(version);
            UpdateCheckerResult result = checker.check();

            Bukkit.getScheduler().runTask(this, () -> {
                String prefix = Messages.PREFIX.getMessage();

                switch (result.getStatus()) {
                    case UPDATE_AVAILABLE:
                        console.sendMessage(ColorAPI.translate(
                                prefix + "&6¡New version available! &fCurrent: &c" + version +
                                        " &f→ &aLatest: " + result.getLatestVersion()
                        ));
                        console.sendMessage(ColorAPI.translate(
                                prefix + "&fDownload: &bhttps://www.spigotmc.org/resources/simplegate.133505/"
                        ));
                        break;

                    case DEV_BUILD:
                        console.sendMessage(ColorAPI.translate(
                                prefix + "&d&lDEV BUILD: &fYou are using a version &5superior &fto SpigotMC."
                        ));
                        console.sendMessage(ColorAPI.translate(
                                prefix + "&fVersion: &b" + version + " &7(Latest on Spigot: " + result.getLatestVersion() + ")"
                        ));
                        break;

                    case UP_TO_DATE:
                        console.sendMessage(ColorAPI.translate(
                                prefix + "&aThe plugin is updated. &f(" + version + ")"
                        ));
                        break;

                    case ERROR:
                    default:
                        console.sendMessage(ColorAPI.translate(
                                prefix + "&eUpdate checker: Could not connect to SpigotMC."
                        ));
                        break;
                }
            });
        });
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

    public EntityGateListener getMobPortalListener() { return entityGateListener; }

    public VehicleGateListener getVehiclePortalListener() { return vehicleGateListener; }

    public PortalSelectorListener getPortalSelectorListener() {
        return portalSelectorListener;
    }
}
