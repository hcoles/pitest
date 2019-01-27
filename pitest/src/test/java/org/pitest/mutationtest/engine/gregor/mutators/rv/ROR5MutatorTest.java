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
package org.pitest.mutationtest.engine.gregor.mutators.rv;

import org.junit.Before;
import org.junit.Test;
import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.gregor.MutatorTestBase;
import org.pitest.mutationtest.engine.gregor.mutators.rv.ROR5Mutator;

import java.util.concurrent.Callable;

public class ROR5MutatorTest extends MutatorTestBase {

    @Before
    public void setupEngineToMutateOnlyConditionals() {
        createTesteeWith(ROR5Mutator.ROR_5_MUTATOR);
    }

    private static int getZeroButPreventInlining() {
        return 0;
    }

    private static class HasIFLT implements Callable<String> {
        private final int i;

        HasIFLT(final int i) {
            this.i = i;
        }

        @Override
        public String call() {
            if (this.i >= 0) {
                return "was >= zero";
            } else {
                return "was < zero";
            }
        }
    }

    @Test
    public void shouldReplaceIFLTWithIFNE() throws Exception {
        final Mutant mutant = getFirstMutant(HasIFLT.class);
        assertMutantCallableReturns(new HasIFLT(1), mutant, "was < zero");
        assertMutantCallableReturns(new HasIFLT(0), mutant, "was >= zero");
        assertMutantCallableReturns(new HasIFLT(-1), mutant, "was < zero");
    }

    private static class HasIF_ICMPLT implements Callable<String> {
        private final int i;

        HasIF_ICMPLT(final int i) {
            this.i = i;
        }

        @Override
        public String call() {
            final int j = getZeroButPreventInlining();
            if (this.i >= j) {
                return "was >= zero";
            } else {
                return "was < zero";
            }
        }
    }

    @Test
    public void shouldReplaceIF_ICMPLTWithIF_ICMPNE() throws Exception {
        final Mutant mutant = getFirstMutant(HasIF_ICMPLT.class);
        assertMutantCallableReturns(new HasIF_ICMPLT(1), mutant, "was < zero");
        assertMutantCallableReturns(new HasIF_ICMPLT(0), mutant, "was >= zero");
        assertMutantCallableReturns(new HasIF_ICMPLT(-1), mutant, "was < zero");
    }

    private static class HasIFLE implements Callable<String> {
        private final int i;

        HasIFLE(final int i) {
            this.i = i;
        }

        @Override
        public String call() {
            if (this.i > 0) {
                return "was > zero";
            } else {
                return "was <= zero";
            }
        }
    }

    @Test
    public void shouldReplaceIFLEWithIFNE() throws Exception {
        final Mutant mutant = getFirstMutant(HasIFLE.class);
        assertMutantCallableReturns(new HasIFLE(1), mutant, "was <= zero");
        assertMutantCallableReturns(new HasIFLE(0), mutant, "was > zero");
        assertMutantCallableReturns(new HasIFLE(-1), mutant, "was <= zero");
    }

    private static class HasIF_ICMPLE implements Callable<String> {
        private final int i;

        HasIF_ICMPLE(final int i) {
            this.i = i;
        }

        @Override
        public String call() {
            final int j = getZeroButPreventInlining();
            if (this.i > j) {
                return "was > zero";
            } else {
                return "was <= zero";
            }
        }
    }

    @Test
    public void shouldReplaceIF_ICMPLEWithIF_ICMPNE() throws Exception {
        final Mutant mutant = getFirstMutant(HasIF_ICMPLE.class);
        assertMutantCallableReturns(new HasIF_ICMPLE(1), mutant, "was <= zero");
        assertMutantCallableReturns(new HasIF_ICMPLE(0), mutant, "was > zero");
        assertMutantCallableReturns(new HasIF_ICMPLE(-1), mutant, "was <= zero");
    }

    private static class HasIFGT implements Callable<String> {
        private final int i;

        HasIFGT(final int i) {
            this.i = i;
        }

        @Override
        public String call() {
            if (this.i <= 0) {
                return "was <= zero";
            } else {
                return "was > zero";
            }
        }
    }

