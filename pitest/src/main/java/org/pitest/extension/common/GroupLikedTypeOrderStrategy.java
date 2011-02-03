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

import static org.pitest.util.Functions.isAssignableFrom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.pitest.Description;
import org.pitest.MultipleTestGroup;
import org.pitest.extension.OrderStrategy;
import org.pitest.extension.TestUnit;

public class GroupLikedTypeOrderStrategy implements OrderStrategy {

  private final Class<?>      UNGROUPED              = null;

  private final Set<Class<?>> mutuallyExclusiveTypes = new HashSet<Class<?>>();

  public GroupLikedTypeOrderStrategy(final Class<?>... types) {
    this.mutuallyExclusiveTypes.addAll(Arrays.asList(types));
  }

  public List<TestUnit> order(final List<TestUnit> tus) {
    final Map<Class<?>, List<TestUnit>> matchesType = createMapOfMutallyExclusiveTypesToTestUnits();

    // add null for all the other classes
    matchesType.put(this.UNGROUPED, new ArrayList<TestUnit>());

    mapTestUnitsToTheirParentType(tus, matchesType);

    final List<TestUnit> groups = createGroupForEachMutuallyExclusiveType(matchesType);

    final List<TestUnit> ungroupedTestUnits = matchesType.get(this.UNGROUPED);

    groups.addAll(ungroupedTestUnits);

    return groups;
  }

  private List<TestUnit> createGroupForEachMutuallyExclusiveType(
      final Map<Class<?>, List<TestUnit>> matchesType) {
    final List<TestUnit> groups = new ArrayList<TestUnit>();
    for (final Class<?> each : this.mutuallyExclusiveTypes) {
      final List<TestUnit> grouped = matchesType.get(each);
      if ((!grouped.isEmpty())) {
        final MultipleTestGroup mtg = new MultipleTestGroup(grouped);
        groups.add(mtg);
      }
    }
    return groups;
  }

  private void mapTestUnitsToTheirParentType(final List<TestUnit> tus,
      final Map<Class<?>, List<TestUnit>> matchesType) {
    for (final TestUnit each : tus) {
      final List<TestUnit> bucket = matchesType
          .get(parentOrChildIsAssignableFromType(each));
      bucket.add(each);
    }
  }

  private Map<Class<?>, List<TestUnit>> createMapOfMutallyExclusiveTypesToTestUnits() {
    final Map<Class<?>, List<TestUnit>> matchesType = new HashMap<Class<?>, List<TestUnit>>();
    for (final Class<?> each : this.mutuallyExclusiveTypes) {
      matchesType.put(each, new ArrayList<TestUnit>());
    }
    return matchesType;
  }

  private Class<?> isAssignableFromTargetType(final Description d) {

    for (final Class<?> mutuallyExclusive : this.mutuallyExclusiveTypes) {
      if (d.contains(isAssignableFrom(mutuallyExclusive))) {
        return mutuallyExclusive;
      }

    }
    return null;
  }

  private Class<?> parentOrChildIsAssignableFromType(final TestUnit tu) {
    final Class<?> type = isAssignableFromTargetType(tu.getDescription());
    if (type != null) {
      return type;
    }

    for (final TestUnit each : tu) {
      final Class<?> t = parentOrChildIsAssignableFromType(each);
      if (t != null) {
        return t;
      }
    }

    return null;

  }

}
