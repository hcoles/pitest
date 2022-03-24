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
package org.pitest.mutationtest.engine.gregor.mutators.experimental;

import org.junit.Test;
import org.pitest.verifier.mutants.MutatorVerifierStart;

import java.util.concurrent.Callable;

import static org.junit.Assert.assertEquals;
import static org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveIncrementsMutator.REMOVE_INCREMENTS;

public class RemoveIncrementsMutatorTest {

    MutatorVerifierStart v = MutatorVerifierStart.forMutator(REMOVE_INCREMENTS)
            .notCheckingUnMutatedValues();

    @Test
    public void shouldProvideAMeaningfulName() {
        assertEquals("REMOVE_INCREMENTS",
                REMOVE_INCREMENTS.getName());
    }

    @Test
    public void shouldRemoveArgumentsToIInc() {
        v.forCallableClass(HasIncrement.class)
                .firstMutantShouldReturn("1");
    }

    @Test
    public void shouldCreateNoMutationsWhenNoIncrementsPresent() {
        v.forCallableClass(HasNoIncrements.class)
                .noMutantsCreated();
    }

    private static class HasIncrement implements Callable<String> {
        public int containsIincInstructions(int i) {
            return ++i;
        }

        @Override
        public String call() {
            return "" + containsIincInstructions(1);
        }

    }

    private static class HasNoIncrements implements Callable<String> {

        @Override
        public String call() {
            return "foo";
        }

    }

}
