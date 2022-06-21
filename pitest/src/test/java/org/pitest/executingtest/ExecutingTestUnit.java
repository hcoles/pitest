package org.pitest.executingtest;

import org.pitest.simpletest.SteppedTestUnit;
import org.pitest.simpletest.TestStep;
import org.pitest.testapi.Description;
import org.pitest.testapi.ExecutedInDiscovery;

import java.util.Collection;
import java.util.Optional;

public class ExecutingTestUnit extends SteppedTestUnit implements ExecutedInDiscovery {
    public ExecutingTestUnit(Description description, Collection<TestStep> steps) {
        super(description, steps, Optional.empty());
    }

}
