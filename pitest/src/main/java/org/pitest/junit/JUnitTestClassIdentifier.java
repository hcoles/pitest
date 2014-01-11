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
package org.pitest.junit;

import org.junit.experimental.categories.Category;
import org.pitest.classinfo.ClassInfo;
import org.pitest.testapi.TestGroupConfig;
import org.pitest.testapi.TestClassIdentifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JUnitTestClassIdentifier implements TestClassIdentifier {

  private TestGroupConfig config;

  public JUnitTestClassIdentifier(TestGroupConfig config) {

    this.config = config;
  }

  public boolean isATestClass(final ClassInfo a) {
    return TestInfo.isWithinATestClass(a);
  }

  public boolean isIncluded(ClassInfo a) {
    List<String> included = config.getIncludedGroups();
    return included.isEmpty() || !Collections.disjoint(included, getCategories(a));
  }

  public boolean isExcluded(ClassInfo a) {
    List<String> excluded = config.getExcludedGroups();
    return !excluded.isEmpty() && !Collections.disjoint(excluded, getCategories(a));
  }

  private List<String> getCategories(ClassInfo a) {
    List<String> categories = new ArrayList<String>();
    try {
      if (a.hasAnnotation(Category.class)) {
        Class<?> targetClass = Class.forName(a.getName().asJavaName());
        Category annotation = targetClass.getAnnotation(Category.class);
        for(Class category: annotation.value()) {
          categories.add(category.getName());
        }
      }
      return categories;
    } catch (ClassNotFoundException e) {
      throw new IllegalStateException("Cannot find class " + a, e);
    }
  }

}
