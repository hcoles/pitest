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

import org.pitest.functional.SideEffect2;

import com.reeltwo.jumble.mutation.Mutater;

public enum Mutator implements SideEffect2<Mutater, Boolean> {
  ALL("All defined mutations", new SideEffect2<Mutater, Boolean>() {

    public void apply(final Mutater a, final Boolean b) {
      for (final Mutator each : values()) {
        if (!each.equals(ALL)) {
          each.function().apply(a, b);
        }
      }
    }

  }),

  NEGS("Mutate neg instructions.", new SideEffect2<Mutater, Boolean>() {
    public void apply(final Mutater a, final Boolean value) {
      a.setMutateNegs(value);
    }
  }),

  RETURN_VALS("Mutate return values.", new SideEffect2<Mutater, Boolean>() {
    public void apply(final Mutater a, final Boolean value) {
      a.setMutateReturnValues(value);
    }
  }), INLINE_CONSTS("Mutate inline constants.",
      new SideEffect2<Mutater, Boolean>() {
        public void apply(final Mutater a, final Boolean value) {
          a.setMutateInlineConstants(value);

        }
      }), CPOOL("Mutate constant pool entries.",
      new SideEffect2<Mutater, Boolean>() {
        public void apply(final Mutater a, final Boolean value) {
          a.setMutateCPool(value);
        }
      }), SWITCHES("Mutate switch instructions.",
      new SideEffect2<Mutater, Boolean>() {
        public void apply(final Mutater a, final Boolean value) {
          a.setMutateSwitch(value);
        }
      }), STORES("Mutate store instructions.",
      new SideEffect2<Mutater, Boolean>() {
        public void apply(final Mutater a, final Boolean value) {
          a.setMutateStores(value);

        }
      }), INCREMENTS("Mutate increments.", new SideEffect2<Mutater, Boolean>() {
    public void apply(final Mutater a, final Boolean value) {
      a.setMutateIncrements(value);
    }
  });

  Mutator(final String description, final SideEffect2<Mutater, Boolean> f) {
    this.description = description;
    this.function = f;
  }

  private final SideEffect2<Mutater, Boolean> function;
  private final String                        description;

  public SideEffect2<Mutater, Boolean> function() {
    return this.function;
  }

  @Override
  public String toString() {
    return this.description;
  }

  public void apply(final Mutater a, final Boolean b) {
    this.function().apply(a, b);
  }

}
