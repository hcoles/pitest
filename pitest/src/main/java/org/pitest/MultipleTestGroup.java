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

package org.pitest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.pitest.extension.ResultCollector;
import org.pitest.extension.TestFilter;
import org.pitest.extension.TestUnit;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.Option;
import org.pitest.testunit.AbstractTestUnit;

public class MultipleTestGroup extends AbstractTestUnit {

  private static final long          serialVersionUID = 1L;

  private final Collection<TestUnit> children;

  public MultipleTestGroup(final Collection<TestUnit> children) {
    super(createDescription(children));
    this.children = children;
  }

  private static Description createDescription(
      final Collection<TestUnit> children) {
    final Set<Class<?>> uniqueClasses = new HashSet<Class<?>>();
    final F<TestUnit, Iterable<Class<?>>> f = new F<TestUnit, Iterable<Class<?>>>() {
      public Iterable<Class<?>> apply(final TestUnit a) {
        return a.getDescription().getTestClasses();
      }
    };
    FCollection.flatMap(children, f, uniqueClasses);
    return new Description("MultipleTestGroup", uniqueClasses, null);
  }

  @Override
  public void execute(final ClassLoader loader, final ResultCollector rc) {
    for (final TestUnit each : this.children) {
      each.execute(loader, rc);
      if (rc.shouldExit()) {
        break;
      }
    }

  }

  @Override
  public Iterator<TestUnit> iterator() {
    return this.children.iterator();
  }

  @Override
  public Option<TestUnit> filter(final TestFilter filter) {

    final Collection<TestUnit> filtered = new ArrayList<TestUnit>(
        this.children.size());
    for (final TestUnit each : this.children) {
      final Option<TestUnit> tu = each.filter(filter);
      for (final TestUnit value : tu) {
        filtered.add(value);
      }
    }

    if (filtered.isEmpty()) {
      return Option.none();
    } else {
      return Option.<TestUnit> some(new MultipleTestGroup(filtered));
    }

  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((this.children == null) ? 0 : this.children.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final MultipleTestGroup other = (MultipleTestGroup) obj;
    if (this.children == null) {
      if (other.children != null) {
        return false;
      }
    } else if (!this.children.equals(other.children)) {
      return false;
    }
    return true;
  }

}
