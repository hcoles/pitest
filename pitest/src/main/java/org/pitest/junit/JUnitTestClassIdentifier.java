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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.experimental.categories.Category;
import org.pitest.classinfo.ClassInfo;
import org.pitest.classinfo.ClassName;
import org.pitest.testapi.TestClassIdentifier;
import org.pitest.testapi.TestGroupConfig;

public class JUnitTestClassIdentifier implements TestClassIdentifier {

  private final TestGroupConfig config;

  public JUnitTestClassIdentifier(TestGroupConfig config) {
    this.config = config;
  }

  @Override
  public boolean isATestClass(final ClassInfo a) {
    return TestInfo.isWithinATestClass(a);
  }

  @Override
  public boolean isIncluded(ClassInfo a) {
    return isIncludedCategory(a) && !isExcludedCategory(a);
  }

  private boolean isIncludedCategory(ClassInfo a) {
    List<String> included = this.config.getIncludedGroups();
    return included.isEmpty()
        || !Collections.disjoint(included, Arrays.asList(getCategories(a)));
  }

  private boolean isExcludedCategory(ClassInfo a) {
    List<String> excluded = this.config.getExcludedGroups();
    return !excluded.isEmpty()
        && !Collections.disjoint(excluded, Arrays.asList(getCategories(a)));
  }

  private String[] getCategories(ClassInfo a) {
    Object[] categoryArray = (Object[]) a.getClassAnnotationValue(ClassName
        .fromClass(Category.class));
    if (categoryArray == null) {
      return new String[] {};
    }
    return copyArray(categoryArray);
  }

  private String[] copyArray(Object[] original) {
    String[] copy = new String[original.length];
    System.arraycopy(original, 0, copy, 0, original.length);
    return copy;
  }

}
