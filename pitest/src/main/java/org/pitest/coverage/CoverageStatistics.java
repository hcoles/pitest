/*
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

package org.pitest.coverage;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.pitest.PitError;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.Option;

public class CoverageStatistics {

  private int                                 classIdCount                  = 0;
  private final Map<Integer, ClassStatistics> classStatisticsInClassIdOrder = new ConcurrentHashMap<Integer, ClassStatistics>();

  public synchronized void clearCoverageStats() {
    for (final Entry<Integer, ClassStatistics> each : this.classStatisticsInClassIdOrder
        .entrySet()) {
      this.classStatisticsInClassIdOrder.put(each.getKey(),
          new ClassStatistics(each.getValue().getClassName()));

    }
  }

  public synchronized int registerClass(final String className) { // NO_UCD
    final int id = this.classIdCount;
    this.classIdCount++;
    this.classStatisticsInClassIdOrder.put(id, new ClassStatistics(className));
    return id;
  }

  public void visitLine(final int classId, final int lineId) {
    this.getClassStatistics(classId).registerLineVisit(lineId);
  }

  private ClassStatistics getClassStatistics(final int id) {
    final ClassStatistics cs = this.classStatisticsInClassIdOrder.get(id);
    if (cs == null) {
      throw new PitError("Unknown class id " + id + " (We have "
          + this.classIdCount + " classes.)");
    }
    return cs;
  }

  public Collection<ClassStatistics> getClassStatistics() {
    return FCollection.flatMap(this.classStatisticsInClassIdOrder.values(),
        hasLineHit());
  }

  private static F<ClassStatistics, Option<ClassStatistics>> hasLineHit() {
    return new F<ClassStatistics, Option<ClassStatistics>>() {

      public Option<ClassStatistics> apply(final ClassStatistics a) {
        if (a.wasVisited()) {
          return Option.some(a);
        } else {
          return Option.none();
        }
      }

    };
  }

}
