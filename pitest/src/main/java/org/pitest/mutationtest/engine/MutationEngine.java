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

import java.util.Collection;

import org.pitest.classinfo.ClassByteArraySource;

/**
 * A mutation engine acts as a factory for mutaters capable of creating mutant
 * classes.
 */
public interface MutationEngine {

  /**
   * Create a mutator using the given ClassByteArraySource as the source of
   * unmated classes
   *
   * @param source
   *          the source to use to retrieve unmated classes
   * @return a Mutater
   */
  Mutater createMutator(ClassByteArraySource source);

  /**
   * Returns a list of mutation operations this engine can perform
   *
   * @return a list of mutator names
   */
  Collection<String> getMutatorNames();

  /**
   * Returns the name of this engine
   * @return The engine name
   */
  String getName();

}
