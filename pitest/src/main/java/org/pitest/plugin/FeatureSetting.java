package org.pitest.plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pitest.functional.Option;

public final class FeatureSetting {

  private final String feature;
  private final ToggleStatus status;
  private final Map<String,List<String>> settings = new HashMap<String,List<String>>();
  
  public FeatureSetting(String feature, ToggleStatus status, Map<String, List<String>> settings) {
    this.feature = feature;
    this.status = status;
    this.settings.putAll(settings);
  }

  public String feature() {
    return feature;
  }

  public ToggleStatus status() {
    return status;
  }

  
  public boolean addsFeature() {
    return status == ToggleStatus.ACTIVATE;
  }

  public boolean removesFeature() {
    return status == ToggleStatus.DEACTIVATE;
  }
  
  public Option<String> getString(String key) {
    if (settings.containsKey(key)) {
      List<String> vals = getList(key);
      if (vals.size() > 1) {
        throw new IllegalArgumentException("More than one value supplied for " + key);
      }
      
      return Option.some(vals.get(0));
    }
    return Option.none();
  }
  
  public List<String> getList(String key) {
    return settings.get(key);
  }
  
}
