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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.pitest.MultipleTestGroup;
import org.pitest.extension.GroupingStrategy;
import org.pitest.extension.TestUnit;
import org.pitest.internal.TestClass;
import org.pitest.junit.JUnitCompatibleConfiguration;

public class GroupLikedTypeOrderStrategyTest {

  private GroupLikedTypeOrderStrategy testee;

  @Before
  public void setUp() {
    this.testee = new GroupLikedTypeOrderStrategy(Target.class);
  }

  static interface Target {
  }

  static class TargetTest1 implements Target {
    @Test
    public void testOne() {

    }

    @Test
    public void testTwo() {

    }

  }

  static class TargetTest2 implements Target {
    @Test
    public void testOne() {

    }

    @Test
    public void testTwo() {
    }

  }

  static class TargetTest3 implements Target {
    @Test
    public void testOne() {

    }

    @Test
    public void testTwo() {

    }
  }

  static class NonTargetTest1 {
    @Test
    public void testOne() {

    }

    @Test
    public void testTwo() {

    }
  }

  static class NonTargetTest2 {
    @Test
    public void testOne() {

    }

    @Test
    public void testTwo() {

    }
  }

  static class NonTargetTest3 {
    @Test
    public void testOne() {

    }

    @Test
    public void testTwo() {

    }
  }

  private Collection<TestUnit> classToTests(final Class<?> c,
      final GroupingStrategy groupStrategy) {
    final TestClass tc = new TestClass(c);
    return tc.getTestUnits(new JUnitCompatibleConfiguration(),
        new NullDiscoveryListener(), groupStrategy);
  }

  private List<TestUnit> createTargetList(final GroupingStrategy groupStrategy) {
    final List<TestUnit> tus = new ArrayList<TestUnit>();
    tus.addAll(classToTests(NonTargetTest1.class, groupStrategy));
    tus.addAll(classToTests(NonTargetTest2.class, groupStrategy));
    tus.addAll(classToTests(NonTargetTest3.class, groupStrategy));
    return tus;
  }

  private List<TestUnit> createNonTargetList(
      final GroupingStrategy groupStrategy) {
    final List<TestUnit> tus = new ArrayList<TestUnit>();
    tus.addAll(classToTests(TargetTest1.class, groupStrategy));
    tus.addAll(classToTests(TargetTest2.class, groupStrategy));
    tus.addAll(classToTests(TargetTest3.class, groupStrategy));
    return tus;
  }

  @Test
  public void testCreatesGroupForTargetClassWhenInputListUngrouped() {
    final List<TestUnit> tus = new ArrayList<TestUnit>();
    final List<TestUnit> expected = createNonTargetList(new UnGroupedStrategy());
    tus.addAll(expected);
    tus.addAll(createTargetList(new UnGroupedStrategy()));

    final List<TestUnit> actual = this.testee.order(tus);
    assertTrue(actual.get(0) instanceof MultipleTestGroup);
    assertEquals(expected.size(), actual.subList(1, actual.size()).size());
  }

  @Test
  public void testCreatesGroupForTargetClassWhenInputListGrouped() {
    final List<TestUnit> tus = new ArrayList<TestUnit>();
    final List<TestUnit> expected = createNonTargetList(new GroupPerClassStrategy());
    tus.addAll(expected);
    tus.addAll(createTargetList(new GroupPerClassStrategy()));

    final List<TestUnit> actual = this.testee.order(tus);
    assertTrue(actual.get(0) instanceof MultipleTestGroup);
    assertEquals(expected.size(), actual.subList(1, actual.size()).size());
  }

}
