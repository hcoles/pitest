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
package org.pitest.mutationtest.build;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.pitest.classinfo.ClassName;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.mutationtest.engine.MutationDetails;

public class MutationTestBuilder {

  private final MutationSource   mutationSource;

  public MutationTestBuilder(final MutationSource mutationSource) {

    this.mutationSource = mutationSource;
  }

  public List<MutationDetails> createMutationTestUnits(final Collection<ClassName> codeClasses) {
   // final List<MutationAnalysisUnit> tus = new ArrayList<>();

    final List<MutationDetails> mutations = FCollection.flatMap(codeClasses,
        classToMutations());

    Collections.sort(mutations, comparator());
    
    return mutations;
  }

  private Comparator<MutationDetails> comparator() {
    return new Comparator<MutationDetails>() {

      @Override
      public int compare(final MutationDetails arg0, final MutationDetails arg1) {
        return arg0.getId().compareTo(arg1.getId());
      }

    };
  }

  private F<ClassName, Iterable<MutationDetails>> classToMutations() {
    return new F<ClassName, Iterable<MutationDetails>>() {
      @Override
      public Iterable<MutationDetails> apply(final ClassName a) {
        return MutationTestBuilder.this.mutationSource.createMutations(a);
      }

    };
  }



}
