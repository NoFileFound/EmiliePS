package org.emilieps.utils;

import java.util.stream.Stream;

public final class DispatchUtils {
    private static final String[] SUPPORTED_VERSIONS = {
            "CNRELWin", "CNRELiOS", "CNRELAndroid", "CNRELPS4", "CNCBPS4",
            "CNRELPS5", "CNCBPS5", "CNGMWin", "CNGMiOS", "CNGMAndroid",
            "CNGMPS4", "CNGMPS5", "CNPREWin", "CNPREiOS", "CNPREAndroid",
            "CNPREPS4", "CNPREPS5", "CNINWin", "CNINiOS", "CNINAndroid",
            "OSRELWin", "OSRELiOS", "OSRELAndroid", "OSRELPS4SIEE", "OSRELPS4SIEA",
            "OSCBPS4", "OSCBPS4SIEE", "OSCBPS4SIEA", "OSRELPS5SIEE", "OSRELPS5SIEA",
            "OSCBPS5", "OSCBPS5SIEE", "OSCBPS5SIEA", "OSGMWin", "OSGMiOS",
            "OSGMAndroid", "OSGMPS4", "OSPREWin", "OSPREiOS", "OSPREAndroid",
            "OSPREPS4", "CNCBWin", "CNCBiOS", "CNCBAndroid", "OSCBWin",
            "OSCBiOS", "OSCBAndroid"
    };

    public static boolean isValidGameVersion(String gameVersion) {
        return Stream.of(SUPPORTED_VERSIONS).anyMatch(gameVersion::startsWith);
    }
}