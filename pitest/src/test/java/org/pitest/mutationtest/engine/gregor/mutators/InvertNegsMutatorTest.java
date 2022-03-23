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
package org.pitest.mutationtest.engine.gregor.mutators;

import org.junit.Test;
import org.pitest.verifier.mutants.MutatorVerifierStart;

import java.util.concurrent.Callable;

import static org.pitest.mutationtest.engine.gregor.mutators.InvertNegsMutator.INVERT_NEGS;

public class InvertNegsMutatorTest {

    MutatorVerifierStart v = MutatorVerifierStart.forMutator(INVERT_NEGS);

    @Test
    public void shouldFindNoMutationsWhenNonePossible() {
        v.forClass(NothingToMutate.class)
                .noMutantsCreated();
    }

    @Test
    public void shouldInvertINegs() {
        v.forCallableClass(HasINeg.class)
                .firstMutantShouldReturn
                        ("1");
    }

    @Test
    public void shouldInvertFNegs() {
        v.forCallableClass(HasFNeg.class)
                .firstMutantShouldReturn
                        ("1.0");
    }

    @Test
    public void shouldInvertLNegs() {
         v.forCallableClass(HasLNeg.class)
                .firstMutantShouldReturn
                        ("1");
    }

    private static class NothingToMutate {

    }

    private static class HasINeg implements Callable<String> {
        public int containsINeg(final int i) {
            return -i;
        }

        @Override
        public String call() {
            return "" + containsINeg(1);
        }

    }

    private static class HasFNeg implements Callable<String> {
        public float containsFNeg(final float i) {
            return -i;
        }

        @Override
        public String call() {
            return "" + containsFNeg(1f);
        }

    }

    private static class HasLNeg implements Callable<String> {
        public int containsLNeg(final int i) {
            return -i;
        }

        @Override
        public String call() {
            return "" + containsLNeg(1);
        }

    }

}
