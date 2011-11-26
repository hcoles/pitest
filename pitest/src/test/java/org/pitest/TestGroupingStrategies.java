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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.extension.Configuration;
import org.pitest.extension.Container;
import org.pitest.extension.GroupingStrategy;
import org.pitest.extension.ResultSource;
import org.pitest.extension.TestUnit;
import org.pitest.extension.common.GroupPerClassStrategy;
import org.pitest.extension.common.UnGroupedStrategy;
import org.pitest.junit.JUnitCompatibleConfiguration;

public class TestGroupingStrategies {

  private Pitest        testee;

  @Mock
  private Container     container;

  @Mock
  private ResultSource  rs;

  private Configuration conf;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    when(this.container.getResultSource()).thenReturn(this.rs);
    when(this.container.awaitCompletion()).thenReturn(true);
    when(this.rs.resultsAvailable()).thenReturn(false);
    this.conf = new JUnitCompatibleConfiguration();
  }

  @RunWith(Suite.class)
  @SuiteClasses({ TestOne.class, TestTwo.class })
  public static class ASuite {

  }

  public static class TestOne {

    @Test
    public void test() {

    }

    @Test
    public void test2() {

    }
  }

  public static class TestTwo {

    @Test
    public void test() {

    }

    @Test
    public void test2() {

    }
  }

  @Test
  public void shouldCorrectlyApplyTheGroupPerClassStrategy() {
    final DefaultStaticConfig staticConfig = new DefaultStaticConfig() {
      @Override
      public GroupingStrategy getGroupingStrategy() {
        return new GroupPerClassStrategy();
      }
    };

    this.testee = new Pitest(staticConfig, this.conf);
    this.testee.run(this.container, ASuite.class);
    verify(this.container, times(2)).submit((any(TestUnit.class)));
  }

  @Test
  public void shouldCorrectlyApplyTheUngroupedStrategy() {
    final DefaultStaticConfig staticConfig = new DefaultStaticConfig() {
      @Override
      public GroupingStrategy getGroupingStrategy() {
        return new UnGroupedStrategy();
      }
    };
    this.testee = new Pitest(staticConfig, this.conf);
    this.testee.run(this.container, ASuite.class);
    verify(this.container, times(4)).submit((any(TestUnit.class)));
  }
}
