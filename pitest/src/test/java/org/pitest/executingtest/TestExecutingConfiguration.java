package org.pitest.executingtest;

import org.pitest.extension.common.NoTestSuiteFinder;
import org.pitest.help.PitHelpError;
import org.pitest.testapi.Configuration;
import org.pitest.testapi.TestSuiteFinder;
import org.pitest.testapi.TestUnitFinder;

import java.util.Optional;

public class TestExecutingConfiguration implements Configuration {

    @Override
    public TestUnitFinder testUnitFinder() {
        return new ExecutingTestFinder();
    }

    @Override
    public TestSuiteFinder testSuiteFinder() {
        return new NoTestSuiteFinder();
    }

    @Override
    public Optional<PitHelpError> verifyEnvironment() {
        return Optional.empty();
    }

}
