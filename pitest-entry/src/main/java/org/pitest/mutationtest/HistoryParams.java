package org.pitest.mutationtest;

import org.pitest.classpath.CodeSource;
import org.pitest.plugin.FeatureSelector;
public class HistoryParams {
    private final FeatureSelector conf;
    private final CodeSource code;

    public HistoryParams(FeatureSelector conf, CodeSource code) {
        this.conf = conf;
        this.code = code;
    }

    public FeatureSelector featureSettings() {
        return conf;
    }

    public CodeSource code() {
        return code;
    }
}
