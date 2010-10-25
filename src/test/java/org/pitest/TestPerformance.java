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

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pitest.annotations.PITSuite;
import org.pitest.containers.UnContainer;
import org.pitest.junit.JUnitCompatibleConfiguration;
import org.pitest.junit.adapter.PITJUnitRunner;

public class TestPerformance {

  public static void main(final String[] args) {
    final Pitest p = new Pitest(new DefaultStaticConfig(),
        new JUnitCompatibleConfiguration());

    p.run(new UnContainer(), RepetativeSuite.class);

  }

  private Pitest testee;

  @RunWith(PITJUnitRunner.class)
  public static class SmallTest {
    @Test
    public void testOne() {

    }

    @Test
    public void testTwo() {

    }

    @Test
    public void testThree() {

    }

    @Test
    public void testFour() {

    }

    @Test
    public void testFive() {

    }

  }

  public static class RepetativeSuite {
    @PITSuite
    public static Collection<Class<?>> classes() {
      final Class<?>[] cs = new Class<?>[1000];
      Arrays.fill(cs, SmallTest.class);
      return Arrays.asList(cs);
    }
  }

  @Test
  @Ignore
  public void testUncontainer() {
    this.testee = new Pitest(new DefaultStaticConfig(),
        new JUnitCompatibleConfiguration());
    final long t0 = System.currentTimeMillis();
    this.testee.run(new UnContainer(), RepetativeSuite.class);

    // numbers will be highly system dependent delete this
    // test if becomes problematic
    assertTrue((System.currentTimeMillis() - t0) < 2500);
  }

}