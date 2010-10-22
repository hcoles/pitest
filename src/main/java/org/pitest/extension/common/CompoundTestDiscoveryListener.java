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
package org.pitest.extension.common;

import java.util.Collection;

import org.pitest.extension.TestDiscoveryListener;
import org.pitest.extension.TestUnit;

public class CompoundTestDiscoveryListener implements TestDiscoveryListener {
  private final Collection<TestDiscoveryListener> children;

  public CompoundTestDiscoveryListener(
      final Collection<TestDiscoveryListener> children) {
    this.children = children;
  }

  public void enterClass(final Class<?> clazz) {
    for (final TestDiscoveryListener each : this.children) {
      each.enterClass(clazz);
    }

  }

  public void leaveClass(final Class<?> clazz) {
    for (final TestDiscoveryListener each : this.children) {
      each.leaveClass(clazz);
    }
  }

  public void reciveTests(final Collection<TestUnit> testUnits) {
    for (final TestDiscoveryListener each : this.children) {
      each.reciveTests(testUnits);
    }
  }

}
