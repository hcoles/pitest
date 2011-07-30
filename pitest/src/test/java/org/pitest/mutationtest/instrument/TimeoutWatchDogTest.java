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

package org.pitest.mutationtest.instrument;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.functional.SideEffect;
import org.pitest.mutationtest.execute.TimeoutWatchDog;

public class TimeoutWatchDogTest {

  private TimeoutWatchDog testee;

  @Mock
  private SideEffect      exitStrategy;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test(timeout = 200)
  public void shouldApplyExitStrategyAfterTimeIntervalHasPassed()
      throws InterruptedException {
    this.testee = new TimeoutWatchDog(this.exitStrategy,
        System.currentTimeMillis() + 100);
    this.testee.requestStart();
    this.testee.waitForExit(150);
    verify(this.exitStrategy).apply();
  }

  @Test(timeout = 1000)
  public void shouldNotApplyExitStrategyIfShutdownBeforeTimeIntervalHasPAssed()
      throws InterruptedException {
    this.testee = new TimeoutWatchDog(this.exitStrategy,
        System.currentTimeMillis() + 100);
    this.testee.requestStart();
    this.testee.requestStop();

    Thread.sleep(305);

    verify(this.exitStrategy, never()).apply();
  }

}
