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
package org.pitest.mutationtest;

public enum Mutator implements MutationOperator {

  NEGS("Mutate neg instructions"), RETURN_VALS("Mutate return values"), INLINE_CONSTS(
      "Mutate inline constants"), MATH("Mutate math operations"), METHOD_CALLS(
      "Remove method calls"), CONDITIONALS(
      "Mutate conditional branch instructions"), INCREMENTS("Mutate increments");

  Mutator(final String description) {
    this.description = description;
  }

  private final String description;

  @Override
  public String toString() {
    return this.description;
  }

}
