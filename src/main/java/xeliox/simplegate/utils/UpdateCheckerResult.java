package xeliox.simplegate.utils;

public class UpdateCheckerResult {

    public enum UpdateStatus {
        UP_TO_DATE,       // Local == Spigot
        UPDATE_AVAILABLE, // Local < Spigot
        DEV_BUILD,        // Local > Spigot (Versión de desarrollo/prueba)
        ERROR             // Error de conexión
    }

    private final UpdateStatus status;
    private final String latestVersion;

    private UpdateCheckerResult(UpdateStatus status, String latestVersion) {
        this.status = status;
        this.latestVersion = latestVersion;
    }

    public static UpdateCheckerResult check(String currentVersion, String remoteVersion) {
        if (remoteVersion == null) return new UpdateCheckerResult(UpdateStatus.ERROR, null);

        int comparison = compareVersions(currentVersion, remoteVersion);

        if (comparison == 0) {
            return new UpdateCheckerResult(UpdateStatus.UP_TO_DATE, remoteVersion);
        } else if (comparison < 0) {
            return new UpdateCheckerResult(UpdateStatus.UPDATE_AVAILABLE, remoteVersion);
        } else {
            return new UpdateCheckerResult(UpdateStatus.DEV_BUILD, remoteVersion);
        }
    }

    /**
     * Compara dos strings de versión (ej. "1.2.1" y "1.2.0")
     * @return 0 si son iguales, negativo si v1 < v2, positivo si v1 > v2
     */
    private static int compareVersions(String v1, String v2) {
        String[] vals1 = v1.replaceAll("[^0-9.]", "").split("\\.");
        String[] vals2 = v2.replaceAll("[^0-9.]", "").split("\\.");

        int i = 0;
        while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i])) {
            i++;
        }

        if (i < vals1.length && i < vals2.length) {
            int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
            return Integer.signum(diff);
        }

        return Integer.signum(vals1.length - vals2.length);
    }

    public static UpdateCheckerResult error() {
        return new UpdateCheckerResult(UpdateStatus.ERROR, null);
    }

    public UpdateStatus getStatus() {
        return status;
    }

    public String getLatestVersion() {
        return latestVersion;
    }
}