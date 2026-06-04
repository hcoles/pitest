package org.pitest.classpath;

import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.plugin.FeatureSetting;

public class CodeSourceParams {
    private final FeatureSetting conf;
    private final ReportOptions data;

    public CodeSourceParams(FeatureSetting conf, ReportOptions data) {
        this.conf = conf;
        this.data = data;
    }

    public FeatureSetting conf() {
        return this.conf;
    }

    public ReportOptions data() {
        return this.data;
    }
}
