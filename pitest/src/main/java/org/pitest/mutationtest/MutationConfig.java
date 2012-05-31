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

import java.util.Collection;
import java.util.List;

import org.pitest.internal.ClassByteArraySource;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationEngine;

public final class MutationConfig {

  private final List<String>   jvmArgs;
  private final MutationEngine engine;

  public MutationConfig(final MutationEngine engine, final List<String> jvmArgs) {
    this.jvmArgs = jvmArgs;
    this.engine = engine;
  }

  public Mutater createMutator(final ClassByteArraySource source) {
    return this.engine.createMutator(source);
  }

  public MutationEngine getEngine() {
    return this.engine;
  }
  
  public List<String> getJVMArgs() {
    return this.jvmArgs;
  }

  public Collection<String> getMutatorNames() {
    return this.engine.getMutatorNames();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((this.engine == null) ? 0 : this.engine.hashCode());
    result = prime * result
        + ((this.jvmArgs == null) ? 0 : this.jvmArgs.hashCode());
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
    return true;
  }

  @Override
  public String toString() {
    return "MutationConfig [jvmArgs=" + this.jvmArgs + ", engine="
        + this.engine + "]";
  }

}
