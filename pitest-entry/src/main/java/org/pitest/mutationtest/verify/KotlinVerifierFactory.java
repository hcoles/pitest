package org.pitest.mutationtest.verify;

import org.pitest.classinfo.ClassName;
import org.pitest.classpath.CodeSource;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;

public class KotlinVerifierFactory implements BuildVerifierFactory {
    @Override
    public BuildVerifier create(CodeSource code) {
        return new KotlinVerifier(code);
    }

    @Override
    public String description() {
        return "Detect missing kotlin plugin";
    }
}

class KotlinVerifier implements BuildVerifier {

    private final CodeSource code;

    KotlinVerifier(CodeSource code) {
        this.code = code;
    }

    @Override
    public List<String> verify() {
        if (kotlinIsOnClassPath() && !kotlinPluginIsPresent() && kotlinClassesToBeMutated()) {
            return asList("Project uses kotlin, but the Arcmutate kotlin plugin is not present (https://docs.arcmutate.com/docs/kotlin.html)");
        }

        return Collections.emptyList();
    }

    private boolean kotlinClassesToBeMutated() {
        return code.codeTrees()
                .anyMatch(c -> c.rawNode().sourceFile != null && c.rawNode().sourceFile.endsWith(".kt"));
    }

    private boolean kotlinPluginIsPresent() {
        return code.fetchClassBytes(ClassName.fromString("com.groupcdg.pitest.kotlin.KotlinFilterInterceptor")).isPresent();
    }

    private boolean kotlinIsOnClassPath() {
        return code.fetchClassBytes(ClassName.fromString("kotlin.KotlinVersion")).isPresent();
    }
}