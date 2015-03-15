package org.pitest.mutationtest.commandline;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.pitest.mutationtest.config.PluginServices;
import org.pitest.mutationtest.engine.gregor.GregorMutationEngine;
import org.pitest.mutationtest.report.html.HtmlReportFactory;
import org.pitest.util.IsolationUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class PluginFilterTest {

  private PluginFilter testee = new PluginFilter(new PluginServices(
                                  IsolationUtils.getContextClassLoader()));

  @Test
  public void shouldExcludeHtmlReportPlugin() throws Exception {
    String pluginLocation = getClassLocationAsCanonicalPath(HtmlReportFactory.class);
    assertFalse(testee.apply(pluginLocation));
  }

  @Test
  public void shouldIncludeMutationEngine() throws Exception {
    String pluginLocation = getClassLocationAsCanonicalPath(GregorMutationEngine.class);
    assertTrue(testee.apply(pluginLocation));
  }

  private String getClassLocationAsCanonicalPath(Class<?> clazz) throws URISyntaxException, IOException {
    return new File(clazz.getProtectionDomain().getCodeSource().getLocation().toURI()).getCanonicalPath();
  }
}
