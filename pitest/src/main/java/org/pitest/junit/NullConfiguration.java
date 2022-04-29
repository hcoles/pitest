package org.pitest.junit;

import org.pitest.help.PitHelpError;
import org.pitest.testapi.Configuration;
import org.pitest.testapi.TestSuiteFinder;
import org.pitest.testapi.TestUnitFinder;

import java.util.Collections;
import java.util.Optional;

public class NullConfiguration implements Configuration {
    @Override
    public TestUnitFinder testUnitFinder() {
        return c -> Collections.emptyList();
    }

    @Override
    public TestSuiteFinder testSuiteFinder() {
        return c -> Collections.emptyList();
    }

    @Override
    public Optional<PitHelpError> verifyEnvironment() {
        return Optional.empty();
    }
}
