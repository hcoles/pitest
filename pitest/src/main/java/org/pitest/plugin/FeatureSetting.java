package org.pitest.plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.Optional;

public final class FeatureSetting {

  private final String feature;
  private final ToggleStatus status;
  private final Map<String,List<String>> settings = new HashMap<>();

  public FeatureSetting(String feature, ToggleStatus status, Map<String, List<String>> settings) {
    this.feature = feature;
    this.status = status;
    this.settings.putAll(settings);
  }

  public String feature() {
    return this.feature;
  }

  public ToggleStatus status() {
    return this.status;
  }


  public boolean addsFeature() {
    return this.status == ToggleStatus.ACTIVATE;
  }

  public boolean removesFeature() {
    return this.status == ToggleStatus.DEACTIVATE;
  }

  public Optional<String> getString(String key) {
    if (this.settings.containsKey(key)) {
      final List<String> vals = getList(key);
      if (vals.size() > 1) {
        throw new IllegalArgumentException("More than one value supplied for " + key);
      }

      return Optional.ofNullable(vals.get(0));
    }
    return Optional.empty();
  }

  public List<String> getList(String key) {
    return this.settings.get(key);
  }

}
