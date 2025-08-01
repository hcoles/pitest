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

import org.pitest.coverage.TestStatListener;

import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class Timings {

    public Timings(TestStatListener testStats) {
        this.testStats = testStats;
    }

    public enum Stage {
    MUTATION_PRE_SCAN("pre-scan for mutations"),
    BUILD_MUTATION_TESTS("build mutation tests"),
    RUN_MUTATION_TESTS("run mutation analysis"),
    SCAN_CLASS_PATH("scan classpath"),
    COVERAGE("coverage and dependency analysis");

    private final String description;

    Stage(final String desc) {
      this.description = desc;
    }

    @Override
    public String toString() {
      return this.description;
    }
  }

  private final TestStatListener testStats;
  private final Map<Stage, TimeSpan> timings = new LinkedHashMap<>();

  public void registerStart(final Stage stage) {
    this.timings.put(stage, new TimeSpan(System.nanoTime(), 0));
  }

  public void registerEnd(final Stage stage) {
    final long end = System.nanoTime();
    this.timings.get(stage).setEnd(end);
  }

  public void report(final PrintStream ps) {
    long total = 0;
    for (final Entry<Stage, TimeSpan> each : this.timings.entrySet()) {
      total = total + each.getValue().duration();
      ps.println("> " + each.getKey() + " : " + each.getValue());
    }
    ps.println(StringUtil.separatorLine());
    ps.println("> Total " + " : " + new TimeSpan(0, MILLISECONDS.toNanos(total)));
    ps.println(StringUtil.separatorLine());

    for (String each : testStats.messages()) {
      ps.println("> " + each);
    }
  }

}
