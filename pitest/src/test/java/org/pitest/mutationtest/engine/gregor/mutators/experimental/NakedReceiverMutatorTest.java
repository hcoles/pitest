/*
 * Copyright 2015 Urs Metz
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
import java.util.function.Function;

import static org.pitest.mutationtest.engine.gregor.mutators.experimental.NakedReceiverMutator.EXPERIMENTAL_NAKED_RECEIVER;


public class NakedReceiverMutatorTest {

    MutatorVerifierStart v = MutatorVerifierStart.forMutator(EXPERIMENTAL_NAKED_RECEIVER)
            .notCheckingUnMutatedValues();


    @Test
    public void shouldReplaceMethodCallOnString() {
        v.forFunctionClass(HasStringMethodCall.class)
                .firstMutantShouldReturn("EXAMPLE", "EXAMPLE");
    }

    @Test
    public void shouldNotReplaceMethodCallWhenDifferentReturnType() {
        v.forClass(HasMethodWithDifferentReturnType.class)
                .noMutantsCreated();
    }

    @Test
    public void shouldNotMutateVoidMethodCall() {
        v.forClass(HasVoidMethodCall.class)
                .noMutantsCreated();

    }

    @Test
    public void shouldNotMutateStaticMethodCall() {
        v.forClass(HasStaticMethodCallWithSameType.class)
                .noMutantsCreated();
    }

    @Test
    public void shouldRemoveDslMethods() {
        v.forCallableClass(HasDslMethodCall.class)
                .firstMutantShouldReturn("HasDslMethodCall [i=3]");
    }

    private static class HasStringMethodCall implements Function<String, String> {
        @Override
        public String apply(String arg) {
            return arg.toLowerCase();
        }
    }

    static class HasMethodWithDifferentReturnType {
        public int call() {
            return "".length();
        }
    }

    static class HasVoidMethodCall {
        public void call() {
        }
    }

    static class HasStaticMethodCallWithSameType {
        private static HasStaticMethodCallWithSameType instance() {
            return new HasStaticMethodCallWithSameType();
        }

        public void call() {
            HasStaticMethodCallWithSameType.instance();
        }
    }

    static class HasDslMethodCall implements Callable<String> {

        private int i = 0;

        public HasDslMethodCall chain(final int newVal) {
            this.i += newVal;
            return this;
        }

        public void voidNonDsl(final int newVal) {
            this.i += newVal;
        }

        public int nonDsl(final int newVal) {
            this.i += newVal;
            return this.i;
        }

        @Override
        public String call() {
            final HasDslMethodCall dsl = this;
            dsl.chain(1).nonDsl(3);
            return "" + dsl;
        }

        @Override
        public String toString() {
            return "HasDslMethodCall [i=" + this.i + "]";
        }

    }
}
