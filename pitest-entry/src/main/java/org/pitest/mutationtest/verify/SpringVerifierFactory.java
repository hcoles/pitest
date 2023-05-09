package org.pitest.mutationtest.verify;

import org.pitest.classinfo.ClassName;
import org.pitest.classpath.CodeSource;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;

public class SpringVerifierFactory implements BuildVerifierFactory {
    @Override
    public BuildVerifier create(CodeSource code) {
        return new SpringVerifier(code);
    }

    @Override
    public String description() {
        return "Detect missing spring plugin";
    }
}

class SpringVerifier implements BuildVerifier {

    private final CodeSource code;

    SpringVerifier(CodeSource code) {
        this.code = code;
    }

    @Override
    public List<String> verify() {
        if (springIsOnClassPath() && !springPluginIsPresent()) {
            return asList("Project uses Spring, but the Arcmutate Spring plugin is not present (https://docs.arcmutate.com/docs/spring.html)");
        }

        return Collections.emptyList();
    }


    private boolean springPluginIsPresent() {
        return code.fetchClassBytes(ClassName.fromString("com.groupcdg.arcmutate.spring.PluginMarker")).isPresent();
    }

    private boolean springIsOnClassPath() {
        return code.fetchClassBytes(ClassName.fromString("org.springframework.core.SpringVersion")).isPresent();
    }
}