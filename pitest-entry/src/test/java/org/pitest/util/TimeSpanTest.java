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
package org.pitest.util;

import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TimeSpanTest {

  @Test
  public void shouldReportTimesAsLessThanOneSecondWhenLessThanOneSecond() {
    assertEquals("< 1 second", new TimeSpan(0, MILLISECONDS.toNanos(999)).toString());
  }

  @Test
  public void shouldReportTimesInSecondsWhenLessThenOneMinute() {
    assertEquals("59 seconds", new TimeSpan(0, SECONDS.toNanos(59)).toString());
  }

  @Test
  public void shouldReportTimesInMinutesWhenMoreThanOneMinute() {
    assertEquals("1 minutes and 1 seconds",
        new TimeSpan(0, MINUTES.toNanos(1) + SECONDS.toNanos(1)).toString());
  }

  @Test
  public void shouldReportTimesInHoursWhenMoreThanOneHour() {
    assertEquals("1 hours, 2 minutes and 1 seconds", new TimeSpan(0,
        HOURS.toNanos(1) + MINUTES.toNanos(2) + SECONDS.toNanos(1)).toString());
  }
}
