package org.pitest.util;

import java.util.logging.Level;

public enum Verbosity {
    QUIET(MinionLogging.DONT_SHOW, false, Level.SEVERE),
    QUIET_WITH_PROGRESS(MinionLogging.DONT_SHOW, true, Level.SEVERE),
    DEFAULT(MinionLogging.DONT_SHOW, true, Level.INFO),
    NO_SPINNER(MinionLogging.DONT_SHOW, false, Level.INFO),
    VERBOSE_NO_SPINNER(MinionLogging.SHOW, false, Level.FINEST),
    VERBOSE(MinionLogging.SHOW, true, Level.FINEST);

    private final MinionLogging minion;
    private final boolean showSpinner;
    private final Level level;

    Verbosity(MinionLogging minion, boolean showSpinner, Level level) {
        this.minion = minion;
        this.showSpinner = showSpinner;
        this.level = level;
    }

    public static Verbosity fromString(String verbosity) {
        if (verbosity == null) {
            return DEFAULT;
        }
        try {
            return valueOf(verbosity.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unrecognised verbosity " + verbosity);
        }
    }

    public boolean showMinionOutput() {
        return minion == MinionLogging.SHOW;
    }

    public boolean showSpinner() {
        return showSpinner;
    }

    public Level level() {
        return level;
    }
}

enum MinionLogging {
    SHOW, DONT_SHOW
}