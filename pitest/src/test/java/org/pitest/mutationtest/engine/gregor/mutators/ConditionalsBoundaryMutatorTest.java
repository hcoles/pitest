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
import org.pitest.verifier.mutants.IntMutantVerifier;
import org.pitest.verifier.mutants.MutatorVerifierStart;

import java.util.function.IntFunction;

import static org.junit.Assert.assertEquals;
import static org.pitest.mutationtest.engine.gregor.mutators.ConditionalsBoundaryMutator.CONDITIONALS_BOUNDARY;

public class ConditionalsBoundaryMutatorTest {

    MutatorVerifierStart v = MutatorVerifierStart.forMutator(CONDITIONALS_BOUNDARY)
            .notCheckingUnMutatedValues();

    private static int getZeroButPreventInlining() {
        return 0;
    }

    @Test
    public void shouldProvideAMeaningfulName() {
        assertEquals("CONDITIONALS_BOUNDARY",
                CONDITIONALS_BOUNDARY.getName());
    }

    @Test
    public void shouldReplaceIFLEwithILT() {
        IntMutantVerifier<String> v2 = v.forIntFunctionClass(HasIFLE.class);

        v2.firstMutantShouldReturn(1, "was > zero");
        v2.firstMutantShouldReturn(-1, "was <= zero");
        v2.firstMutantShouldReturn(0, "was > zero");
    }

    @Test
    public void shouldReplaceIFGEwithIFGT() {
        IntMutantVerifier<String> v2 = v.forIntFunctionClass(HasIFGE.class);

        v2.firstMutantShouldReturn(-1, "was < zero");
        v2.firstMutantShouldReturn(1, "was >= zero");
        v2.firstMutantShouldReturn(0, "was < zero");
    }

    @Test
    public void shouldReplaceIFGTwithIFGE() {
        IntMutantVerifier<String> v2 = v.forIntFunctionClass(HasIFGT.class);
        v2.firstMutantShouldReturn(-1, "was <= zero");
        v2.firstMutantShouldReturn(1, "was > zero");
        v2.firstMutantShouldReturn(0, "was > zero");
    }

    @Test
    public void shouldReplaceIFLTwithIFLE() {
        IntMutantVerifier<String> v2 = v.forIntFunctionClass(HasIFLT.class);
        v2.firstMutantShouldReturn(-1, "was < zero");
        v2.firstMutantShouldReturn(1, "was >= zero");
        v2.firstMutantShouldReturn(0, "was < zero");
    }

    @Test
    public void shouldReplaceICMPLEwithIF_ICMPLT() {
        IntMutantVerifier<String> v2 = v.forIntFunctionClass(HasIF_ICMPLE.class);
        v2.firstMutantShouldReturn(1, "was > zero");
        v2.firstMutantShouldReturn(-1, "was <= zero");
        v2.firstMutantShouldReturn(0, "was > zero");
    }

    @Test
    public void shouldReplaceIF_ICMPGEwithIF_ICMPGT() {
        IntMutantVerifier<String> v2 = v.forIntFunctionClass(HasIF_ICMPGE.class);
        v2.firstMutantShouldReturn(-1, "was < zero");
        v2.firstMutantShouldReturn(1, "was >= zero");
        v2.firstMutantShouldReturn(0, "was < zero");
    }

    @Test
    public void shouldReplaceIF_ICMPGTwithIF_ICMPGE() {
        IntMutantVerifier<String> v2 = v.forIntFunctionClass(HasIF_ICMPGT.class);
        v2.firstMutantShouldReturn(-1, "was <= zero");
        v2.firstMutantShouldReturn(1, "was > zero");
        v2.firstMutantShouldReturn(0, "was > zero");
    }

    @Test
    public void shouldReplaceIF_ICMPLTwithIF_ICMPGT() {
        IntMutantVerifier<String> v2 = v.forIntFunctionClass(HasIF_ICMPLT.class);
        v2.firstMutantShouldReturn(-1, "was < zero");
        v2.firstMutantShouldReturn(1, "was >= zero");
        v2.firstMutantShouldReturn(0, "was < zero");
    }

    private static class HasIFLE implements IntFunction<String> {
        @Override
        public String apply(int i) {
            if (i > 0) {
                return "was > zero";
            } else {
                return "was <= zero";
            }
        }
    }

    private static class HasIFGE implements IntFunction<String> {

        @Override
        public String apply(int i) {
            if (i < 0) {
                return "was < zero";
            } else {
                return "was >= zero";
            }
        }
    }

    private static class HasIFGT implements IntFunction<String> {

        @Override
        public String apply(int i) {
            if (i <= 0) {
                return "was <= zero";
            } else {
                return "was > zero";
            }
        }
    }

    private static class HasIFLT implements IntFunction<String> {
        @Override
        public String apply(int i) {
            if (i >= 0) {
                return "was >= zero";
            } else {
                return "was < zero";
            }
        }
    }

    private static class HasIF_ICMPLE implements IntFunction<String> {

        @Override
        public String apply(int i) {
            final int j = getZeroButPreventInlining();
            if (i > j) {
                return "was > zero";
            } else {
                return "was <= zero";
            }
        }
    }

    private static class HasIF_ICMPGE implements IntFunction<String> {

        @Override
        public String apply(int i) {
            final int j = getZeroButPreventInlining();
            if (i < j) {
                return "was < zero";
            } else {
                return "was >= zero";
            }
        }
    }

    private static class HasIF_ICMPGT implements IntFunction<String> {

        @Override
        public String apply(int i) {
            final int j = getZeroButPreventInlining();
            if (i <= j) {
                return "was <= zero";
            } else {
                return "was > zero";
            }
        }
    }

    private static class HasIF_ICMPLT implements IntFunction<String> {

        @Override
        public String apply(int i) {
            final int j = getZeroButPreventInlining();
            if (i >= j) {
                return "was >= zero";
            } else {
                return "was < zero";
            }
        }
    }

}
