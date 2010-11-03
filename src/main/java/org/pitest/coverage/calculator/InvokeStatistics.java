/*
 * Based on http://code.google.com/p/javacoveragent/ by
 * "alex.mq0" and "dmitry.kandalov"
 * 
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

package org.pitest.coverage.calculator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * @author ivanalx
 * @date 28.01.2009 14:08:25
 */
public class InvokeStatistics {
  private final List<List<MethodNameDescription>> methodDescriptionsInClassIdOrder = new ArrayList<List<MethodNameDescription>>();
  private final List<String>                      classNames                       = new ArrayList<String>();

  private final Map<Integer, Set<Integer>>        visitedMethodsByClassId          = new ConcurrentHashMap<Integer, Set<Integer>>();
  private final Map<Integer, Set<Integer>>        visitedLinesByClassId            = new ConcurrentHashMap<Integer, Set<Integer>>();

  public InvokeStatistics() {
  }

  public void clearStats() {
    this.visitedMethodsByClassId.clear();
    this.visitedLinesByClassId.clear();

    for (int id = 0; id != this.classNames.size(); id++) {
      this.visitedMethodsByClassId
          .put(id, new ConcurrentSkipListSet<Integer>());
      this.visitedLinesByClassId.put(id, new ConcurrentSkipListSet<Integer>());
    }

  }

  public synchronized int registerClass(final String className) {
    this.methodDescriptionsInClassIdOrder
        .add(new LinkedList<MethodNameDescription>());
    this.classNames.add(className);
    final int id = this.classNames.size() - 1;
    if (!this.visitedMethodsByClassId.containsKey(id)) {
      this.visitedMethodsByClassId
          .put(id, new ConcurrentSkipListSet<Integer>());
    }
    if (!this.visitedLinesByClassId.containsKey(id)) {
      this.visitedLinesByClassId.put(id, new ConcurrentSkipListSet<Integer>());
    }
    return id;
  }

  public synchronized int registerMethod(final int classId,
      final String methodName, final String methodDesc) {
    final List<MethodNameDescription> methods = this.methodDescriptionsInClassIdOrder
        .get(classId);
    methods.add(new MethodNameDescription(methodName, methodDesc));
    return methods.size() - 1;
  }

  public void visitMethod(final int classId, final int methodId) {
    this.visitedMethodsByClassId.get(classId).add(methodId);
  }

  public void visitLine(final int classId, final int lineId) {
    this.visitedLinesByClassId.get(classId).add(lineId);
  }

  public Map<String, Map<MethodNameDescription, Boolean>> generateMethodInvokeStatistics() {
    final Map<String, Map<MethodNameDescription, Boolean>> statistics = new HashMap<String, Map<MethodNameDescription, Boolean>>();

    for (final Map.Entry<Integer, Set<Integer>> entry : this.visitedMethodsByClassId
        .entrySet()) {
      final String className = this.classNames.get(entry.getKey());

      final List<MethodNameDescription> methodNames = this.methodDescriptionsInClassIdOrder
          .get(entry.getKey());

      final Set<Integer> visitedMethods = entry.getValue();
      final Set<Integer> notVisitedMethods = new HashSet<Integer>();
      for (int i = 0; i < methodNames.size(); i++) {
        notVisitedMethods.add(i);
      }
      notVisitedMethods.removeAll(visitedMethods);
      final Map<MethodNameDescription, Boolean> methodVisitStatistics = new HashMap<MethodNameDescription, Boolean>();
      statistics.put(className, methodVisitStatistics);
      for (final Integer methodId : visitedMethods) {
        methodVisitStatistics.put(methodNames.get(methodId), true);
      }
      for (final Integer methodId : notVisitedMethods) {
        methodVisitStatistics.put(methodNames.get(methodId), false);
      }
    }
    return statistics;
  }
  
  public Set<Integer> getVisitedLines() {
    return visitedLinesByClassId.values().iterator().next();
  }

  public Map<String, Map<Integer, Boolean>> generateLineInvokeStatistics() {
    final Map<String, Map<Integer, Boolean>> statistics = new HashMap<String, Map<Integer, Boolean>>(); // TODO
    // use
    // it
    return null;
  }

}
