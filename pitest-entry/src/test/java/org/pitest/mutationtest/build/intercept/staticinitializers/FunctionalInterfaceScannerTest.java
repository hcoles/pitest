package org.pitest.mutationtest.build.intercept.staticinitializers;

import org.junit.Test;
import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.classpath.CodeSource;
import org.pitest.mutationtest.FixedCodeSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;


public class FunctionalInterfaceScannerTest {

    private final ClassByteArraySource source = ClassloaderByteArraySource.fromContext();

    FunctionalInterfaceScanner underTest = new FunctionalInterfaceScanner();

    @Test
    public void recognisesFunctionalInterfaces() {
        ClassTree functional = ClassTree.fromBytes(bytesFor(Function.class));
        ClassTree not = ClassTree.fromBytes(bytesFor(String.class));

        assertThat(underTest.apply(codeSourceFor(functional, not))).containsExactly("java/util/function/Function");
    }


    /*
    @Test
    public void wholeClassPath() throws Exception {
        ClassPath cp = new ClassPath();
        List<String> classes = cp.classNames().stream()
                .map(n -> source.getBytes(n))
                .map(b -> ClassTree.fromBytes(b.get()))
                .filter(ClassTree::isInterface)
                .flatMap(t -> underTest.apply(codeSourceFor(t)).stream())
                .distinct()
                .collect(Collectors.toList());

        System.out.println(classes.stream()
                .collect(Collectors.joining("\n")));
    }
*/

    /*
    // not a test and only works with java 9+
    public void crudelyScanJdk() throws Exception {
        FileSystem fs = FileSystems.getFileSystem(URI.create("jrt:/"));
        Path modules = fs.getPath("");

        List<String> jdkClasses = Files.walk(modules, FileVisitOption.FOLLOW_LINKS)
                .filter(f -> f.getFileName().toString().endsWith(".class"))
                .map(this::toBytes)
                .map(ClassTree::fromBytes)
                .flatMap(t -> underTest.apply(codeSourceFor(t)).stream())
                .distinct()
                .collect(Collectors.toList());


        System.out.println(jdkClasses.stream()
                .collect(Collectors.joining("\n")));
    }*/

    private byte[] toBytes(Path p) {
        try {
            return Files.readAllBytes(p);
        } catch (IOException x) {
            throw new RuntimeException(x);
        }
    }

    private CodeSource codeSourceFor(ClassTree... classes) {
        return new FixedCodeSource(asList(classes));
    }

    byte[] bytesFor(Class<?> clazz) {
        return this.source.getBytes(clazz.getName()).get();
    }
}