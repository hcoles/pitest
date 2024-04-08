package org.pitest.verifier.interceptors;

import org.assertj.core.api.ListAssert;
import org.objectweb.asm.ClassWriter;
import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.classinfo.ClassName;
import org.pitest.classpath.ClassFilter;
import org.pitest.classpath.ClassPath;
import org.pitest.classpath.ClassPathRoot;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.classpath.CodeSource;
import org.pitest.classpath.DefaultCodeSource;
import org.pitest.classpath.PathFilter;
import org.pitest.classpath.ProjectClassPaths;
import org.pitest.mutationtest.config.PluginServices;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.mutationtest.verify.BuildMessage;
import org.pitest.mutationtest.verify.BuildVerifierArguments;
import org.pitest.mutationtest.verify.BuildVerifierFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
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

    public ListAssert<BuildMessage> messages() {
        return assertThat(factory.create(new BuildVerifierArguments(codeSource, new ReportOptions())).verifyBuild());
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


    public static CodeSource codeSourceForClasses(ClassTree... trees) {
        List<Sample> samples = Arrays.stream(trees)
                .map(t -> new Sample(t.name(), t))
                .collect(Collectors.toList());
        return codeSourceReturning(new SampleClassRoot(samples));
    }


    public static CodeSource codeSourceReturning(ClassName clazz, ClassName... classes) {
        List<ClassName> cs = new ArrayList<>();
        cs.add(clazz);
        cs.addAll(Arrays.stream(classes).collect(Collectors.toList()));

        List<Sample> samples = cs.stream()
                .map(c -> new Sample(c,aValidClass().rename(c)))
                .collect(Collectors.toList());

        return codeSourceReturning(new SampleClassRoot(samples));
    }

    public static CodeSource codeSourceReturning(ClassPathRoot root) {
        final PathFilter pf = new PathFilter(p -> true, p -> true);
        final ProjectClassPaths pcp = new ProjectClassPaths(
                new ClassPath(root), new ClassFilter(c -> true, c -> true), pf);
        return new DefaultCodeSource(pcp);
    }

    public static ClassTree aValidClass() {
        return ClassTree.fromBytes(ClassloaderByteArraySource.fromContext()
                .getBytes(EmptyClass.class.getName()).get());
    }

    private static CodeSource emptyCodeSource() {
        final PathFilter pf = new PathFilter(p -> true, p -> true);
        final ProjectClassPaths pcp = new ProjectClassPaths(
                new ClassPath(), new ClassFilter(c -> true, c -> true), pf);
        return new DefaultCodeSource(pcp);
    }
}

class EmptyClass {

}

class SampleClassRoot implements ClassPathRoot {

    private final Collection<Sample> samples;

    SampleClassRoot(Collection<Sample> samples) {
        this.samples = samples;
    }

    @Override
    public URL getResource(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public InputStream getData(String name) {
        return samples.stream()
                .filter(s -> s.className.equals(ClassName.fromString(name)))
                .map(s -> s.clazz)
                .filter(t -> t != null)
                .map(this::asBytes)
                .map(b -> new ByteArrayInputStream(b))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Collection<String> classNames() {
        return samples.stream()
                .map(s -> s.className.asJavaName())
                .collect(Collectors.toSet());
    }

    @Override
    public Optional<String> cacheLocation() {
        return Optional.empty();
    }


    private byte[] asBytes(ClassTree tree) {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        tree.rawNode().accept(classWriter);
        return classWriter.toByteArray();
    }

}
