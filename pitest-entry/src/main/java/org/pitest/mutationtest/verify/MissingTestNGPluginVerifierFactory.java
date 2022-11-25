package org.pitest.mutationtest.verify;

import org.pitest.classinfo.ClassName;
import org.pitest.classpath.CodeSource;
import org.pitest.util.Log;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;

public class MissingTestNGPluginVerifierFactory implements BuildVerifierFactory {
    @Override
    public BuildVerifier create(CodeSource code) {
        return new MissingTestNGPluginVerifier(code);
    }

    @Override
    public String description() {
        return "Detect missing TestNG plugin";
    }

}

class MissingTestNGPluginVerifier implements BuildVerifier {

    private final CodeSource code;

    MissingTestNGPluginVerifier(CodeSource code) {
        this.code = code;
    }

    @Override
    public List<String> verify() {
        if (!testNGPluginIsPresent() && testNGisPresent()) {
            // log as well as return in case the run is aborted before messages are displayed at the end
            String msg = "TestNG is on the classpath but the pitest TestNG plugin is not installed (https://github.com/pitest/pitest-testng-plugin)";
            Log.getLogger().warning(msg);
            return asList(msg);
        }

        return Collections.emptyList();
    }

    private boolean testNGisPresent() {
        return code.fetchClassBytes(ClassName.fromString("org.testng.annotations.Test")).isPresent();
    }

    private boolean testNGPluginIsPresent() {
        return code.fetchClassBytes(ClassName.fromString("org.pitest.testng.TestNGPlugin")).isPresent();
    }
}
