package org.pitest.testapi;

import java.util.Collection;

import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.plugin.ClientClasspathPlugin;

public interface TestPluginFactory extends ClientClasspathPlugin {

  Configuration createTestFrameworkConfiguration(TestGroupConfig config,
      ClassByteArraySource source, Collection<String> excludedRunners, Collection<String> includedTestMethods);

  String name();

}
