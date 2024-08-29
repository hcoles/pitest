package org.pitest.mutationtest.build.intercept.staticinitializers;

import org.pitest.classpath.CodeSource;
import org.pitest.mutationtest.build.InterceptorParameters;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.build.MutationInterceptorFactory;
import org.pitest.plugin.Feature;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class StaticInitializerInterceptorFactory implements MutationInterceptorFactory {

    private final Function<CodeSource, Set<String>> delayedExecutionTypes;

    public StaticInitializerInterceptorFactory() {
        this(new FunctionalInterfaceScanner());
    }

    public StaticInitializerInterceptorFactory(Function<CodeSource, Set<String>> delayedExecutionTypes) {
        this.delayedExecutionTypes = delayedExecutionTypes.andThen(this::functionalInterfaces);
    }

    @Override
    public String description() {
        return "Static initializer code detector plugin";
    }

    @Override
    public MutationInterceptor createInterceptor(InterceptorParameters params) {
        Set<String> types = delayedExecutionTypes.apply(params.code());
        return new StaticInitializerInterceptor(types);
    }

    @Override
    public Feature provides() {
        return Feature.named("FSTATI")
                .withOnByDefault(true)
                .withDescription("Filters mutations in static initializers and code called only from them");
    }

    private Set<String> functionalInterfaces(Set<String> existing) {
        Set<String> classes = new HashSet<>(existing);
        try (BufferedReader r = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/functional_interfaces.txt")))) {
            String line = r.readLine();
            while (line != null) {
                classes.add(line);
                line = r.readLine();
            }
            return classes;
        } catch (IOException e) {
            throw new RuntimeException("Could not read embedded list of functional interfaces!");
        }
    }
}
