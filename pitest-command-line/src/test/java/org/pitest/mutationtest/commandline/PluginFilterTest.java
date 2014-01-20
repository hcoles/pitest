package org.pitest.mutationtest.commandline;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.pitest.mutationtest.config.PluginServices;
import org.pitest.mutationtest.engine.gregor.GregorMutationEngine;
import org.pitest.mutationtest.report.html.HtmlReportFactory;
import org.pitest.util.IsolationUtils;

public class PluginFilterTest {

  private PluginFilter testee = new PluginFilter(new PluginServices(
                                  IsolationUtils.getContextClassLoader()));

  @Test
  public void shouldExcludeHtmlReportPlugin() {
    String pluginLocation = HtmlReportFactory.class.getProtectionDomain()
        .getCodeSource().getLocation().getFile();
    assertFalse(testee.apply(pluginLocation));
  }

  @Test
  public void shouldIncludeMutationEngine() {
    String pluginLocation = GregorMutationEngine.class.getProtectionDomain()
        .getCodeSource().getLocation().getFile();
    assertTrue(testee.apply(pluginLocation));
  }

}
