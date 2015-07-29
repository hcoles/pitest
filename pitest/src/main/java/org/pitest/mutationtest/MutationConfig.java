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

import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationEngine;
import org.pitest.process.LaunchOptions;

public final class MutationConfig {

  private final LaunchOptions  launchOptions;
  private final MutationEngine engine;

  public MutationConfig(final MutationEngine engine,
      final LaunchOptions launchOptions) {
    this.launchOptions = launchOptions;
    this.engine = engine;
  }

  public Mutater createMutator(final ClassByteArraySource source) {
    return this.engine.createMutator(source);
  }

  public MutationEngine getEngine() {
    return this.engine;
  }

  public LaunchOptions getLaunchOptions() {
    return this.launchOptions;
  }

  @Override
  public boolean equals(final Object rhs) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int hashCode() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String toString() {
    return "MutationConfig [launchOptions=" + this.launchOptions + ", engine="
        + this.engine + "]";
  }

}
