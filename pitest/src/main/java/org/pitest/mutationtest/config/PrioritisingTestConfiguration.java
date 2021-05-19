package org.pitest.mutationtest.config;

import org.pitest.help.PitHelpError;
import org.pitest.testapi.Configuration;
import org.pitest.testapi.TestSuiteFinder;
import org.pitest.testapi.TestUnitFinder;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

class PrioritisingTestConfiguration implements Configuration {
    private final List<Configuration> children;
    private final TestUnitFinder finder;
    private final TestSuiteFinder suiteFinder;

    PrioritisingTestConfiguration(List<Configuration> children) {
        this.children = pickChildren(children);
        this.finder = makeFinder(this.children);
        this.suiteFinder = makeSuiteFinder(this.children);
    }

    @Override
    public TestUnitFinder testUnitFinder() {
        return finder;
    }

    @Override
    public TestSuiteFinder testSuiteFinder() {
        return suiteFinder;
    }

    @Override
    public Optional<PitHelpError> verifyEnvironment() {
        return children.stream()
                .map(Configuration::verifyEnvironment)
                .findFirst()
                .get();
    }

    private static List<Configuration> pickChildren(List<Configuration> configs) {
        List<Configuration> working = configs.stream()
                .filter(c -> !c.verifyEnvironment().isPresent())
                .sorted(byPriority())
                .collect(Collectors.toList());
        // We don't have a working config, let it report errors later
        if (working.isEmpty()) {
            return configs;
        }
        return working;
    }

    private static Comparator<Configuration> byPriority() {
        return Comparator.comparingInt(Configuration::priority);
    }

    private TestUnitFinder makeFinder(List<Configuration> children) {
        List<TestUnitFinder> finders = children.stream()
                .map(Configuration::testUnitFinder)
                .collect(Collectors.toList());
        return new PrioritisingTestUnitFinder(finders);
    }

    private TestSuiteFinder makeSuiteFinder(List<Configuration> children) {
        List<TestSuiteFinder> finders = children.stream()
                .map(Configuration::testSuiteFinder)
                .collect(Collectors.toList());
        return new PrioritisingTestSuiteFinder(finders);
    }
}
