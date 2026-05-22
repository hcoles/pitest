package org.pitest.mutationtest.build;

import org.junit.Test;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.plugin.Feature;
import org.pitest.plugin.FeatureSetting;
import org.pitest.plugin.ToggleStatus;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.pitest.mutationtest.engine.MutationDetailsMother.aMutationDetail;

public class CompoundProjectMutationInterceptorFactoryTest {

  @Test
  public void includesFiltersEnabledByDefault() {
    ProjectMutationInterceptorFactory removeAll = factoryFor(Feature.named("enabled").withOnByDefault(true));
    CompoundProjectMutationFilterFactory testee = new CompoundProjectMutationFilterFactory(
        Collections.emptyList(), List.of(removeAll));

    ProjectMutationInterceptor filter = testee.createFilter(null, null, null, null, null, null);
    Collection<MutationDetails> mutations = aMutationDetail().build(3);

    assertThat(filter.intercept(mutations)).isEmpty();
  }

  @Test
  public void excludesFiltersDisabledByDefault() {
    ProjectMutationInterceptorFactory removeAll = factoryFor(Feature.named("disabled").withOnByDefault(false));
    CompoundProjectMutationFilterFactory testee = new CompoundProjectMutationFilterFactory(
        Collections.emptyList(), List.of(removeAll));

    ProjectMutationInterceptor filter = testee.createFilter(null, null, null, null, null, null);
    Collection<MutationDetails> mutations = aMutationDetail().build(3);

    assertThat(filter.intercept(mutations)).hasSize(3);
  }

  @Test
  public void canActivateOptInFilters() {
    ProjectMutationInterceptorFactory removeAll = factoryFor(Feature.named("optin").withOnByDefault(false));
    FeatureSetting activate = new FeatureSetting("optin", ToggleStatus.ACTIVATE, new HashMap<>());
    CompoundProjectMutationFilterFactory testee = new CompoundProjectMutationFilterFactory(
            List.of(activate), List.of(removeAll));

    ProjectMutationInterceptor filter = testee.createFilter(null, null, null, null, null, null);
    Collection<MutationDetails> mutations = aMutationDetail().build(3);

    assertThat(filter.intercept(mutations)).isEmpty();
  }

  @Test
  public void canDeactivateDefaultOnFilters() {
    ProjectMutationInterceptorFactory removeAll = factoryFor(Feature.named("enabled").withOnByDefault(true));
    FeatureSetting deactivate = new FeatureSetting("enabled", ToggleStatus.DEACTIVATE, new HashMap<>());
    CompoundProjectMutationFilterFactory testee = new CompoundProjectMutationFilterFactory(
            List.of(deactivate), List.of(removeAll));

    ProjectMutationInterceptor filter = testee.createFilter(null, null, null, null, null,null);
    Collection<MutationDetails> mutations = aMutationDetail().build(3);

    assertThat(filter.intercept(mutations)).hasSize(3);
  }

  private ProjectMutationInterceptorFactory factoryFor(Feature feature) {
    return new ProjectMutationInterceptorFactory() {
      @Override
      public ProjectMutationInterceptor createInterceptor(InterceptorParameters params) {
        return new ProjectMutationInterceptor() {
          @Override
          public Collection<MutationDetails> intercept(Collection<MutationDetails> mutations) {
              return List.of();
          }

          @Override
          public InterceptorType type() {
            return InterceptorType.FILTER;
          }
        };
      }

      @Override
      public Feature provides() {
        return feature;
      }

      @Override
      public String description() {
        return feature.name();
      }
    };
  }
}
