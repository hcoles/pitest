package org.pitest.testapi.execute;

import org.junit.Test;
import org.pitest.help.PitHelpError;
import org.pitest.testapi.Configuration;
import org.pitest.testapi.Description;
import org.pitest.testapi.ResultCollector;
import org.pitest.testapi.TestSuiteFinder;
import org.pitest.testapi.TestUnit;
import org.pitest.testapi.TestUnitFinder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class FindTestUnitsTest {

    @Test
    public void doesNotDuplicateTestsWhenClassesReintroducedViaSuite() {
        Configuration config = new Configuration() {
            @Override
            public TestUnitFinder testUnitFinder() {
                return (c,n) -> {
                    if (c.getName().contains("ATest")) {
                        return List.of(new FakeTestUnit());
                    }
                    return List.of();
                };
            }

            @Override
            public TestSuiteFinder testSuiteFinder() {
                return c -> List.of(ATest.class, ATest2.class);
            }

            @Override
            public Optional<PitHelpError> verifyEnvironment() {
                return Optional.empty();
            }
        };
        FindTestUnits underTest = new FindTestUnits(config);

        List<TestUnit> actual = underTest.findTestUnitsForAllSuppliedClasses(List.of(ATest2.class, ATest.class));
        assertThat(actual).hasSize(2);
    }
}

class ATest {

}

class ATest2 {

}

class FakeTestUnit implements TestUnit {
    @Override
    public void execute(ResultCollector rc) {

    }

    @Override
    public Description getDescription() {
        return null;
    }
}