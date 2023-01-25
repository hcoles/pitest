package org.pitest.coverage;

import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.classinfo.ClassName;
import org.pitest.classpath.CodeSource;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Line based coverage data, used by html report and the history system
 * separated here to prevent methods being re-implemented with data
 * not available when loaded from disk for the report aggregate
 */
public class LegacyClassCoverage implements ReportCoverage {

    private final CodeSource code;
    private final Map<String, List<ClassLines>> classesForFile;
    private final Map<ClassName, Map<ClassLine, Set<TestInfo>>> lineCoverage  = new LinkedHashMap<>();
    private final Map<BlockLocation, Set<Integer>> blocksToLines = new LinkedHashMap<>();
    private final LineMap lm;

    public LegacyClassCoverage(CodeSource code, LineMap lm) {
        this.code = code;
        this.lm = lm;
        this.classesForFile = code.codeTrees()
                .collect(Collectors.groupingBy(keyFromClassInfo(), Collectors.mapping(tree -> new ClassLines(tree.name(), tree.codeLineNumbers()), Collectors.toList())));
    }

    public void loadBlockDataOnly(Collection<BlockLocation> coverageData) {
        addTestToClasses(new TestInfo("fake", "fakeTest",0,  Optional.empty(), 1 ),
                coverageData);
    }

    @Override
    public ClassLines getCodeLinesForClass(final ClassName clazz) {
        return code.fetchClassBytes(clazz)
                .map(ClassTree::fromBytes)
                .map(ClassLines::fromTree)
                .orElse(new ClassLines(clazz, Collections.emptySet()));
    }

    @Override
    public Set<ClassLine> getCoveredLines(ClassName mutatedClass) {
        return  Stream.of(mutatedClass)
                .flatMap(m -> getLineCoverageForClassName(m).entrySet().stream())
                .filter(e -> !e.getValue().isEmpty())
                .map( e -> e.getKey())
                .collect(Collectors.toSet());
    }

    @Override
    public Collection<ClassLines> getClassesForFile(String sourceFile,
                                                   String packageName) {
        return classesForFile.getOrDefault(keyFromSourceAndPackage(sourceFile, packageName), Collections.emptyList());
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

    private static Function<ClassTree, String> keyFromClassInfo() {
        return c -> keyFromSourceAndPackage(c.rawNode().sourceFile, c.name()
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
