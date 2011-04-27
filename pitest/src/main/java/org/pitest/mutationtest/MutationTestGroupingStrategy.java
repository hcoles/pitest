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
package org.pitest.mutationtest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.pitest.Description;
import org.pitest.MetaData;
import org.pitest.PitError;
import org.pitest.extension.GroupingStrategy;
import org.pitest.extension.ResultCollector;
import org.pitest.extension.TestUnit;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.internal.TestClass;
import org.pitest.mutationtest.instrument.InstrumentedMutationTestUnit;
import org.pitest.testunit.AbstractTestUnit;

public class MutationTestGroupingStrategy implements GroupingStrategy {

  private final GroupingStrategy defaultGrouping;

  public MutationTestGroupingStrategy(final GroupingStrategy defaultGrouping) {
    this.defaultGrouping = defaultGrouping;
  }

  public Collection<? extends TestUnit> group(final TestClass c,
      final Collection<TestUnit> testUnitsFromClass) {
    final List<TestUnit> mutationTests = FCollection.filter(testUnitsFromClass,
        isAMutationTest());
    if (!mutationTests.isEmpty()) {
      testUnitsFromClass.removeAll(mutationTests);
      return Collections.singletonList(new MutationTestGroup(
          testUnitsFromClass, mutationTests));

    } else {
      return this.defaultGrouping.group(c, testUnitsFromClass);
    }
  }

  private F<TestUnit, Boolean> isAMutationTest() {
    return new F<TestUnit, Boolean>() {
      public Boolean apply(final TestUnit a) {
        return a instanceof InstrumentedMutationTestUnit;
      }
    };
  }

  private static class MutationTestGroup extends AbstractTestUnit {

    private final Collection<TestUnit> testUnitsFromClass;
    private final Collection<TestUnit> mutationTests;

    public MutationTestGroup(final Collection<TestUnit> testUnitsFromClass,
        final Collection<TestUnit> mutationTests) {
      super(createDescription(testUnitsFromClass, mutationTests));
      this.testUnitsFromClass = testUnitsFromClass;
      this.mutationTests = mutationTests;
    }

    private static Description createDescription(
        final Collection<TestUnit> children,
        final Collection<TestUnit> mutationTests) {
      final Set<Class<?>> uniqueClasses = new HashSet<Class<?>>();
      final F<TestUnit, Iterable<Class<?>>> f = new F<TestUnit, Iterable<Class<?>>>() {
        public Iterable<Class<?>> apply(final TestUnit a) {
          return a.getDescription().getTestClasses();
        }
      };
      FCollection.flatMapTo(children, f, uniqueClasses);
      return new Description("MutationTestGroup", uniqueClasses, null);
    }

    @Override
    public Iterator<TestUnit> iterator() {
      final Collection<TestUnit> all = new ArrayList<TestUnit>(
          this.testUnitsFromClass.size() + 1);
      all.addAll(this.testUnitsFromClass);
      all.addAll(this.mutationTests);
      return all.iterator();
    }

    @Override
    public void execute(final ClassLoader loader, final ResultCollector rc) {
      final ResultCollectorWrapper rcw = new ResultCollectorWrapper(rc);
      runTests(loader, rcw);

      if (rcw.isGreenSuite() && !rcw.shouldExit()) {
        runMutationTests(loader, rc);
      } else if (!rcw.isGreenSuite()) {
        reportErrorForEachMutationTestUnit(rc);
      }

    }

    private void reportErrorForEachMutationTestUnit(final ResultCollector rc) {
      for (final TestUnit each : this.mutationTests) {
        rc.notifyEnd(each.getDescription(), new PitError(
            "Cannot mutation test as tests do not pass without mutation"));
      }

    }

    private void runMutationTests(final ClassLoader loader,
        final ResultCollector rc) {
      for (final TestUnit each : this.mutationTests) {
        each.execute(loader, rc);
        if (rc.shouldExit()) {
          break;
        }
      }
    }

    private void runTests(final ClassLoader loader,
        final ResultCollectorWrapper rcw) {
      for (final TestUnit each : this.testUnitsFromClass) {
        each.execute(loader, rcw);
        if (rcw.shouldExit()) {
          break;
        }
      }
    }

  }

  private static class ResultCollectorWrapper implements ResultCollector {

    private final ResultCollector child;
    private boolean               greenSuite = true;

    ResultCollectorWrapper(final ResultCollector child) {
      this.child = child;

    }

    public void notifyEnd(final Description description, final Throwable t,
        final MetaData... data) {
      this.child.notifyEnd(description, t, data);
      if (t != null) {
        this.greenSuite = false;
      }

    }

    public void notifyEnd(final Description description, final MetaData... data) {
      this.child.notifyEnd(description, data);

    }

    public void notifySkipped(final Description description) {
      this.child.notifySkipped(description);

    }

    public void notifyStart(final Description description) {
      this.child.notifyStart(description);

    }

    public boolean shouldExit() {
      return this.child.shouldExit();
    }

    boolean isGreenSuite() {
      return this.greenSuite;
    }

  }

}
