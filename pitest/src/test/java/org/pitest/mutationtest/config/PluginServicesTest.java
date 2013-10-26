package org.pitest.mutationtest.config;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.mutationtest.config.PluginServices;
import org.pitest.mutationtest.engine.gregor.config.GregorEngineFactory;
import org.pitest.mutationtest.filter.LimitNumberOfMutationsPerClassFilterFactory;
import org.pitest.mutationtest.report.csv.CSVReportFactory;

public class PluginServicesTest {

  @Test
  public void shouldListDefaultEngineAsClientClasspathPlugin() {
    assertTrue(FCollection.contains(PluginServices.findClientClasspathPlugins(), theClass(GregorEngineFactory.class)));
  }

  @Test
  public void shouldListCSVReportAsToolClasspathPlugin() {
    assertTrue(FCollection.contains(PluginServices.findToolClasspathPlugins(), theClass(CSVReportFactory.class)));
  }
  
  @Test
  public void shouldListDefaultMutationFilterAsToolClasspathPlugin() {
    assertTrue(FCollection.contains(PluginServices.findToolClasspathPlugins(), theClass(LimitNumberOfMutationsPerClassFilterFactory.class)));
  }
  
  private static F<Object, Boolean> theClass(final Class<?> clss) {
    return new F<Object, Boolean>() {
      public Boolean apply(Object a) {
        return a.getClass().equals(clss);
      }
      
    };
  }

}
