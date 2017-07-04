package org.pitest.plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.pitest.functional.F;
import org.pitest.functional.FCollection;

public class FeatureSelector<T extends ProvidesFeature> {

  private final Map<String, Collection<FeatureSetting>> settings;
  private final List<T> active;
  
  public FeatureSelector(List<FeatureSetting> features, Collection<T> filters) {
    settings = FCollection.bucket(features, byFeature());
    active = selectFeatures(features, filters);
  }
  
  public  List<T> getActiveFeatures() {
    return active;
  }
  
  public FeatureSetting getSettingForFeature(String feature) {
    FeatureSetting conf = null;
    Collection<FeatureSetting> groupedSettings = settings.get(feature);
    if (groupedSettings != null) {
      conf = groupedSettings.iterator().next();
    }
    return conf;
  }
  
   public List<T> selectFeatures(List<FeatureSetting> features, Collection<T> filters) {
    List<T> factories = new ArrayList<T>(filters);
    Map<String, Collection<T>> featureMap = FCollection.bucket(factories, byFeatureName());
    
    List<T> active = FCollection.filter(factories, isOnByDefault());
    
    for ( FeatureSetting each : features ) {
      Collection<T> providers = featureMap.get(each.feature());
      if (providers == null || providers.isEmpty()) {
        throw new IllegalArgumentException("Pitest and its installed plugins do not recognise the feature " + each.feature());
      }
      
      if (each.addsFeature()) {
        active.addAll(providers);
      }
      
      if (each.removesFeature()) {
        active.removeAll(providers);
      }
    }
    
    return active;
  }

  private F<T, Boolean> isOnByDefault() {
    return new  F<T, Boolean>() {
      @Override
      public Boolean apply(T a) {
        return a.provides().isOnByDefault();
      }
    };
  }

  private F<T, String> byFeatureName() {
    return new  F<T, String>() {
      @Override
      public String apply(T a) {
        return a.provides().name();
      }
    };
  }
  
  private F<FeatureSetting, String> byFeature() {
    return new  F<FeatureSetting, String>() {
      @Override
      public String apply(FeatureSetting a) {
        return a.feature();
      }
    };
  }

}