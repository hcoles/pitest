package org.pitest.mutationtest;

import org.pitest.classpath.CodeSource;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.plugin.FeatureSelector;
public class HistoryParams {
    private final FeatureSelector conf;
    private final CodeSource code;
    private final ReportOptions data;

    public HistoryParams(FeatureSelector conf, CodeSource code, ReportOptions data) {
        this.conf = conf;
        this.code = code;
        this.data = data;
    }

    public FeatureSelector featureSettings() {
        return conf;
    }

    public CodeSource code() {
        return code;
    }

    public ReportOptions data() {
        return data;
    }
}
