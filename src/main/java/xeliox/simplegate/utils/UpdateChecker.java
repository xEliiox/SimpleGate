package xeliox.simplegate.utils;

import api.xeliox.colorapi.ColorAPI;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import xeliox.simplegate.SimpleGate;
import xeliox.simplegate.config.Messages;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class UpdateChecker {

    private static final String RESOURCE_URL = "https://api.spigotmc.org/legacy/update.php?resource=";
    private static final int RESOURCE_ID = 133505;

    public static void checkForUpdates(SimpleGate plugin) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(RESOURCE_URL + RESOURCE_ID);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
                    String latestVersion = reader.readLine();
                    reader.close();

                    if (latestVersion == null) return;

                    String currentVersion = plugin.getDescription().getVersion();
                    int comparison = compareVersions(currentVersion, latestVersion);

                    if (comparison < 0) {
                        Bukkit.getConsoleSender().sendMessage(ColorAPI.translate(
                                Messages.PREFIX.getMessage() + "&cOutdated version detected! Latest: &fv" + latestVersion));
                        Bukkit.getConsoleSender().sendMessage(ColorAPI.translate(
                                Messages.PREFIX.getMessage() + "&cCurrent version: &fv" + currentVersion));
                        Bukkit.getConsoleSender().sendMessage(ColorAPI.translate(
                                Messages.PREFIX.getMessage() + "&fDownload: &7https://www.spigotmc.org/resources/simplegate.133505/"));
                    } else if (comparison > 0) {
                        Bukkit.getConsoleSender().sendMessage(ColorAPI.translate(
                                Messages.PREFIX.getMessage() + "&eRunning a development version: &fv" + currentVersion));
                    } else {
                        Bukkit.getConsoleSender().sendMessage(ColorAPI.translate(
                                Messages.PREFIX.getMessage() + "&aRunning the latest version: &fv" + currentVersion));
                    }

                } catch (Exception e) {
                    Bukkit.getConsoleSender().sendMessage(ColorAPI.translate(
                            Messages.PREFIX.getMessage() + "&cFailed to check for updates: &f" + e.getMessage()));
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    private static int compareVersions(@NotNull String version1, @NotNull String version2) {
        String[] v1Parts = version1.replaceAll("[^0-9.]", "").split("\\.");
        String[] v2Parts = version2.replaceAll("[^0-9.]", "").split("\\.");
        int maxLength = Math.max(v1Parts.length, v2Parts.length);
        for (int i = 0; i < maxLength; i++) {
            int num1 = (i < v1Parts.length) ? Integer.parseInt(v1Parts[i]) : 0;
            int num2 = (i < v2Parts.length) ? Integer.parseInt(v2Parts[i]) : 0;
            if (num1 != num2) {
                return Integer.compare(num1, num2);
            }
        }
        return 0;
    }
}
