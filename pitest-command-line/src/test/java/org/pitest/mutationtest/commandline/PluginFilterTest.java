package org.pitest.mutationtest.commandline;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Test;
import org.pitest.mutationtest.config.PluginServices;
import org.pitest.mutationtest.engine.gregor.GregorMutationEngine;
import org.pitest.mutationtest.report.html.HtmlReportFactory;

public class PluginFilterTest {

  private final PluginFilter testee = new PluginFilter(PluginServices.makeForContextLoader());

  @Test
  public void shouldExcludeHtmlReportPlugin() throws Exception {
    final String pluginLocation = getClassLocationAsCanonicalPath(HtmlReportFactory.class);
    assertThat(this.testee.test(pluginLocation)).isFalse();
  }

  @Test
  public void shouldIncludeMutationEngine() throws Exception {
    final String pluginLocation = getClassLocationAsCanonicalPath(GregorMutationEngine.class);
    assertThat(this.testee.test(pluginLocation)).isTrue();
  }

  private String getClassLocationAsCanonicalPath(Class<?> clazz)
      throws URISyntaxException, IOException {
    return new File(clazz.getProtectionDomain().getCodeSource().getLocation()
        .toURI()).getCanonicalPath();
  }
}
