package org.pitest.mutationtest.build;

import org.junit.Test;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.plugin.Feature;
import org.pitest.plugin.FeatureSetting;
import org.pitest.plugin.ToggleStatus;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.pitest.mutationtest.engine.MutationDetailsMother.aMutationDetail;

public class CompoundProjectMutationFilterFactoryTest {

  @Test
  public void includesFiltersEnabledByDefault() {
    ProjectMutationFilterFactory removeAll = factoryFor(Feature.named("enabled").withOnByDefault(true), ms -> Collections.emptyList());
    CompoundProjectMutationFilterFactory testee = new CompoundProjectMutationFilterFactory(
        Collections.emptyList(), Arrays.asList(removeAll));

    ProjectMutationFilter filter = testee.createFilter(null, null, null, null, null);
    Collection<MutationDetails> mutations = aMutationDetail().build(3);

    assertThat(filter.filter(mutations)).isEmpty();
  }

  @Test
  public void excludesFiltersDisabledByDefault() {
    ProjectMutationFilterFactory removeAll = factoryFor(Feature.named("disabled").withOnByDefault(false), ms -> Collections.emptyList());
    CompoundProjectMutationFilterFactory testee = new CompoundProjectMutationFilterFactory(
        Collections.emptyList(), Arrays.asList(removeAll));

    ProjectMutationFilter filter = testee.createFilter(null, null, null, null, null);
    Collection<MutationDetails> mutations = aMutationDetail().build(3);

    assertThat(filter.filter(mutations)).hasSize(3);
  }

  @Test
  public void canActivateOptInFilters() {
    ProjectMutationFilterFactory removeAll = factoryFor(Feature.named("optin").withOnByDefault(false), ms -> Collections.emptyList());
    FeatureSetting activate = new FeatureSetting("optin", ToggleStatus.ACTIVATE, new HashMap<>());
    CompoundProjectMutationFilterFactory testee = new CompoundProjectMutationFilterFactory(
        Arrays.asList(activate), Arrays.asList(removeAll));

    ProjectMutationFilter filter = testee.createFilter(null, null, null, null, null);
    Collection<MutationDetails> mutations = aMutationDetail().build(3);

    assertThat(filter.filter(mutations)).isEmpty();
  }

  @Test
  public void canDeactivateDefaultOnFilters() {
    ProjectMutationFilterFactory removeAll = factoryFor(Feature.named("enabled").withOnByDefault(true), ms -> Collections.emptyList());
    FeatureSetting deactivate = new FeatureSetting("enabled", ToggleStatus.DEACTIVATE, new HashMap<>());
    CompoundProjectMutationFilterFactory testee = new CompoundProjectMutationFilterFactory(
        Arrays.asList(deactivate), Arrays.asList(removeAll));

    ProjectMutationFilter filter = testee.createFilter(null, null, null, null, null);
    Collection<MutationDetails> mutations = aMutationDetail().build(3);

    assertThat(filter.filter(mutations)).hasSize(3);
  }

  private ProjectMutationFilterFactory factoryFor(Feature feature, ProjectMutationFilter filter) {
    return new ProjectMutationFilterFactory() {
      @Override
      public ProjectMutationFilter createFilter(InterceptorParameters params) {
        return filter;
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