    @Test
    public void shouldReplaceIFGTWithIFNE() throws Exception {
        final Mutant mutant = getFirstMutant(HasIFGT.class);
        assertMutantCallableReturns(new HasIFGT(1), mutant, "was > zero");
        assertMutantCallableReturns(new HasIFGT(0), mutant, "was <= zero");
        assertMutantCallableReturns(new HasIFGT(-1), mutant, "was > zero");
    }

    private static class HasIF_ICMPGT implements Callable<String> {
        private final int i;

        HasIF_ICMPGT(final int i) {
            this.i = i;
        }

        @Override
        public String call() {
            final int j = getZeroButPreventInlining();
            if (this.i <= j) {
                return "was <= zero";
            } else {
                return "was > zero";
            }
        }
    }

    @Test
    public void shouldReplaceIF_ICMPGTWithIF_ICMPNE() throws Exception {
        final Mutant mutant = getFirstMutant(HasIF_ICMPGT.class);
        assertMutantCallableReturns(new HasIF_ICMPGT(1), mutant, "was > zero");
        assertMutantCallableReturns(new HasIF_ICMPGT(0), mutant, "was <= zero");
        assertMutantCallableReturns(new HasIF_ICMPGT(-1), mutant, "was > zero");
    }

    private static class HasIFGE implements Callable<String> {
        private final int i;

        HasIFGE(final int i) {
            this.i = i;
        }

        @Override
        public String call() {
            if (this.i < 0) {
                return "was < zero";
            } else {
                return "was >= zero";
            }
        }
    }

    @Test
    public void shouldReplaceIFGEWithIFNE() throws Exception {
        final Mutant mutant = getFirstMutant(HasIFGE.class);
        assertMutantCallableReturns(new HasIFGE(1), mutant, "was >= zero");
        assertMutantCallableReturns(new HasIFGE(0), mutant, "was < zero");
        assertMutantCallableReturns(new HasIFGE(-1), mutant, "was >= zero");
    }

    private static class HasIF_ICMPGE implements Callable<String> {
        private final int i;

        HasIF_ICMPGE(final int i) {
            this.i = i;
        }

        @Override
        public String call() {
            final int j = getZeroButPreventInlining();
            if (this.i < j) {
                return "was < zero";
            } else {
                return "was >= zero";
            }
        }
    }

    @Test
    public void shouldReplaceIF_ICMPGEWithIF_ICMPNE() throws Exception {
        final Mutant mutant = getFirstMutant(HasIF_ICMPGE.class);
        assertMutantCallableReturns(new HasIF_ICMPGE(1), mutant, "was >= zero");
        assertMutantCallableReturns(new HasIF_ICMPGE(0), mutant, "was < zero");
        assertMutantCallableReturns(new HasIF_ICMPGE(-1), mutant, "was >= zero");
    }

    private static class HasIFEQ implements Callable<String> {
        private final int i;

        HasIFEQ(final int i) {
            this.i = i;
        }

        @Override
        public String call() {
            if (this.i != 0) {
                return "was not zero";
            } else {
                return "was zero";
            }
        }
    }

    @Test
    public void shouldReplaceIFEQWithIFNE() throws Exception {
        final Mutant mutant = getFirstMutant(HasIFEQ.class);
        assertMutantCallableReturns(new HasIFEQ(1), mutant, "was zero");
        assertMutantCallableReturns(new HasIFEQ(0), mutant, "was not zero");
        assertMutantCallableReturns(new HasIFEQ(-1), mutant, "was zero");
    }

    private static class HasIF_ICMPEQ implements Callable<String> {
        private final int i;

        HasIF_ICMPEQ(final int i) {
            this.i = i;
        }

        @Override
        public String call() {
            final int j = getZeroButPreventInlining();
            if (this.i != j) {
                return "was not zero";
            } else {
                return "was zero";
            }
        }
    }

    @Test
    public void shouldReplaceIF_ICMPEQWithIF_CMPNE() throws Exception {
        final Mutant mutant = getFirstMutant(HasIF_ICMPEQ.class);
        assertMutantCallableReturns(new HasIF_ICMPEQ(1), mutant, "was zero");
        assertMutantCallableReturns(new HasIF_ICMPEQ(0), mutant, "was not zero");
        assertMutantCallableReturns(new HasIF_ICMPEQ(-1), mutant, "was zero");
    }


    private static class HasIFNULL implements Callable<String> {
        private final Object i;

