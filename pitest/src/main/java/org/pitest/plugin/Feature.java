package org.pitest.plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class Feature {

  private final boolean onByDefault;
  private final boolean isInternal;
  private final boolean missing;
  private final String  name;
  private final String  description;
  private final List<FeatureParameter> params;

  private Feature(boolean onByDefault, boolean isInternal, boolean missing, String name, String description, List<FeatureParameter> params) {
    this.onByDefault = onByDefault;
    this.isInternal = isInternal;
    this.missing = missing;
    this.name = name;
    this.description = description;
    this.params = params;

  }

  public static Feature named(String name) {
    return new Feature(false, false, false, name.toLowerCase(Locale.ROOT), "", Collections.emptyList());
  }

  public Feature withOnByDefault(boolean onByDefault) {
    return new Feature(onByDefault, this.isInternal, this.missing, this.name, this.description, this.params);
  }

  public Feature asInternalFeature() {
    return new Feature(this.onByDefault, true, this.missing, this.name, this.description, this.params);
  }

  public Feature withDescription(String description) {
    return new Feature(this.onByDefault, this.isInternal, this.missing, this.name, description, this.params);
  }

  public Feature withParameter(FeatureParameter param) {
    final List<FeatureParameter> params = new ArrayList<>(this.params);
    params.add(param);
    return new Feature(this.onByDefault, this.isInternal, this.missing, this.name, this.description, params);
  }

  public Feature markMissing() {
    return new Feature(this.onByDefault, this.isInternal, true, this.name, description, this.params);
  }

  public String name() {
    return this.name;
  }

  public String description() {
    return this.description;
  }

  public boolean isOnByDefault() {
    return this.onByDefault;
  }

  public boolean isInternal() {
    return this.isInternal;
  }

  public boolean isMissing() {
    return this.missing;
  }

  public List<FeatureParameter> params() {
    return this.params;
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final Feature other = (Feature) obj;
    return Objects.equals(name, other.name);
  }


}
