package org.pitest.mutationtest.engine.gregor;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.pitest.plugin.ClientClasspathPlugin;
import org.pitest.util.ServiceLoader;

public class GregorPluginTest {

  @Test
  public void shouldProvideAClientClasspathPlugin() {
    Iterable<ClientClasspathPlugin> plugins = ServiceLoader.load(ClientClasspathPlugin.class);
    assertTrue(plugins.iterator().hasNext());
  }

}
