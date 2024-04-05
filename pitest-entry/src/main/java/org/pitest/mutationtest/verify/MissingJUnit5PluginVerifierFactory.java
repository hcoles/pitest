package org.pitest.mutationtest.verify;

import org.pitest.classinfo.ClassName;
import org.pitest.classpath.CodeSource;
import org.pitest.util.Log;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;

public class MissingJUnit5PluginVerifierFactory implements BuildVerifierFactory {
    @Override
    public BuildVerifier create(CodeSource code) {
        return new MissingJUnit5PluginVerifier(code);
    }

    @Override
    public String description() {
        return "Detect missing JUnit5 plugin";
    }

}

class MissingJUnit5PluginVerifier implements BuildVerifier {

    private final CodeSource code;

    MissingJUnit5PluginVerifier(CodeSource code) {
        this.code = code;
    }

    @Override
    public List<BuildMessage> verifyBuild() {
        if (!junit5PluginIsPresent() && junitJupiterPresent()) {
            // log as well as return in case the run is aborted before messages are displayed at the end
            String msg = "JUnit 5 is on the classpath but the pitest junit 5 plugin is not installed.";
            Log.getLogger().warning(msg);
            return asList(new BuildMessage(msg, "https://github.com/pitest/pitest-junit5-plugin", 5));
        }

        return Collections.emptyList();
    }

    private boolean junitJupiterPresent() {
        return code.fetchClassBytes(ClassName.fromString("org.junit.jupiter.api.Test")).isPresent();
    }

    private boolean junit5PluginIsPresent() {
        return code.fetchClassBytes(ClassName.fromString("org.pitest.junit5.JUnit5TestPluginFactory")).isPresent();
    }
}
