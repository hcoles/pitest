package org.pitest.mutationtest.config;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.mutationtest.engine.gregor.config.GregorEngineFactory;
import org.pitest.mutationtest.filter.LimitNumberOfMutationsPerClassFilterFactory;
import org.pitest.mutationtest.report.csv.CSVReportFactory;

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

  private static F<Object, Boolean> theClass(final Class<?> clss) {
    return new F<Object, Boolean>() {
      @Override
      public Boolean apply(Object a) {
        return a.getClass().equals(clss);
      }

    };
  }

}
