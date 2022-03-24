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
import org.pitest.mutationtest.engine.gregor.mutators.ConstructorCallMutatorTest.HasConstructorCall;
import org.pitest.verifier.mutants.MutatorVerifierStart;

import java.util.concurrent.Callable;
import java.util.logging.Logger;

import static org.pitest.mutationtest.engine.gregor.mutators.NonVoidMethodCallMutator.NON_VOID_METHOD_CALLS;

public class NonVoidMethodCallMutatorTest {

    MutatorVerifierStart v = MutatorVerifierStart.forMutator(NON_VOID_METHOD_CALLS)
            .notCheckingUnMutatedValues();


    @Test
    public void shouldRemoveNonVoidMethods() {
        v.forCallableClass(HasIntMethodCall.class)
                .firstMutantShouldReturn("0");
    }

    @Test
    public void shouldNotRemoveVoidMethodCalls() {
        v.forClass(HasVoidMethodCall.class)
                .noMutantsCreated();
    }

    @Test
    public void shouldNotRemoveConstructorCalls() {
        v.consideringOnlyMutantsMatching(m -> m.getDescription().contains("Integer"))
                .forClass(HasConstructorCall.class)
                .noMutantsCreated();
    }

    @Test
    public void shouldRemoveNonVoidMethodCallReturningObjectType() {
        v.forCallableClass(HasObjectMethodCall.class)
                .firstMutantShouldReturn(null);
    }

    @Test
    public void shouldRemoveNonVoidMethodCallReturningBooleanType() {
        v.forCallableClass(HasBooleanMethodCall.class)
                .firstMutantShouldReturn("false");
    }

    @Test
    public void shouldRemoveNonVoidMethodCallReturningDoubleType() {
        v.forCallableClass(HasDoubleMethodCall.class)
                .firstMutantShouldReturn("0.0");
    }

    @Test
    public void shouldRemoveNonVoidMethodCallReturningByteType() {
        v.forCallableClass(HasByteMethodCall.class)
                .firstMutantShouldReturn("0");
    }

    @Test
    public void shouldRemoveNonVoidMethodCallReturningCharType() {
        v.forCallableClass(HasCharMethodCall.class)
                .firstMutantShouldReturn("" + '\0');
    }

    @Test
    public void shouldRemoveNonVoidMethodCallReturningShortType() {
        v.forCallableClass(HasShortMethodCall.class)
                .firstMutantShouldReturn("0");
    }

    @Test
    public void shouldRemoveNonVoidMethodCallReturningLongType() {
        v.forCallableClass(HasLongMethodCall.class)
                .firstMutantShouldReturn("0");
    }

    @Test
    public void shouldRemoveNonVoidMethodCallReturningFloatType() {
        v.forCallableClass(HasFloatMethodCall.class)
                .firstMutantShouldReturn("0.0");
    }

    @Test(expected = RuntimeException.class)
    public void canCauseNullPointerExceptionWhenMethodCallRemoved() {
        v.forCallableClass(UsesReturnValueOfMethodCall.class)
                .firstMutantShouldReturn("?");
    }

    @Test
    public void shouldReplaceAssignmentsFromIntMethodCallsWithZero() {
        v.forCallableClass(HasIntMethodCall.class)
                .firstMutantShouldReturn("0");
    }

    private static class HasVoidMethodCall implements Callable<String> {
        public void set(final int i) {
        }

        @Override
        public String call() {
            set(1);
            return "";
        }

    }

    private static class HasObjectMethodCall implements Callable<String> {

        @Override
        public String call() {
            return this.toString();
        }
    }

    private static class HasBooleanMethodCall implements Callable<String> {
        private boolean booleanMethod() {
            return true;
        }

        @Override
        public String call() {
            final boolean result = booleanMethod();
            return "" + result;
        }
    }

    private static class HasDoubleMethodCall implements Callable<String> {
        private double doubleMethod() {
            return 9123475.3d;
        }

        @Override
        public String call() {
            final double result = doubleMethod();
            return "" + result;
        }
    }

    private static class HasByteMethodCall implements Callable<String> {
        private byte byteMethod() {
            return 5;
        }

        @Override
        public String call() {
            final byte result = byteMethod();
            return "" + result;
        }
    }

    private static class HasCharMethodCall implements Callable<String> {
        private char charMethod() {
            return 'g';
        }

        @Override
        public String call() {
            final char result = charMethod();
            return "" + result;
        }
    }

    private static class HasShortMethodCall implements Callable<String> {
        private short shortMethod() {
            return 23;
        }

        @Override
        public String call() {
            final short result = shortMethod();
            return "" + result;
        }
    }

    private static class HasLongMethodCall implements Callable<String> {
        private long longMethod() {
            return 23;
        }

        @Override
        public String call() {
            final long result = longMethod();
            return "" + result;
        }
    }

    private static class HasFloatMethodCall implements Callable<String> {
        private float floatMethod() {
            return 23;
        }

        @Override
        public String call() {
            final float result = floatMethod();
            return "" + result;
        }
    }

    private static class UsesReturnValueOfMethodCall implements Callable<String> {
        @Override
        public String call() {
            return this.toString().toUpperCase();
        }
    }

    private static class HasLogger implements Callable<String> {
        @SuppressWarnings("unused")
        private static final Logger log = Logger.getLogger(HasLogger.class.getName());

        @Override
        public String call() {
            return "ok";
        }
    }

    static class HasIntMethodCall implements Callable<String> {

        private static int i = 0;

        public int set(final int newVal) {
            i = newVal;
            return i + 42;
        }

        @Override
        @SuppressWarnings("finally")
        public String call() {
            int c = 2;
            try {
                c = set(1);
            } finally {
                return "" + c;
            }
        }

    }

}
