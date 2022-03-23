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
import org.pitest.mutationtest.engine.gregor.mutators.NonVoidMethodCallMutatorTest.HasIntMethodCall;
import org.pitest.mutationtest.engine.gregor.mutators.VoidMethodCallMutatorTest.HasVoidMethodCall;
import org.pitest.verifier.mutants.MutatorVerifierStart;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static org.pitest.mutationtest.engine.gregor.mutators.ConstructorCallMutator.CONSTRUCTOR_CALLS;

public class ConstructorCallMutatorTest {

    MutatorVerifierStart v = MutatorVerifierStart.forMutator(CONSTRUCTOR_CALLS)
            .notCheckingUnMutatedValues();

    @Test
    public void shouldReplaceConstructorCallsWithNullValue() {
        v.forCallableClass(HasConstructorCall.class)
                .firstMutantShouldReturn("true");
    }

    @Test
    public void shouldNotRemoveVoidMethodCalls() {
        v.consideringOnlyMutantsMatching(m -> m.getMethod().startsWith("set"))
                .forCallableClass(HasVoidMethodCall.class)
                .noMutantsCreated();
    }

    @Test
    public void shouldNotRemoveNonVoidMethods() {
        v.consideringOnlyMutantsMatching(m -> m.getMethod().startsWith("set"))
                .forCallableClass(HasIntMethodCall.class)
                .noMutantsCreated();
    }

    @Test
    public void shouldNotRemoveCallsToSuper() {
        v.consideringOnlyMutantsMatching(m -> m.getDescription().contains("java/lang/Object::<init>"))
                .forCallableClass(HasConstructorCall.class)
                .noMutantsCreated();
    }

    @Test
    public void shouldNotRemoveCallsToDelegateContructor() {
        v.consideringOnlyMutantsMatching(m -> m.getDescription().contains("HasDelegateConstructorCall::<init>"))
                .forCallableClass(HasDelegateConstructorCall.class)
                .noMutantsCreated();
    }

    @Test
    public void shouldCreateViableClassWhenMutatingArrayListConstructor() {
        v.forCallableClass(HasArrayListConstructor.class)
                .firstMutantShouldReturn("null");
    }

    static class HasConstructorCall implements Callable<String> {
        @Override
        public String call() throws Exception {
            final Integer i = new Integer(12);
            return "" + (i == null);
        }
    }

    private static class HasDelegateConstructorCall implements Callable<String> {

        private final int i;

        @SuppressWarnings("unused")
        HasDelegateConstructorCall() {
            this(1);
        }

        HasDelegateConstructorCall(final int i) {
            this.i = i;
        }

        @Override
        public String call() throws Exception {
            return "" + this.i;
        }

    }

    private static class HasArrayListConstructor implements Callable<String> {

        private List<String> list;

        @Override
        public String call() throws Exception {

            this.list = new ArrayList<>();

            return "" + this.list;
        }
    }


}
