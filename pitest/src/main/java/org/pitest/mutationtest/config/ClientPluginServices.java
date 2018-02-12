package org.pitest.mutationtest.config;

import java.util.Collection;

import org.pitest.mutationtest.MutationEngineFactory;
import org.pitest.testapi.TestPluginFactory;
import org.pitest.util.IsolationUtils;
import org.pitest.util.ServiceLoader;

public class ClientPluginServices {
  private final ClassLoader loader;

  public ClientPluginServices(ClassLoader loader) {
    this.loader = loader;
  }

  public static ClientPluginServices makeForContextLoader() {
    return new ClientPluginServices(IsolationUtils.getContextClassLoader());
  }


  Collection<? extends TestPluginFactory> findTestFrameworkPlugins() {
    return ServiceLoader.load(TestPluginFactory.class, this.loader);
  }

  Collection<? extends MutationEngineFactory> findMutationEngines() {
    return ServiceLoader.load(MutationEngineFactory.class, this.loader);
  }

}
