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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.pitest.functional.F;
import org.pitest.functional.FCollection;

public class CoverageStatistics {

  private final List<ClassStatistics> classStatisticsInClassIdOrder = new ArrayList<ClassStatistics>();

  public synchronized void clearCoverageStats() {
    for (final ClassStatistics each : this.classStatisticsInClassIdOrder) {
      each.clearLineCoverageStats();
    }
  }

  public synchronized int registerClass(final String className) {
    this.classStatisticsInClassIdOrder.add(new ClassStatistics(className));
    return this.classStatisticsInClassIdOrder.size() - 1;
  }

  public synchronized void visitLine(final int classId, final int lineId) {
    this.getClassStatistics(classId).registerLineVisit(lineId);
  }

  private synchronized ClassStatistics getClassStatistics(final int id) {
    return this.classStatisticsInClassIdOrder.get(id);
  }

  public synchronized Collection<ClassStatistics> getClassStatistics(
      final Collection<String> classNames) {
    return FCollection.map(classNames, classNameToClassStatistics());
  }

  private F<String, ClassStatistics> classNameToClassStatistics() {
    return new F<String, ClassStatistics>() {
      public ClassStatistics apply(final String a) {
        return getClassStatistics(a);
      }

    };
  }

  public synchronized Collection<ClassStatistics> getClassStatistics() {
    return this.classStatisticsInClassIdOrder;
  }

  public synchronized ClassStatistics getClassStatistics(final String clazz) {
    for (final ClassStatistics each : this.classStatisticsInClassIdOrder) {
      if (each.getClassName().equals(clazz)) {
        return each;
      }
    }
    return null; // fixme
  }

}
