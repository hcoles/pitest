package org.pitest.mutationtest.config;

import org.junit.Test;
import org.mockito.Mockito;
import org.pitest.functional.FCollection;
import org.pitest.mutationtest.MutationResultListenerFactory;
import org.pitest.mutationtest.build.InterceptorParameters;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.build.MutationInterceptorFactory;
import org.pitest.mutationtest.engine.gregor.config.GregorEngineFactory;
import org.pitest.mutationtest.filter.LimitNumberOfMutationsPerClassFilterFactory;
import org.pitest.mutationtest.report.csv.CSVReportFactory;
import org.pitest.plugin.Feature;

import java.util.function.Predicate;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class PluginServicesTest {

  PluginServices testee = PluginServices.makeForContextLoader();

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

  @Test
  public void missingFeaturesSelectedWhenOnlyOnesAvailable() {
    MissingFeature missing = new MissingFeature("foo");
    testee = createWithFeatures(missing);

    assertThat(testee.findFeatures()).containsExactly(missing);
  }

  @Test
  public void missingFeaturesRemovedWhenImplementationAvailable() {
    MissingFeature missing = new MissingFeature("foo");
    ExampleFeature present = new ExampleFeature("foo");
    ExampleFeature alsoMissing = new ExampleFeature("bar");
    ExampleFeature presentAsWell = new ExampleFeature("moo");
    testee = createWithFeatures(missing, present, alsoMissing, presentAsWell);

    assertThat(testee.findFeatures()).containsExactlyInAnyOrder(present, alsoMissing, presentAsWell);
  }

  @Test
  public void missingInterceptorsRemovedWhenImplementationAvailable() {
    MutationInterceptorFactory missing = new MissingFeature("foo");
    MutationInterceptorFactory present = new ExampleFeature("foo");
    MutationInterceptorFactory alsoMissing = new ExampleFeature("bar");
    MutationInterceptorFactory presentAsWell = new ExampleFeature("moo");
    testee = createWithFeatures(missing, present, alsoMissing, presentAsWell);

    assertThat(testee.findInterceptors()).containsExactlyInAnyOrder(present, alsoMissing, presentAsWell);
  }

  private PluginServices createWithFeatures(MutationInterceptorFactory ... features) {
    Services loader = Mockito.mock(Services.class);
    when(loader.load(MutationInterceptorFactory.class)).thenReturn(asList(features));
    return new PluginServices(loader);
  }

  private static Predicate<Object> theClass(final Class<?> clss) {
    return a -> a.getClass().equals(clss);
  }


  static class ExampleFeature implements MutationInterceptorFactory {

    final String name;

    ExampleFeature(String name) {
      this.name = name;
    }

    @Override
    public Feature provides() {
      return Feature.named(name);
    }

    @Override
    public MutationInterceptor createInterceptor(InterceptorParameters params) {
      return null;
    }

    @Override
    public String description() {
      return null;
    }
  }

  static class MissingFeature extends ExampleFeature {

    MissingFeature(String name) {
      super(name);
    }

    @Override
    public Feature provides() {
      return super.provides().markMissing();
    }

  }
}

