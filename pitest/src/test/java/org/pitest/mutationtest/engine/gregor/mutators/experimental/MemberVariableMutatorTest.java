/*
 * Copyright 2011 Henry Coles and Stefan Penndorf
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
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pitest.mutationtest.engine.gregor.mutators.experimental;

import org.junit.Test;
import org.pitest.verifier.mutants.MutatorVerifierStart;

import java.util.concurrent.Callable;

import static org.junit.Assert.assertEquals;

/**
 * @author Stefan Penndorf <stefan.penndorf@gmail.com>
 */
public class MemberVariableMutatorTest {

    private static final Object TEST_OBJECT = new Object();
    MutatorVerifierStart v = MutatorVerifierStart.forMutator(new MemberVariableMutator())
            .notCheckingUnMutatedValues();

    @Test
    public void shouldProvideAMeaningfulName() {
        assertEquals("EXPERIMENTAL_MEMBER_VARIABLE",
                new MemberVariableMutator().getName());
    }

    @Test
    public void shouldRemoveAssignmentToMemberVariable() {
        v.forCallableClass(HasMemberVariable.class)
                .firstMutantShouldReturn(null);
    }

    @Test
    public void shouldRemoveAssignmentToFinalMemberVariable() {
        v.forCallableClass(HasFinalMemberVariable.class)
                .firstMutantShouldReturn(null);
    }

    @Test
    public void shouldRemoveAssignmentToFinalPrimitiveMemberVariable() {
        v.forCallableClass(HasFinalPrimitiveMemberVariable.class)
                .firstMutantShouldReturn(0);
    }

    @Test
    public void shouldNotCreateMutationForNonInitializedVariable() {
        v.forClass(NoInit.class)
                .noMutantsCreated();
    }

    @Test
    public void isUnableToCreateConsistentMutationForConstantFinalPrimitiveMember() {
        // Attention: Mutant is created but return value ist still 42!!!
        // Property member2 is mutated (as shown by the reflection stuff, but
        // constant will be inlined by the compiler!
        v.forCallableClass(HasConstantFinalPrimitiveMemberVariable.class)
                .firstMutantShouldReturn("42-0");
    }

    @Test
    public void consumesFromStackTheSameValuesAsPutfieldWouldConsume_SingleWord() {
        v.mutatingOnly(m -> m.getName().equals("call"))
                .forCallableClass(AssignmentAfterSomeValuesOnStack_SingleWord.class)
                .firstMutantShouldReturn("1 102");
    }

    @Test
    public void consumesFromStackTheSameValuesAsPutfieldWouldConsume_TwoWord() {
        v.mutatingOnly(m -> m.getName().equals("call"))
                .forCallableClass(AssignmentAfterSomeValuesOnStack_TwoWord.class)
                .firstMutantShouldReturn("1 102");
    }

    private static class HasMemberVariable implements Callable<Object> {

        private Object member;

        @Override
        public Object call() {
            this.member = TEST_OBJECT;
            return this.member;
        }

    }

    static class HasFinalMemberVariable implements Callable<Integer> {

        private final Integer member2 = 5;

        @Override
        public Integer call() {
            return this.member2;
        }
    }

    static class HasFinalPrimitiveMemberVariable implements Callable<Integer> {

        private final int member2 = preventInlining(5);

        private static int preventInlining(int i) {
            if (1 > 0) {
                return i;
            }
            return Integer.MAX_VALUE;
        }

        @Override
        public Integer call() {
            return this.member2;
        }
    }

    static class NoInit implements Callable<Integer> {
        private int x;

        @Override
        public Integer call() {
            return this.x;
        }
    }

    static class HasConstantFinalPrimitiveMemberVariable implements Callable<String> {

        public final int member2 = 42;

        @Override
        public String call() throws Exception {
            final Class<?> c = getClass();
            final Integer i = (Integer) c.getField("member2").get(this);
            return "" + this.member2 + "-" + i; // will be optimized by compiler to
            // "42-" + i;
        }
    }

    static class AssignmentAfterSomeValuesOnStack_SingleWord implements
            Callable<String> {

        private int member = preventInlining(1);

        @Override
        public String call() {
            // bipush 100
            // aload_0
            // iconst_2
            // dup_x1
            // -> stack: 100, 2, this, 2
            // putfield
            // -> stack: 100, 2
            // iadd
            // istore_1
            final int i = 100 + (this.member = 2);
            return "" + this.member + " " + i;
        }

        private int preventInlining(int i) {
            if (1 > 0) {
                return i;
            }
            return Integer.MAX_VALUE;
        }

    }

    static class AssignmentAfterSomeValuesOnStack_TwoWord implements
            Callable<String> {

        private long member = preventInlining(1);

        private static long preventInlining(long i) {
            if (1 > 0) {
                return i;
            }
            return Long.MAX_VALUE;
        }

        @Override
        public String call() {
            // ldc2_w 100
            // aload_0
            // ldc2_w 2
            // dup2_x1
            // -> stack: 0 100, 0 2, this, 0 2
            // putfield
            // -> stack: 0 100, 0 2
            // ladd
            // lstore_1
            final long i = 100 + (this.member = 2);
            return "" + this.member + " " + i;
        }
    }

}
