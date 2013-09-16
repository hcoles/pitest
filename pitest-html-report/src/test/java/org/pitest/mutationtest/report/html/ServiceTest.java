package org.pitest.mutationtest.report.html;

import static org.junit.Assert.assertNotNull;

import java.util.Arrays;

import org.junit.Test;
import org.pitest.mutationtest.ReportOptions;
import org.pitest.mutationtest.SettingsFactory;

public class ServiceTest {

  @Test
  public void shouldProvideListenerNamedHTML() {
    final ReportOptions options = new ReportOptions();

    final SettingsFactory factory = new SettingsFactory(options);

    options.addOutputFormats(Arrays.asList("HTML"));
    assertNotNull(factory.createListener());
  }

}