        HasIFNULL(final Object i) {
            this.i = i;
        }

        @Override
        public String call() {
            if (this.i != null) {
                return "was not null";
            } else {
                return "was null";
            }
        }
    }

    @Test
    public void shouldReplaceIFNULLWithIFNONNULL() throws Exception {
        final Mutant mutant = getFirstMutant(HasIFNULL.class);
        assertMutantCallableReturns(new HasIFNULL(null), mutant, "was not null");
        assertMutantCallableReturns(new HasIFNULL("foo"), mutant, "was null");
    }

    private static class HasIF_ACMPEQ implements Callable<String> {
        private final Object i;

        HasIF_ACMPEQ(final Object i) {
            this.i = i;
        }

        @Override
        public String call() {
            final Object integer = Integer.class;
            if (this.i != integer) {
                return "was not integer";
            } else {
                return "was integer";
            }
        }
    }

    @Test
    public void shouldReplaceIF_ACMPEQWithIF_CMPNE() throws Exception {
        final Mutant mutant = getFirstMutant(HasIF_ACMPEQ.class);
        assertMutantCallableReturns(new HasIF_ACMPEQ(String.class), mutant,
                "was integer");
        assertMutantCallableReturns(new HasIF_ACMPEQ(Integer.class), mutant,
                "was not integer");
    }

    private static class HasIFNE implements Callable<String> {
        private final int i;

        HasIFNE(final int i) {
            this.i = i;
        }

        @Override
        public String call() {
            if (this.i == 0) {
                return "was zero";
            } else {
                return "was not zero";
            }
        }
    }

    @Test
    public void shouldReplaceIFNEWithIFEQ() throws Exception {
        final Mutant mutant = getFirstMutant(HasIFNE.class);
        assertMutantCallableReturns(new HasIFNE(1), mutant, "was zero");
        assertMutantCallableReturns(new HasIFNE(0), mutant, "was not zero");
        assertMutantCallableReturns(new HasIFNE(-1), mutant, "was zero");
    }

    private static class HasIF_ICMPNE implements Callable<String> {
        private final int i;

        HasIF_ICMPNE(final int i) {
            this.i = i;
        }

        @Override
        public String call() {
            final int j = getZeroButPreventInlining();
            if (this.i == j) {
                return "was zero";
            } else {
                return "was not zero";
            }
        }
    }

    @Test
    public void shouldReplaceIF_ICMPNEWithIF_CMPEQ() throws Exception {
        final Mutant mutant = getFirstMutant(HasIF_ICMPNE.class);
        assertMutantCallableReturns(new HasIF_ICMPNE(1), mutant, "was zero");
        assertMutantCallableReturns(new HasIF_ICMPNE(0), mutant, "was not zero");
        assertMutantCallableReturns(new HasIF_ICMPNE(-1), mutant, "was zero");
    }

    private static class HasIFNONNULL implements Callable<String> {
        private final Object i;

        HasIFNONNULL(final Object i) {
            this.i = i;
        }

        @Override
        public String call() {
            if (this.i == null) {
                return "was null";
            } else {
                return "was not null";
            }
        }
    }

    @Test
    public void shouldReplaceIFNONNULLWithIFNULL() throws Exception {
        final Mutant mutant = getFirstMutant(HasIFNONNULL.class);
        assertMutantCallableReturns(new HasIFNONNULL(null), mutant, "was not null");
        assertMutantCallableReturns(new HasIFNONNULL("foo"), mutant, "was null");
    }

    private static class HasIF_ACMPNE implements Callable<String> {
        private final Object i;

        HasIF_ACMPNE(final Object i) {
            this.i = i;
        }

        @Override
        public String call() {
            final Object integer = Integer.class;
            if (this.i == integer) {
                return "was integer";
            } else {
                return "was not integer";
            }
        }
    }

    @Test
    public void shouldReplaceIF_ACMPNEWithIF_CMPEQ() throws Exception {
        final Mutant mutant = getFirstMutant(HasIF_ACMPNE.class);
        printMutant(mutant);
        assertMutantCallableReturns(new HasIF_ACMPNE(String.class), mutant,
                "was integer");
        assertMutantCallableReturns(new HasIF_ACMPNE(Integer.class), mutant,
                "was not integer");
    }
}
