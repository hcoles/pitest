package org.pitest.mutationtest.report.html;

import static org.junit.Assert.assertNotNull;

import java.util.Arrays;

import org.junit.Test;
import org.pitest.mutationtest.config.PluginServices;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.mutationtest.config.SettingsFactory;

public class ServiceTest {

  @Test
  public void shouldProvideListenerNamedHTML() {
    final ReportOptions options = new ReportOptions();

    final SettingsFactory factory = new SettingsFactory(options, PluginServices.makeForContextLoader());

    options.addOutputFormats(Arrays.asList("HTML"));
    assertNotNull(factory.createListener());
  }

}
