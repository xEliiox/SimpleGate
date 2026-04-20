package xeliox.simplegate.managers;

import xeliox.simplegate.utils.UpdateCheckerResult;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateCheckerManager {

    private static final int RESOURCE_ID = 133505;
    private static final String API_URL =
            "https://api.spigotmc.org/legacy/update.php?resource=" + RESOURCE_ID;

    private static final int MAX_VERSION_LENGTH = 10;
    private static final int TIMEOUT_MS = 2000;

    private final String currentVersion;

    public UpdateCheckerManager(String currentVersion) {
        this.currentVersion = currentVersion;
    }

    public UpdateCheckerResult check() {
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(API_URL).openConnection();
            con.setConnectTimeout(TIMEOUT_MS);
            con.setReadTimeout(TIMEOUT_MS);
            con.setRequestMethod("GET");

            String latestVersion;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(con.getInputStream()))) {
                latestVersion = reader.readLine();
            }

            if (latestVersion == null || latestVersion.isEmpty() || latestVersion.length() > MAX_VERSION_LENGTH) {
                return UpdateCheckerResult.error();
            }

            return UpdateCheckerResult.check(currentVersion, latestVersion);

        } catch (Exception ex) {
            return UpdateCheckerResult.error();
        }
    }
}