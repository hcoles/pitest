package org.pitest.util;

import java.util.logging.Level;

public enum Verbosity {
    SILENT(true, false, Level.OFF),
    QUIET(false, false, Level.SEVERE),
    VERBOSE(false, true, Level.FINEST),
    DEFAULT(false, true, Level.INFO);

    private final boolean disableInMinions;
    private final boolean showSpinner;
    private final Level level;

    Verbosity(boolean disableInMinions, boolean showSpinner, Level level) {
        this.disableInMinions = disableInMinions;
        this.showSpinner = showSpinner;
        this.level = level;
    }

    public boolean disableInMinions() {
        return disableInMinions;
    }

    public boolean showSpinner() {
        return showSpinner;
    }

    public Level level() {
        return level;
    }
}
