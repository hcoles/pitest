package org.pitest.coverage;

import org.pitest.classinfo.ClassInfo;
import org.pitest.classinfo.ClassName;
import org.pitest.classpath.CodeSource;
import org.pitest.functional.FCollection;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Line based coverage data, used by html report and the history system
 * separated here to prevent methods being re-implemented with data
 * not available when loaded from disk for the report aggregate
 */
public class LegacyClassCoverage implements ReportCoverage {

    private final CodeSource code;
    private final Map<String, Collection<ClassInfo>> classesForFile;
    private final Map<ClassName, Map<ClassLine, Set<TestInfo>>> lineCoverage  = new LinkedHashMap<>();
    private final Map<BlockLocation, Set<Integer>> blocksToLines = new LinkedHashMap<>();
    private final LineMap lm;

    public LegacyClassCoverage(CodeSource code, LineMap lm) {
        this.code = code;
        this.lm = lm;
        this.classesForFile = FCollection.bucket(code.getCode(),
                keyFromClassInfo());
    }

    public void loadBlockDataOnly(Collection<BlockLocation> coverageData) {
        addTestToClasses(new TestInfo("fake", "fakeTest",0,  Optional.empty(), 1 ),
                coverageData);
    }

    @Override
    public Collection<ClassInfo> getClassInfo(final Collection<ClassName> classes) {
        return this.code.getClassInfo(classes);
    }

    @Override
    public int getNumberOfCoveredLines(Collection<ClassName> mutatedClass) {
        return mutatedClass.stream()
                .map(this::getLineCoverageForClassName)
                .mapToInt(Map::size)
                .sum();
    }

    @Override
    public Collection<TestInfo> getTestsForClassLine(final ClassLine classLine) {
        final Collection<TestInfo> result = getLineCoverageForClassName(
                classLine.getClassName()).get(classLine);
        if (result == null) {
            return Collections.emptyList();
        } else {
            return result;
        }
    }

    @Override
    public Collection<ClassInfo> getClassesForFile(final String sourceFile,
                                                   String packageName) {
        final Collection<ClassInfo> value = classesForFile.get(
                keyFromSourceAndPackage(sourceFile, packageName));
        if (value == null) {
            return Collections.emptyList();
        } else {
            return value;
        }
    }

    public Collection<TestInfo> getTestsForClass(ClassName clazz) {
        return this.lineCoverage.getOrDefault(clazz, Collections.emptyMap()).values().stream()
                .flatMap(s -> s.stream())
                .collect(Collectors.toSet());
    }

    void addTestToClasses(TestInfo ti, Collection<BlockLocation> coverage) {
        for (BlockLocation each : coverage) {
            ClassName clazz = each.getLocation().getClassName();
            Map<ClassLine, Set<TestInfo>> linesToTests = lineCoverage.getOrDefault(clazz, new LinkedHashMap<>(0));
            for (int line : getLinesForBlock(each)) {
                addTestToClassLine(each.getLocation().getClassName(), linesToTests, ti, line);
            }
            // can we get blocks from different classes?
            this.lineCoverage.put(each.getLocation().getClassName(), linesToTests);
        }
    }
    private void addTestToClassLine(ClassName clazz,
                                    Map<ClassLine, Set<TestInfo>> linesToTests,
                                    TestInfo test,
                                    int line) {
        ClassLine cl = new ClassLine(clazz, line);
        Set<TestInfo> tis = linesToTests.getOrDefault(cl, new TreeSet<>(new TestInfoNameComparator()));
        tis.add(test);
        linesToTests.put(cl, tis);
    }


    private Map<ClassLine, Set<TestInfo>> getLineCoverageForClassName(final ClassName clazz) {
        return this.lineCoverage.getOrDefault(clazz, Collections.emptyMap());
    }

    private static Function<ClassInfo, String> keyFromClassInfo() {
        return c -> keyFromSourceAndPackage(c.getSourceFileName(), c.getName()
                .getPackage().asJavaName());
    }

    private static String keyFromSourceAndPackage(final String sourceFile,
                                                  final String packageName) {
        return packageName + " " + sourceFile;
    }

    private Set<Integer> getLinesForBlock(BlockLocation bl) {
        Set<Integer> lines = this.blocksToLines.get(bl);
        if (lines == null) {
            calculateLinesForBlocks(bl.getLocation().getClassName());
            lines = this.blocksToLines.get(bl);
            if (lines == null) {
                lines = Collections.emptySet();
            }
        }

        return lines;
    }

    private void calculateLinesForBlocks(ClassName className) {
        final Map<BlockLocation, Set<Integer>> lines = this.lm.mapLines(className);
        this.blocksToLines.putAll(lines);
    }

}
