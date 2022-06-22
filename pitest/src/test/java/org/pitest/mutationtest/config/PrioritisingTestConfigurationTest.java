package org.pitest.mutationtest.config;

import org.junit.Test;
import org.pitest.help.Help;
import org.pitest.help.PitHelpError;
import org.pitest.testapi.Configuration;
import org.pitest.testapi.Description;
import org.pitest.testapi.NullExecutionListener;
import org.pitest.testapi.ResultCollector;
import org.pitest.testapi.TestSuiteFinder;
import org.pitest.testapi.TestUnit;
import org.pitest.testapi.TestUnitFinder;

import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

public class PrioritisingTestConfigurationTest {

    TestUnit findMe = fakeUnit("a");
    TestUnit dontFindMe = fakeUnit("b");
    private Class<?> dontFindThisClass = String.class;
    private Class<?> findThisClass = Integer.class;

    @Test
    public void findsNoTestsWhenNothingMatchesChildConfiguration() {
        Configuration findsNothing = configuration(1, emptyList());
        PrioritisingTestConfiguration testee = new PrioritisingTestConfiguration(asList(findsNothing));
        List<TestUnit> actual = testee.testUnitFinder().findTestUnits(String.class, new NullExecutionListener());
        assertThat(actual).isEmpty();
    }

    @Test
    public void highestPriorityConfigurationFindsTest() {
        Configuration c0 = configuration(2, dontFindMe);
        Configuration c1 = configuration(1, findMe);
        Configuration c2 = configuration(2, dontFindMe);
        PrioritisingTestConfiguration testee = new PrioritisingTestConfiguration(asList(c0, c1, c2));

        List<TestUnit> actual = testee.testUnitFinder().findTestUnits(String.class, new NullExecutionListener());

        assertThat(actual).containsOnly(findMe);
    }

    @Test
    public void configurationsWithEnvironmentalErrorsNotUsed() {
        Configuration c0 = configuration(1, asList(dontFindMe), new PitHelpError(Help.NO_JUNIT));
        Configuration c1 = configuration(2, findMe);
        PrioritisingTestConfiguration testee = new PrioritisingTestConfiguration(asList(c0, c1));

        List<TestUnit> actual = testee.testUnitFinder().findTestUnits(String.class, new NullExecutionListener());

        assertThat(actual).containsOnly(findMe);
    }

    @Test
    public void allowsMixedTestTypes() {
        Configuration findsNothing = configuration(1, emptyList());
        Configuration c1 = configuration(2, findMe);
        Configuration c2 = configuration(3, dontFindMe);
        PrioritisingTestConfiguration testee = new PrioritisingTestConfiguration(asList(findsNothing, c1, c2));

        List<TestUnit> actual = testee.testUnitFinder().findTestUnits(String.class, new NullExecutionListener());

        assertThat(actual).containsOnly(findMe);
    }


    @Test
    public void reportsNoErrorIfAtLeastOneConfigValid() {
        Configuration c0 = configuration(1, asList(dontFindMe), new PitHelpError(Help.NO_JUNIT));
        Configuration c1 = configuration(2, findMe);
        PrioritisingTestConfiguration testee = new PrioritisingTestConfiguration(asList(c0, c1));

        assertThat(testee.verifyEnvironment()).isEmpty();
    }

    @Test
    public void reportsErrorWhenAllConfigsInValid() {
        Configuration c0 = configuration(1, asList(dontFindMe), new PitHelpError(Help.NO_JUNIT));
        Configuration c1 = configuration(2, asList(dontFindMe), new PitHelpError(Help.NO_JUNIT));
        PrioritisingTestConfiguration testee = new PrioritisingTestConfiguration(asList(c0, c1));

        assertThat(testee.verifyEnvironment()).isPresent();
    }

    @Test
    public void highestPriorityConfigurationFindsSuites() {
        Configuration c0 = suiteConfiguration(2, dontFindThisClass);
        Configuration c1 = suiteConfiguration(1, findThisClass);
        Configuration c2 = suiteConfiguration(2, dontFindThisClass);
        PrioritisingTestConfiguration testee = new PrioritisingTestConfiguration(asList(c0, c1, c2));

        List<Class<?>> actual = testee.testSuiteFinder().apply(String.class);

        assertThat(actual).containsOnly(findThisClass);
    }

    private TestUnit fakeUnit(String name) {
        return new TestUnit() {
            @Override
            public void execute(ResultCollector rc) {

            }

            @Override
            public Description getDescription() {
                return new Description(name);
            }
        };
    }

    private Configuration configuration(int priority, TestUnit testUnit) {
        return configuration(priority, asList(testUnit), null);
    }

    private Configuration configuration(int priority, List<TestUnit> testUnit) {
        return configuration(priority, testUnit, null);
    }

    private Configuration configuration(int priority, List<TestUnit> testUnits, PitHelpError error) {
        return new Configuration() {

            @Override
            public int priority() {
                return priority;
            }

            @Override
            public TestUnitFinder testUnitFinder() {
                return (c,l) -> testUnits;
            }

            @Override
            public TestSuiteFinder testSuiteFinder() {
                return null;
            }

            @Override
            public Optional<PitHelpError> verifyEnvironment() {
                return Optional.ofNullable(error);
            }
        };
    }

    private Configuration suiteConfiguration(int priority, Class<?> suiteClass) {
        return new Configuration() {

            @Override
            public int priority() {
                return priority;
            }

            @Override
            public TestUnitFinder testUnitFinder() {
                return (c,l) -> emptyList();
            }

            @Override
            public TestSuiteFinder testSuiteFinder() {
                return c -> asList(suiteClass);
            }

            @Override
            public Optional<PitHelpError> verifyEnvironment() {
                return Optional.empty();
            }
        };
    }
}