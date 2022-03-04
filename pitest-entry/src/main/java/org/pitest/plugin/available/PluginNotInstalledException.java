package org.pitest.plugin.available;

public class PluginNotInstalledException extends RuntimeException {
    public PluginNotInstalledException(String msg) {
        super(line() + msg + line());
    }

    private static String line() {
        return String.format("%n%n--------------------------------------------------------------------------------%n%n");
    }
}
