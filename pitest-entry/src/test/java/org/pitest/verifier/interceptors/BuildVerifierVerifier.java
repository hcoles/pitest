package org.pitest.verifier.interceptors;

import org.assertj.core.api.ListAssert;
import org.pitest.classinfo.ClassName;
import org.pitest.classpath.ClassFilter;
import org.pitest.classpath.ClassPath;
import org.pitest.classpath.ClassPathRoot;
import org.pitest.classpath.CodeSource;
import org.pitest.classpath.PathFilter;
import org.pitest.classpath.ProjectClassPaths;
import org.pitest.mutationtest.config.PluginServices;
import org.pitest.mutationtest.verify.BuildVerifierFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class BuildVerifierVerifier {
    private final BuildVerifierFactory factory;
    private final CodeSource codeSource;

    public BuildVerifierVerifier(BuildVerifierFactory factory) {
        this(factory, emptyCodeSource());
    }

    public BuildVerifierVerifier(BuildVerifierFactory factory, CodeSource codeSource) {
        this.factory = factory;
        this.codeSource = codeSource;
    }

    public static BuildVerifierVerifier confirmFactory(BuildVerifierFactory factory) {
        return new BuildVerifierVerifier(factory);
    }

    public void isOnChain() {
        factoryIsOnChain(factory.getClass());
    }

    public ListAssert<String> issues() {
        return assertThat(factory.create(codeSource).verify());
    }

    public BuildVerifierVerifier withCodeSource(CodeSource source) {
        return new BuildVerifierVerifier(factory, source);
    }

    private static void factoryIsOnChain(Class<?> factory) {
        List<Class<?>> allInterceptors = PluginServices.makeForContextLoader().findVerifiers().stream()
                .map(BuildVerifierFactory::getClass)
                .collect(Collectors.toList());

        assertThat(allInterceptors).contains(factory);
    }


   public static CodeSource codeSourceReturning(ClassName clazz, ClassName ... classes) {
        List<ClassName> cs = new ArrayList<>();
        cs.add(clazz);
        cs.addAll(Arrays.stream(classes).collect(Collectors.toList()));
       return codeSourceReturning(new NameOnlyClassPathRoot(cs.stream()
               .map(ClassName::asJavaName)
               .collect(Collectors.toSet())));
   }

    public static CodeSource codeSourceReturning(ClassPathRoot root) {
        final PathFilter pf = new PathFilter(p -> true, p -> true);
        final ProjectClassPaths pcp = new ProjectClassPaths(
                new ClassPath(root), new ClassFilter(c -> true, c -> true), pf);
        return new CodeSource(pcp);
    }

    private static CodeSource emptyCodeSource() {
        final PathFilter pf = new PathFilter(p -> true, p -> true);
        final ProjectClassPaths pcp = new ProjectClassPaths(
                new ClassPath(), new ClassFilter(c -> true, c -> true), pf);
        return new CodeSource(pcp);
    }


}

class NameOnlyClassPathRoot implements ClassPathRoot {

    private final Set<String> names;

    NameOnlyClassPathRoot(Set<String> names) {
        this.names = names;
    }
    
    @Override
    public URL getResource(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public InputStream getData(String name) {
        if (names.contains(name)) {
            return new ByteArrayInputStream(new byte[0]);
        }
        return null;
    }

    @Override
    public Collection<String> classNames() {
        return names;
    }

    @Override
    public Optional<String> cacheLocation() {
        return Optional.empty();
    }
}
