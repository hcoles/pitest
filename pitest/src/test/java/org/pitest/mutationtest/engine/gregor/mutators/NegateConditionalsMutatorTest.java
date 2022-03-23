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

import java.util.function.Function;
import java.util.function.IntFunction;

import static org.pitest.mutationtest.engine.gregor.mutators.NegateConditionalsMutator.NEGATE_CONDITIONALS;

public class NegateConditionalsMutatorTest {

    MutatorVerifierStart v = MutatorVerifierStart.forMutator(NEGATE_CONDITIONALS)
            .notCheckingUnMutatedValues();


    private static int getZeroButPreventInlining() {
        return 0;
    }

    @Test
    public void shouldReplaceIFEQWithIFNE() {
        v.forIntFunctionClass(HasIFEQ.class)
                .firstMutantShouldReturn(1, "was zero");
        v.forIntFunctionClass(HasIFEQ.class)
                .firstMutantShouldReturn(0, "was not zero");
    }

    @Test
    public void shouldReplaceIFNEWithIFEQ() {
        v.forIntFunctionClass(HasIFNE.class)
                .firstMutantShouldReturn(1, "was zero");
        v.forIntFunctionClass(HasIFNE.class)
                .firstMutantShouldReturn(0, "was not zero");
    }

    @Test
    public void shouldReplaceIFLEWithIGT() {
        v.forIntFunctionClass(HasIFLE.class)
                .firstMutantShouldReturn(1, "was <= zero");
        v.forIntFunctionClass(HasIFLE.class)
                .firstMutantShouldReturn(-1, "was > zero");
        v.forIntFunctionClass(HasIFLE.class)
                .firstMutantShouldReturn(0, "was > zero");
    }

    @Test
    public void shouldReplaceIFGEWithIFLT() {
        v.forIntFunctionClass(HasIFGE.class)
                .firstMutantShouldReturn(-1, "was >= zero");
        v.forIntFunctionClass(HasIFGE.class)
                .firstMutantShouldReturn(1, "was < zero");
        v.forIntFunctionClass(HasIFGE.class)
                .firstMutantShouldReturn(0, "was < zero");
    }

    @Test
    public void shouldReplaceIFGTWithIFLE() {
        v.forIntFunctionClass(HasIFGT.class)
                .firstMutantShouldReturn(-1, "was > zero");
        v.forIntFunctionClass(HasIFGT.class)
                .firstMutantShouldReturn(1, "was <= zero");
        v.forIntFunctionClass(HasIFGT.class)
                .firstMutantShouldReturn(0, "was > zero");
    }

    @Test
    public void shouldReplaceIFLTWithIFGE() {
        v.forIntFunctionClass(HasIFLT.class)
                .firstMutantShouldReturn(-1, "was >= zero");
        v.forIntFunctionClass(HasIFLT.class)
                .firstMutantShouldReturn(1, "was < zero");
        v.forIntFunctionClass(HasIFLT.class)
                .firstMutantShouldReturn(0, "was < zero");
    }

    @Test
    public void shouldReplaceIFNULLWithIFNONNULL() {
        v.forFunctionClass(HasIFNULL.class)
                .firstMutantShouldReturn(() -> null, "was not null");
        v.forFunctionClass(HasIFNULL.class)
                .firstMutantShouldReturn("foo", "was null");
    }

    @Test
    public void shouldReplaceIFNONNULLWithIFNULL() {
        v.forFunctionClass(HasIFNONNULL.class)
                .firstMutantShouldReturn(() -> null, "was not null");
        v.forFunctionClass(HasIFNONNULL.class)
                .firstMutantShouldReturn("foo", "was null");
    }

    @Test
    public void shouldReplaceIF_ICMPNEWithIF_CMPEQ() {
        v.forIntFunctionClass(HasIF_ICMPNE.class)
                .firstMutantShouldReturn(1, "was zero");
        v.forIntFunctionClass(HasIF_ICMPNE.class)
                .firstMutantShouldReturn(0, "was not zero");
    }

    @Test
    public void shouldReplaceIF_ICMPEQWithIF_CMPNE() {
        v.forIntFunctionClass(HasIF_ICMPEQ.class)
                .firstMutantShouldReturn(1, "was zero");
        v.forIntFunctionClass(HasIF_ICMPEQ.class)
                .firstMutantShouldReturn(0, "was not zero");
    }

