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

import java.util.ArrayList;
import java.util.List;

import org.pitest.MultipleTestGroup;
import org.pitest.extension.OrderStrategy;
import org.pitest.extension.TestUnit;

public class GroupLikedTypeOrderStrategy implements OrderStrategy {

  private final Class<?> type;

  public GroupLikedTypeOrderStrategy(final Class<?> type) {
    this.type = type;
  }

  public List<TestUnit> order(final List<TestUnit> tus) {
    final List<TestUnit> matchesType = new ArrayList<TestUnit>();
    final List<TestUnit> noMatch = new ArrayList<TestUnit>();

    for (final TestUnit each : tus) {
      if (parentOrChildIsOfType(each)) {
        matchesType.add(each);
      } else {
        noMatch.add(each);
      }
    }

    final List<TestUnit> ordered = new ArrayList<TestUnit>();
    if (!matchesType.isEmpty()) {
      final MultipleTestGroup mtg = new MultipleTestGroup(matchesType);
      ordered.add(mtg);
    }

    ordered.addAll(noMatch);
    return ordered;
  }

  private boolean parentOrChildIsOfType(final TestUnit tu) {
    if (this.type.isAssignableFrom(tu.description().getTestClass())) {
      return true;
    }

    for (final TestUnit each : tu) {
      if (parentOrChildIsOfType(each)) {
        return true;
      }
    }

    return false;

  }

}
