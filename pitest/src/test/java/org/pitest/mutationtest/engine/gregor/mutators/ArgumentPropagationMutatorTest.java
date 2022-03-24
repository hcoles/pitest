/*
 * Copyright 2014 Stefan Mandel, Urs Metz
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.LongFunction;

import static java.util.Arrays.asList;
import static org.pitest.mutationtest.engine.gregor.mutators.experimental.ArgumentPropagationMutator.EXPERIMENTAL_ARGUMENT_PROPAGATION;

public class ArgumentPropagationMutatorTest {

    MutatorVerifierStart v = MutatorVerifierStart.forMutator(EXPERIMENTAL_ARGUMENT_PROPAGATION)
            .notCheckingUnMutatedValues();

    @Test
    public void shouldReplaceMethodCallWithStringArgument() {
        v.forFunctionClass(HasStringMethodCall.class)
                .firstMutantShouldReturn(() -> "example", "example");
    }

    @Test
    public void shouldReplaceMethodCallWithIntArgument() {
        v.forIntFunctionClass(HasIntMethodCall.class)
                .firstMutantShouldReturn(() -> 20, "20");
    }

    @Test
    public void shouldReplaceMethodCallWithLongArgument() {
        v.forLongFunctionClass(HasLongMethodCall.class)
                .firstMutantShouldReturn(() -> 20L, "20");
    }

    @Test
    public void shouldNotMutateMethodThatReturnsDifferentType() {
        v.forClass(ReturnsDifferentType.class)
                .noMutantsCreated();
    }

    @Test
    public void continuesUntilMatchingArgumentTypeIsFound() {
        v.forCallableClass(OnlyFirstArgumentHasMatchingType.class)
                .firstMutantShouldReturn("abc");

    }

    @Test
    public void usesLastArgumentOfMatchingTypeToReplaceMethod() {
        v.forBiFunctionClass(HasSeveralArgumentWithMatchingType.class)
                .firstMutantShouldReturn(11, 22, "22");
    }

    @Test
    public void alsoReplaceCallToMethodWhenReturnValueIsNotUsed() {
        v.forFunctionClass(ReturnValueNotUsed.class)
                .firstMutantShouldReturn(() -> asList("xyz"), false);
    }

    @Test
    public void shouldReplaceMethodsReturningArraysMatchingArgumentType() {
        final String[] expected = {"1", "2"};
        v.forCallableClass(HasArrayMethod.class)
                .firstMutantShouldReturn(expected);
    }

    @Test
    public void shouldNotReplaceMethodsReturningArraysOfUnmatchedType() {
        v.forClass(HasArrayMethodOfDifferentType.class)
                .noMutantsCreated();
    }

    @Test
    public void willSubstituteCollectionsOfDifferentTypesDueToTypeErasure() {
        v.forCallableClass(HasListMethod.class)
                .firstMutantShouldReturn(Collections.emptyList());
    }

    @Test
    public void shouldReplaceInstanceMethodCallThatIsUsedAsArgumentForCallToOtherObject() {
        v.forCallableClass(CallsOtherObjectWithResultOfInstanceMethod.class)
                .firstMutantShouldReturn("lowercase");
    }

    @Test
    public void shouldReplaceStaticMethodCallThatIsUsedAsArgumentForCallToOtherObject() {
        v.forCallableClass(CallsOtherObjectWithResultOfStaticMethod.class)
                .firstMutantShouldReturn("lowercase");
    }

    @Test
    public void shouldReplaceInstanceMethodCallWithSeveralArgumentsThatIsUsedAsArgumentForCallToOtherObject() {
        v.forCallableClass(CallsOtherObjectWithResultOfInstanceMethodHavingSeveralArguments.class)
                .firstMutantShouldReturn("lowercase");

    }

    private static class HasStringMethodCall implements Function<String, String> {

        public String delegate(final String aString) {
            return "abc" + aString;
        }

        public String apply(String arg) {
            return delegate(arg);
        }
    }

    private static class HasIntMethodCall implements IntFunction<String> {

        public int delegate(int aInt) {
            return 22 + aInt;
        }

        @Override
        public String apply(int arg) {
            return String.valueOf(delegate(arg));
        }
    }

    private static class HasLongMethodCall implements LongFunction<String> {

        public long delegate(long argument) {
            return 22L + argument;
        }

        @Override
        public String apply(long arg) {
            return String.valueOf(delegate(arg));
        }
    }

    private static class OnlyFirstArgumentHasMatchingType implements Callable<String> {
        @Override
        public String call() {
            return aMethod("abc", new Object(), 3);
        }

        private String aMethod(String aString, Object anObject, long aLong) {
            return String.valueOf(anObject) + aString + String.valueOf(aLong);
        }
    }

    private static class HasSeveralArgumentWithMatchingType implements BiFunction<Integer, Integer, String> {
        @Override
        public String apply(Integer i1, Integer i2) {
            int int1 = i1;
            int int2 = i2;
            final String anInt = "3";
            return String.valueOf(aMethod(int1, anInt, int2));
        }

        private int aMethod(int int1, String aString, int int2) {
            return int1 + int2;
        }
    }

    private static class ReturnValueNotUsed implements Function<List<String>, Boolean> {
        @Override
        public Boolean apply(List<String> aList) {
            aList.set(0, "will not be present in list in mutated version");
            return aList
                    .contains("will not be present in list in mutated version");
        }
    }

    private static class HasArrayMethod implements Callable<String[]> {

        public String[] delegate(final String[] ss) {
            return new String[]{};
        }

        @Override
        public String[] call() {
            final String[] s = {"1", "2"};
            return delegate(s);
        }
    }

    private static class HasArrayMethodOfDifferentType implements
            Callable<String[]> {

        public String[] delegate(final Integer[] ss) {
            return new String[]{};
        }

        @Override
        public String[] call() {
            final Integer[] s = {1, 2};
            return delegate(s);
        }
    }

    private static class HasListMethod implements Callable<List<String>> {

        public List<String> delegate(final List<Integer> is) {
            return Arrays.asList(new String[]{"foo", "bar"});
        }

        @Override
        public List<String> call() {
            final List<Integer> s = Collections.emptyList();
            return delegate(s);
        }
    }

    private static class CallsOtherObjectWithResultOfInstanceMethod implements
            Callable<String> {
        private final MyListener listener = new MyListener();

        private String delegate(String aString) {
            return aString.toUpperCase();
        }

        @Override
        public String call() {
            this.listener.call(delegate("lowercase"));
            return this.listener.getCalledWith();
        }
    }

    private static class CallsOtherObjectWithResultOfStaticMethod implements
            Callable<String> {
        private final MyListener listener = new MyListener();

        private static String delegate(int i, String aString, long l) {
            return aString.toUpperCase();
        }

        @Override
        public String call() {
            this.listener.call(delegate(3, "lowercase", 5L));
            return this.listener.getCalledWith();
        }
    }

    private static class CallsOtherObjectWithResultOfInstanceMethodHavingSeveralArguments
            implements Callable<String> {
        private final MyListener listener = new MyListener();

        private String delegate(int i, double aDouble, Object object,
                                String aString, long l) {
            return aString.toUpperCase();
        }

        @Override
        public String call() {
            this.listener.call(delegate(3, 4.2D, new Object(), "lowercase", 5L));
            return this.listener.getCalledWith();
        }
    }

    private static class MyListener {
        private String calledWith = "not called";

        public void call(String text) {
            this.calledWith = text;
        }

        public String getCalledWith() {
            return this.calledWith;
        }
    }

    class ReturnsDifferentType implements Callable<String> {
        @Override
        public String call() {
            return addThreeAndConvertToString(3);
        }

        private String addThreeAndConvertToString(int argument) {
            return String.valueOf(3 + argument);
        }
    }
}
