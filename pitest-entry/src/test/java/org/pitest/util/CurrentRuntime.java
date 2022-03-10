package org.pitest.util;

public class CurrentRuntime {

    public static int version() {
        String version = System.getProperty("java.version").replace("-ea", "");
        if (version.startsWith("1.")) {
            version = version.substring(2, 3);
        } else {
            int dot = version.indexOf(".");
            if (dot != -1) {
                version = version.substring(0, dot);
            }
        }
        return Integer.parseInt(version);
    }
}
