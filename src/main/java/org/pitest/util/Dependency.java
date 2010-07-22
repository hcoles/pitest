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
package org.pitest.util;

import java.util.List;

import org.pitest.extension.TestUnit;
import org.pitest.functional.FCollection;
import org.pitest.functional.SideEffect1;

public class Dependency {

  public static void chainDepencies(final List<TestUnit> tus) {
    for (int i = 0; i != tus.size(); i++) {
      if (i != 0) {
        tus.get(i).setDependency(tus.get(i - 1));
      }
    }
  }

  public static void dependOnFirst(final List<TestUnit> tus) {
    if (tus.size() >= 1) {
      final TestUnit firstUnit = tus.get(0);
      final SideEffect1<TestUnit> e = new SideEffect1<TestUnit>() {
        public void apply(final TestUnit a) {
          a.setDependency(firstUnit);
        }
      };
      FCollection.forEach(tus.subList(1, tus.size()), e);
    }
  }

}
