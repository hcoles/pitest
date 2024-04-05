package org.pitest.mutationtest.verify;

import org.pitest.classpath.CodeSource;
import org.pitest.mutationtest.config.ReportOptions;

public class BuildVerifierArguments {

    private final CodeSource code;
    private final ReportOptions data;

    public BuildVerifierArguments(CodeSource code, ReportOptions data) {
        this.code = code;
        this.data = data;
    }

    public ReportOptions data() {
        return data;
    }

    public CodeSource code() {
        return code;
    }
}
