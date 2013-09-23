package org.pitest.mutationtest.commandline;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.pitest.mutationtest.report.html.HtmlReportFactory;

public class PluginFilterTest {

  @Test
  public void shouldExcludeHtmlReportPlugin() {
    PluginFilter testee = new PluginFilter();
    String pluginLocation = HtmlReportFactory.class.getProtectionDomain().getCodeSource().getLocation().getFile();
    assertFalse(testee.apply(pluginLocation));
  }
  
  @Test
  public void shouldAllowUnrecognisedClasspathElements() {
    PluginFilter testee = new PluginFilter();
    assertTrue(testee.apply("foo"));
  }

}