    @Test
    public void shouldReplaceHasIF_ICMPLEWithHasIF_ICMPGT() {
        v.forIntFunctionClass(HasIF_ICMPLE.class)
                .firstMutantShouldReturn(1, "was <= zero");
        v.forIntFunctionClass(HasIF_ICMPLE.class)
                .firstMutantShouldReturn(-1, "was > zero");
        v.forIntFunctionClass(HasIF_ICMPLE.class)
                .firstMutantShouldReturn(0, "was > zero");
    }

    @Test
    public void shouldReplaceIF_ICMPGEWithHasIF_ICMPLT() {
        v.forIntFunctionClass(HasIF_ICMPGE.class)
                .firstMutantShouldReturn(-1, "was >= zero");
        v.forIntFunctionClass(HasIF_ICMPGE.class)
                .firstMutantShouldReturn(1, "was < zero");
        v.forIntFunctionClass(HasIF_ICMPGE.class)
                .firstMutantShouldReturn(0, "was < zero");
    }

    @Test
    public void shouldReplaceHasIF_ICMPGTWithHasIF_ICMPLE() {
        v.forIntFunctionClass(HasIF_ICMPGT.class)
                .firstMutantShouldReturn(-1, "was > zero");
        v.forIntFunctionClass(HasIF_ICMPGT.class)
                .firstMutantShouldReturn(1, "was <= zero");
        v.forIntFunctionClass(HasIF_ICMPGT.class)
                .firstMutantShouldReturn(0, "was > zero");
    }

    @Test
    public void shouldReplaceHasIF_ICMPLTWithHasIF_ICMPGE() {
        v.forIntFunctionClass(HasIF_ICMPLT.class)
                .firstMutantShouldReturn(-1, "was >= zero");
        v.forIntFunctionClass(HasIF_ICMPLT.class)
                .firstMutantShouldReturn(1, "was < zero");
        v.forIntFunctionClass(HasIF_ICMPLT.class)
                .firstMutantShouldReturn(0, "was < zero");
    }

    @Test
    public void shouldReplaceIF_ACMPNEWithIF_CMPEQ() {
        v.forFunctionClass(HasIF_ACMPNE.class)
                .firstMutantShouldReturn(String.class,
                        "was integer");
        v.forFunctionClass(HasIF_ACMPNE.class)
                .firstMutantShouldReturn(Integer.class,
                        "was not integer");
    }

    @Test
    public void shouldReplaceIF_ACMPEQWithIF_CMPNE() {
        v.forFunctionClass(HasIF_ACMPEQ.class)
                .firstMutantShouldReturn(String.class,
                        "was integer");
        v.forFunctionClass(HasIF_ACMPEQ.class)
                .firstMutantShouldReturn(Integer.class,
                        "was not integer");
    }

    private static class HasIFEQ implements IntFunction<String> {

        @Override
        public String apply(int i) {
            if (i != 0) {
                return "was not zero";
            } else {
                return "was zero";
            }
        }
    }

    private static class HasIFNE implements IntFunction<String> {
        @Override
        public String apply(int i) {
            if (i == 0) {
                return "was zero";
            } else {
                return "was not zero";
            }
        }
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

    private static class HasIFNULL implements Function<Object, String> {
        @Override
        public String apply(Object i) {
            if (i != null) {
                return "was not null";
            } else {
                return "was null";
            }
        }
    }

    private static class HasIFNONNULL implements Function<Object, String> {

        @Override
        public String apply(Object i) {
            if (i == null) {
                return "was null";
            } else {
                return "was not null";
            }
        }
    }

    private static class HasIF_ICMPNE implements IntFunction<String> {
        @Override
        public String apply(int i) {
            final int j = getZeroButPreventInlining();
            if (i == j) {
                return "was zero";
            } else {
                return "was not zero";
            }
        }
    }

    private static class HasIF_ICMPEQ implements IntFunction<String> {
        @Override
        public String apply(int i) {
            final int j = getZeroButPreventInlining();
            if (i != j) {
                return "was not zero";
            } else {
                return "was zero";
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

    private static class HasIF_ACMPNE implements Function<Object, String> {

        @Override
        public String apply(Object i) {
            final Object integer = Integer.class;
            if (i == integer) {
                return "was integer";
            } else {
                return "was not integer";
            }
        }
    }

    private static class HasIF_ACMPEQ implements Function<Object, String> {
        @Override
        public String apply(Object i) {
            final Object integer = Integer.class;
            if (i != integer) {
                return "was not integer";
            } else {
                return "was integer";
            }
        }
    }

}
