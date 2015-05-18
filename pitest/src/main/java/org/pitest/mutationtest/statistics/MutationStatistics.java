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
package org.pitest.mutationtest.statistics;

import org.pitest.functional.F2;
import org.pitest.functional.FCollection;
import org.pitest.functional.SideEffect1;
import org.pitest.mutationtest.MutationResult;

import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MutationStatistics {
  private final Map<String, Score> mutatorTotalMap  = new HashMap<String, Score>();
  private long                     numberOfTestsRun = 0;

  public void registerResults(Collection<MutationResult> results) {
    FCollection.forEach(results, register());
  }

  private SideEffect1<MutationResult> register() {
    return new SideEffect1<MutationResult>() {

      public void apply(MutationResult mr) {
        numberOfTestsRun = numberOfTestsRun + mr.getNumberOfTestsRun();
        String key = mr.getDetails().getId().getMutator();
        Score total = mutatorTotalMap.get(key);
        if (total == null) {
          total = new Score(key);
          mutatorTotalMap.put(key, total);
        }
        total.registerResult(mr.getStatus());
      }
    };
  }

  public Iterable<Score> getScores() {
    return mutatorTotalMap.values();
  }

  public long getTotalMutations() {
    return FCollection.fold(addTotals(), 0L, mutatorTotalMap.values());
  }

  public long getTotalDetectedMutations() {
    return FCollection.fold(addDetectedTotals(), 0L, mutatorTotalMap.values());
  }

  public long getPercentageDetected() {
    if (getTotalMutations() == 0) {
      return 100;
    }

    if (getTotalDetectedMutations() == 0) {
      return 0;
    }

    return Math.round((100f / getTotalMutations()) * getTotalDetectedMutations());
  }

  private static F2<Long, Score, Long> addDetectedTotals() {
    return new F2<Long, Score, Long>() {
      public Long apply(Long a, Score b) {
        return a + b.getTotalDetectedMutations();
      }
    };
  }

  private static F2<Long, Score, Long> addTotals() {
    return new F2<Long, Score, Long>() {
      public Long apply(Long a, Score b) {
        return a + b.getTotalMutations();
      }
    };
  }

  public void report(PrintStream out) {
    out.println(">> Generated " + getTotalMutations()
        + " mutations Killed " + getTotalDetectedMutations() + " ("
        + getPercentageDetected() + "%)");
    out.println(">> Ran " + numberOfTestsRun + " tests ("
        + getTestsPerMutation() + " tests per mutation)");
  }

  private String getTestsPerMutation() {
    if (getTotalMutations() == 0) {
      return "0";
    }

    float testsPerMutation = numberOfTestsRun / (float) getTotalMutations();
    DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ENGLISH);
    return new DecimalFormat("#.##", symbols).format(testsPerMutation);
  }

}
