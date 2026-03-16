package xeliox.simplegate.utils;

import xeliox.simplegate.SimpleGate;

public class VersionCheck {


    public static boolean serverIsNew() {
        return SimpleGate.versionUtils.isGreaterOrEqual(VersionUtils.v1_16_R1);
    }

    public static boolean serverIsLegacy() {
        return !SimpleGate.versionUtils.isGreaterOrEqual(VersionUtils.v1_13_R1);
    }
}
