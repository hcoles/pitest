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

/**
 * A fully generated mutant
 */
public final class Mutant {

  private final MutationDetails details;
  private final byte[]          bytes;

  public Mutant(final MutationDetails details, final byte[] bytes) {
    this.details = details;
    this.bytes = bytes;
  }

  /**
   * Returns a data relating to the mutant
   *
   * @return A MutationDetails object
   */
  public MutationDetails getDetails() {
    return this.details;
  }

  /**
   * Returns a byte array containing the mutant class
   *
   * @return A byte array
   */
  public byte[] getBytes() {
    return this.bytes;
  }

}
