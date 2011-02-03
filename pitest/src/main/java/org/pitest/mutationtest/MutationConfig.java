/*
 * Copyright 2010 Henry Coles
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, 
 * software distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and limitations under the License. 
 */
package org.pitest.mutationtest;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationEngine;
import org.pitest.mutationtest.report.MutationTestSummaryData.MutationTestType;

public final class MutationConfig {

  private final int              threshold;
  private final List<String>     jvmArgs;
  private final MutationTestType type;
  private final MutationEngine   engine;

  public MutationConfig(final MutationEngine engine,
      final MutationTestType type, final int threshold, final String[] jvmArgs) {
    this(engine, type, threshold, Arrays.asList(jvmArgs));
  }

  public MutationConfig(final MutationTestType type, final int threshold) {
    this(DefaultMutationConfigFactory.makeDefaultEngine(), type, threshold,
        Collections.<String> emptyList());
  }

  public MutationConfig(final MutationEngine engine,
      final MutationTestType type, final int threshold,
      final List<String> jvmArgs) {
    this.type = type;
    this.threshold = threshold;
    this.jvmArgs = jvmArgs;
    this.engine = engine;
  }

  public Mutater createMutator(final ClassLoader loader) {
    return this.engine.createMutator(this, loader);
  }

  public int getThreshold() {
    return this.threshold;
  }

  public List<String> getJVMArgs() {
    return this.jvmArgs;
  }

  public MutationTestType getRunType() {
    return this.type;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((this.engine == null) ? 0 : this.engine.hashCode());
    result = prime * result
        + ((this.jvmArgs == null) ? 0 : this.jvmArgs.hashCode());
    result = prime * result + this.threshold;
    result = prime * result + ((this.type == null) ? 0 : this.type.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final MutationConfig other = (MutationConfig) obj;
    if (this.engine == null) {
      if (other.engine != null) {
        return false;
      }
    } else if (!this.engine.equals(other.engine)) {
      return false;
    }
    if (this.jvmArgs == null) {
      if (other.jvmArgs != null) {
        return false;
      }
    } else if (!this.jvmArgs.equals(other.jvmArgs)) {
      return false;
    }
    if (this.threshold != other.threshold) {
      return false;
    }
    if (this.type == null) {
      if (other.type != null) {
        return false;
      }
    } else if (!this.type.equals(other.type)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "MutationConfig [engine=" + this.engine + ", jvmArgs="
        + this.jvmArgs + ", threshold=" + this.threshold + ", type="
        + this.type + "]";
  }

  public Collection<String> getMutatorNames() {
    return this.engine.getMutatorNames();
  }

}
