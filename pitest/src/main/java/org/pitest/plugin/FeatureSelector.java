package org.pitest.plugin;

import org.pitest.functional.FCollection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FeatureSelector<T extends ProvidesFeature> {

  private final Map<String, Collection<FeatureSetting>> settings;
  private final List<T> active;

  public FeatureSelector(List<FeatureSetting> features, Collection<T> filters) {
    this.settings = FCollection.bucket(features, byFeature());
    this.active = selectFeatures(features, filters);
  }

  public List<T> getActiveFeatures() {
    return this.active;
  }

  public FeatureSetting getSettingForFeature(String feature) {
    FeatureSetting conf = null;
    final Collection<FeatureSetting> groupedSettings = this.settings.get(feature.toLowerCase());
    if (groupedSettings != null) {
      conf = groupedSettings.iterator().next();
    }
    return conf;
  }

  private List<T> selectFeatures(List<FeatureSetting> features, Collection<T> filters) {
    List<T> factories = new ArrayList<>(filters);
    Map<String, Collection<T>> featureMap = FCollection.bucket(factories, byFeatureName());

    Set<T> active = factories.stream()
            .filter(isOnByDefault())
            .collect(Collectors.toSet());

    for (FeatureSetting each : features) {
      Collection<T> providers = featureMap.get(each.feature().toLowerCase());
      if ((providers == null) || providers.isEmpty()) {
        continue;
      }

      if (each.addsFeature()) {
        active.addAll(providers);
      }

      if (each.removesFeature()) {
        Collection<T> removable = providers.stream()
                .filter(p -> !p.provides().isInternal())
                .collect(Collectors.toList());
        active.removeAll(removable);
      }
    }

    return active.stream()
            .sorted(Comparator.comparing(ProvidesFeature::provides))
            .collect(Collectors.toList());
  }

  private Predicate<T> isOnByDefault() {
    return a -> a.provides().isOnByDefault();
  }

  private Function<T, String> byFeatureName() {
    return a -> a.provides().name().toLowerCase();
  }

  private Function<FeatureSetting, String> byFeature() {
    return a -> a.feature().toLowerCase();
  }

}