package org.pitest.plugin;

import org.pitest.functional.FCollection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
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
    final List<T> factories = new ArrayList<>(filters);
    final Map<String, Collection<T>> featureMap = FCollection.bucket(factories, byFeatureName());

    final List<T> active = factories.stream()
            .filter(isOnByDefault())
            .collect(Collectors.toCollection(ArrayList::new));

    for ( final FeatureSetting each : features ) {
      final Collection<T> providers = featureMap.get(each.feature().toLowerCase());
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

    return active;
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