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
    Collection<? extends TestPluginFactory> cls = ServiceLoader.load(TestPluginFactory.class, this.loader);
    System.out.println("Collection<? extends TestPluginFactory> : " + cls);
    return cls;
  }

  Collection<? extends MutationEngineFactory> findMutationEngines() {
    return ServiceLoader.load(MutationEngineFactory.class, this.loader);
  }

}
