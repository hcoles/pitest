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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.pitest.functional.FCollection;
import org.pitest.functional.SideEffect1;
import org.pitest.mutationtest.results.MutationResult;

public class MutationStatistics {
  private final Map<String, Score> mutatorTotalMap = new HashMap<String, Score>();

  public void registerResults(final Collection<MutationResult> results) {
    FCollection.forEach(results, register());
  }

  private SideEffect1<MutationResult> register() {
    return new SideEffect1<MutationResult>() {

      public void apply(final MutationResult mr) {
        final String key = mr.getDetails().getId().getMutator();
        Score total = MutationStatistics.this.mutatorTotalMap.get(key);
        if (total == null) {
          total = new Score(key);
          MutationStatistics.this.mutatorTotalMap.put(key, total);
        }
        total.registerResult(mr.getStatus());
      }

    };
  }

  public Iterable<Score> getScores() {
    return this.mutatorTotalMap.values();
  }

}
