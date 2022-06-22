package org.pitest.executingtest;

import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.testapi.Configuration;
import org.pitest.testapi.TestGroupConfig;
import org.pitest.testapi.TestPluginFactory;

import java.util.Collection;

/**
 * Toy test plugin that executes tests during discovery in the same
 * manner as JUnit 5. Exists purely to allow integration testing.
 */
public class ExecutingTestPlugin implements TestPluginFactory {

    @Override
    public String description() {
        return "Executing test plugin for testing";
    }

    @Override
    public Configuration createTestFrameworkConfiguration(TestGroupConfig config,
                                                          ClassByteArraySource source,
                                                          Collection<String> excludedRunners,
                                                          Collection<String> includedMethods) {
        return new TestExecutingConfiguration();
    }

    @Override
    public String name() {
        return "ExecutingPluginForTesting";
    }

}