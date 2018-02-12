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
package org.pitest.mutationtest.engine;

import java.util.List;

import org.pitest.classinfo.ClassName;

/**
 * Generates mutants
 */
public interface Mutater {

  /**
   * Creates a mutant matching the given MutationIdentifier
   *
   * @param id
   *          the mutant to create
   * @return a Mutant
   */
  Mutant getMutation(MutationIdentifier id);

  /**
   * Scans for possible mutants in the given class
   *
   * @param classToMutate
   *          the class to scan for mutants
   * @return a list of possible mutants
   */
  List<MutationDetails> findMutations(ClassName classToMutate);

}
