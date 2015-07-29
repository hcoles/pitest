/*
 * Copyright 2011 Henry Coles
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
package org.pitest.testapi;

import java.util.Collections;
import java.util.List;

public class TestGroupConfig {

  private final List<String> excludedGroups;
  private final List<String> includedGroups;

  public TestGroupConfig(final List<String> excludedGroups,
      final List<String> includedGroups) {
    this.excludedGroups = (excludedGroups != null ? excludedGroups
        : Collections.<String> emptyList());
    this.includedGroups = (includedGroups != null ? includedGroups
        : Collections.<String> emptyList());
  }

  public TestGroupConfig() {
    this(null, null);
  }

  public List<String> getExcludedGroups() {
    return this.excludedGroups;
  }

  public List<String> getIncludedGroups() {
    return this.includedGroups;
  }

  @Override
  public String toString() {
    return "TestGroupConfig [excludedGroups=" + this.excludedGroups
        + ", includedGroups=" + this.includedGroups + "]";
  }

}
