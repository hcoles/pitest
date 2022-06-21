package org.pitest.executingtest;

import org.pitest.simpletest.NoArgsConstructorInstantiationStrategy;
import org.pitest.simpletest.TestMethod;
import org.pitest.simpletest.TestStep;
import org.pitest.simpletest.steps.CallStep;
import org.pitest.simpletest.steps.NoArgsInstantiateStep;
import org.pitest.testapi.Description;
import org.pitest.testapi.TestListener;
import org.pitest.testapi.TestResult;
import org.pitest.testapi.TestUnit;
import org.pitest.testapi.TestUnitExecutionListener;
import org.pitest.testapi.TestUnitFinder;
import org.pitest.testapi.execute.Pitest;
import org.pitest.testapi.execute.containers.UnContainer;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

/**
 * Toy example of a finder that executes tests during discovery. For testing
 * purposes only.
 */
public class ExecutingTestFinder implements TestUnitFinder {
    @Override
    public List<TestUnit> findTestUnits(Class<?> clazz, TestUnitExecutionListener listener) {
        return Arrays.stream(clazz.getMethods())
                .filter(m -> m.getAnnotation(ExecutingTest.class) != null)
                .map(m -> toTest(clazz, m))
                .map(m -> execute(listener, m))
                .collect(Collectors.toList());

    }

    private TestUnit toTest(Class<?> clazz, Method m) {
        TestMethod testMethod = new TestMethod(m);
        List<TestStep> steps = asList(NoArgsInstantiateStep.instantiate(clazz), new CallStep(testMethod));
        return new ExecutingTestUnit(new Description(testMethod.getName(), clazz), steps);
    }

    private TestUnit execute(TestUnitExecutionListener listener, TestUnit testUnit) {
        new Pitest(convertListener(listener)).run(new UnContainer(), Collections.singletonList(testUnit));
        return testUnit;
    }

    private TestListener convertListener(TestUnitExecutionListener listener) {
        return new TestListener() {
            @Override
            public void onRunStart() {

            }

            @Override
            public void onTestStart(Description d) {
                listener.executionStarted(d);
            }

            @Override
            public void onTestFailure(TestResult tr) {
                listener.executionFinished(tr.getDescription(), false);
            }

            @Override
            public void onTestSkipped(TestResult tr) {

            }

            @Override
            public void onTestSuccess(TestResult tr) {
                listener.executionFinished(tr.getDescription(), true);
            }

            @Override
            public void onRunEnd() {

            }
        };
    }
}
