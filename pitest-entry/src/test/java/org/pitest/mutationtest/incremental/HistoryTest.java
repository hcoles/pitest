package org.pitest.mutationtest.incremental;

import com.example.history.ClassA;
import com.example.history.ClassATest;
import com.example.history.SlowKillingTest;
import com.example.history.UselessTest1;
import com.example.history.UselessTest2;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.classinfo.ClassHash;
import org.pitest.classinfo.ClassIdentifier;
import org.pitest.classinfo.ClassName;
import org.pitest.classinfo.HierarchicalClassId;
import org.pitest.classinfo.Repository;
import org.pitest.classpath.ClassPath;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.classpath.CodeSource;
import org.pitest.classpath.ProjectClassPaths;
import org.pitest.functional.Streams;
import org.pitest.mutationtest.config.PluginServices;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.mutationtest.config.SettingsFactory;
import org.pitest.mutationtest.tooling.AnalysisResult;
import org.pitest.mutationtest.tooling.EntryPoint;
import org.pitest.testapi.TestGroupConfig;
import org.pitest.util.Verbosity;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class HistoryTest {

    @Rule
    public TemporaryFolder root = new TemporaryFolder();

    @Test
    public void sameResultsWhenNoChanges() throws Exception {
        Project project = createProject(root, ClassA.class, ClassATest.class);
        AnalysisResult run1 = runPitest(project);
        AnalysisResult run2 = runPitest(project);

        assertSameNumberOfMutationsKilled(run1, run2);
        assertThat(numberOfTestsRun(run2)).isEqualTo(0);
    }


    @Test
    public void sameResultWhenCodeUnderTestTouched() throws Exception {
        Project project = createProject(root, ClassA.class, ClassATest.class);

        AnalysisResult run1 = runPitest(project);

        project.modifyClass(ClassA.class);

        AnalysisResult run2 = runPitest(project);

        assertSameNumberOfMutationsKilled(run1, run2);
        assertThat(numberOfTestsRun(run2)).isGreaterThan(0);
    }

    @Test
    public void testsReRunWhenTestAltered() throws Exception {
        Project project = createProject(root, ClassA.class, ClassATest.class);

        runPitest(project);

        project.modifyClass(ClassATest.class);

        AnalysisResult run2 = runPitest(project);

        assertThat(numberOfTestsRun(run2)).isGreaterThan(0);
    }


    @Test
    public void mutationsUncoveredWhenTestRemoved() throws Exception {
        Project project = createProject(root, ClassA.class, ClassATest.class);
        AnalysisResult run1 = runPitest(project);

        project.removeTest(ClassATest.class);

        AnalysisResult run2 = runPitest(project);

        assertThat(getTotalDetectedMutations(run2)).isEqualTo(0);
        assertThat(run1).isNotEqualTo(run2);
    }

    @Test
    public void mutationsKilledWhenTestAdded() throws Exception {
        Project project = createProject(root, ClassA.class);
        AnalysisResult run1 = runPitest(project);

        project.addTest(ClassATest.class);

        AnalysisResult run2 = runPitest(project);

        assertThat(getTotalDetectedMutations(run2)).isEqualTo(1);
        assertThat(run1).isNotEqualTo(run2);
    }

    @Test
    public void prioritisesLastKillingTest() throws Exception {
        Project project = createProject(root, ClassA.class, UselessTest1.class, UselessTest2.class, SlowKillingTest.class);
        AnalysisResult run1 = runPitest(project);

        project.modifyClass(ClassA.class);

        AnalysisResult run2 = runPitest(project);

        assertThat(numberOfTestsRun(run2)).isLessThan(numberOfTestsRun(run1));
    }


    private static long getTotalDetectedMutations(AnalysisResult run2) {
        return run2.getStatistics().get().getMutationStatistics().getTotalDetectedMutations();
    }

    private AnalysisResult runPitest(Project project) throws IOException {
        EntryPoint entryPoint = new EntryPoint();
        ReportOptions data = new ReportOptions();
        data.setReportDir(project.reportsDir());
        data.setGroupConfig(new TestGroupConfig());
        data.setExcludedRunners(Collections.emptyList());
        data.setSourceDirs(Collections.emptyList());
        data.setVerbosity(Verbosity.VERBOSE);
        data.setTargetClasses(singletonList("com.example.*"));

        data.setHistoryInputLocation(project.root().resolve("history.txt").toFile());
        data.setHistoryOutputLocation(project.root().resolve("history.txt").toFile());

        SettingsFactory settings = settingsFactory(project, data);
        return entryPoint.execute(project.root().toFile(), data, settings, new HashMap<>());

    }

    private SettingsFactory settingsFactory(Project project, ReportOptions data) {
        return new SettingsFactory(data, PluginServices.makeForContextLoader()) {
            public CodeSource createCodeSource(ProjectClassPaths classPath) {
                ClassloaderByteArraySource bas = ClassloaderByteArraySource.fromContext();
                Repository r = new Repository(bas);
                return new CodeSource() {
                    @Override
                    public Stream<ClassTree> codeTrees() {
                        return project.classes().stream()
                                .map(c -> bas.getBytes(c.getName()).get())
                                .map(ClassTree::fromBytes);
                    }

                    @Override
                    public Set<ClassName> getCodeUnderTestNames() {
                        return project.classes().stream()
                                .map(ClassName::fromClass)
                                .collect(Collectors.toSet());
                    }

                    @Override
                    public Set<ClassName> getTestClassNames() {
                        return project.tests().stream()
                                .map(ClassName::fromClass)
                                .peek(System.out::println)
                                .collect(Collectors.toSet());
                    }

                    @Override
                    public Stream<ClassTree> testTrees() {
                        return project.tests().stream()
                                .map(c -> bas.getBytes(c.getName()).get())
                                .map(ClassTree::fromBytes);
                    }

                    @Override
                    public ClassPath getClassPath() {
                        // return our own classpath???
                        return new ClassPath();
                    }

                    @Override
                    public Optional<ClassName> findTestee(String s) {
                        return Optional.empty();
                    }

                    @Override
                    public Optional<byte[]> fetchClassBytes(ClassName className) {
                        return getBytes(className.asJavaName());
                    }

                    @Override
                    public Optional<ClassHash> fetchClassHash(ClassName className) {
                        return project.hasClass(className)
                                .flatMap(r::fetchClassHash)
                                .map(this::modify);
                    }

                    private ClassHash modify(ClassHash classInfo) {
                        if (project.isModified(classInfo.getName())) {

                            return new ClassHash() {

                                @Override
                                public ClassIdentifier getId() {
                                    return new ClassIdentifier(classInfo.getId().getHash() + 1, classInfo.getName());
                                }

                                @Override
                                public ClassName getName() {
                                    return classInfo.getName();
                                }

                                @Override
                                public BigInteger getDeepHash() {
                                    return classInfo.getDeepHash();
                                }

                                @Override
                                public HierarchicalClassId getHierarchicalId() {
                                    return new HierarchicalClassId(this.getId(), classInfo.getHierarchicalId().getHierarchicalHash());
                                }
                            };
                        }
                        return classInfo;
                    }

                    @Override
                    public Collection<ClassHash> fetchClassHashes(Collection<ClassName> classes) {
                        return classes.stream()
                                .flatMap(c -> Streams.fromOptional(fetchClassHash(c)))
                                .collect(Collectors.toList());
                    }

                    @Override
                    public Optional<byte[]> getBytes(String s) {
                        return project.hasClass(ClassName.fromString(s)).
                                flatMap(c -> bas.getBytes(c.asJavaName()));
                    }
                };
            }
        };
    }

    private Project createProject(TemporaryFolder tempFolder, Class<?> c, Class<?>... tests) throws IOException {
        Path root = tempFolder.getRoot().toPath();
        Path project = Files.createDirectories(root.resolve("project"));
        Files.createDirectory(project.resolve("reports"));
        return new Project(project, asList(c), asList(tests));
    }

    private void assertSameNumberOfMutationsKilled(AnalysisResult r, AnalysisResult r2) {
        long detected1 = getTotalDetectedMutations(r);
        long detected2 = getTotalDetectedMutations(r2);
        assertThat(detected1).isEqualTo(detected2);
    }

    private static long numberOfTestsRun(AnalysisResult r) {
        return r.getStatistics().get().getMutationStatistics().getNumberOfTestsRun();
    }
}
