package org.pitest.mutationtest.config;

import org.junit.Test;
import org.pitest.functional.FCollection;
import org.pitest.mutationtest.MutationResultListenerFactory;
import org.pitest.mutationtest.build.MutationInterceptorFactory;
import org.pitest.mutationtest.engine.gregor.config.GregorEngineFactory;
import org.pitest.mutationtest.filter.LimitNumberOfMutationsPerClassFilterFactory;
import org.pitest.mutationtest.report.csv.CSVReportFactory;

import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

public class PluginServicesTest {

  private final PluginServices testee = PluginServices.makeForContextLoader();

  @Test
  public void shouldListDefaultEngineAsClientClasspathPlugin() {
    assertTrue(FCollection.contains(this.testee.findClientClasspathPlugins(),
        theClass(GregorEngineFactory.class)));
  }

  @Test
  public void shouldListCSVReportAsToolClasspathPlugin() {
    assertTrue(FCollection.contains(this.testee.findToolClasspathPlugins(),
        theClass(CSVReportFactory.class)));
  }

  @Test
  public void shouldListDefaultMutationFilterAsToolClasspathPlugin() {
    assertTrue(FCollection.contains(this.testee.findToolClasspathPlugins(),
        theClass(LimitNumberOfMutationsPerClassFilterFactory.class)));
  }

  @Test
  public void shouldListAllTypesOfFeature() {
    assertThat(testee.findFeatures()).hasAtLeastOneElementOfType(MutationInterceptorFactory.class);
    assertThat(testee.findFeatures()).hasAtLeastOneElementOfType(MutationResultListenerFactory.class);
  }

  private static Predicate<Object> theClass(final Class<?> clss) {
    return a -> a.getClass().equals(clss);
  }

}
