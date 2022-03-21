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

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.pitest.mutationtest.engine.gregor.mutators.experimental.ArgumentPropagationMutator.EXPERIMENTAL_ARGUMENT_PROPAGATION;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.LongFunction;

import org.junit.Before;
import org.junit.Test;
import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.gregor.MutatorTestBase;
import org.pitest.verifier.mutants.MutatorVerifierStart;

public class ArgumentPropagationMutatorTest extends MutatorTestBase {

  MutatorVerifierStart v = MutatorVerifierStart.forMutator(EXPERIMENTAL_ARGUMENT_PROPAGATION);

  @Before
  public void setupEngineToUseReplaceMethodWithArgumentOfSameTypeAsReturnValueMutator() {
    createTesteeWith(mutateOnlyCallMethod(), EXPERIMENTAL_ARGUMENT_PROPAGATION);
  }

  @Test
  public void shouldReplaceMethodCallWithStringArgument() {
    v.forFunctionClass(HasStringMethodCall.class)
            .firstMutantShouldReturn(() -> "example", "example");
  }

  private static class HasStringMethodCall implements Function<String, String> {

    public String delegate(final String aString) {
      return "abc" + aString;
    }

    public String apply(String arg) {
      return delegate(arg);
    }
  }

  @Test
  public void shouldReplaceMethodCallWithIntArgument() {
    v.forIntFunctionClass(HasIntMethodCall.class)
                    .firstMutantShouldReturn(() -> 20, "20");
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

  @Test
  public void shouldReplaceMethodCallWithLongArgument() {
    v.forLongFunctionClass(HasLongMethodCall.class)
            .firstMutantShouldReturn(() -> 20L, "20");
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

  @Test
  public void shouldNotMutateMethodThatReturnsDifferentType() {
    v.forClass(ReturnsDifferentType.class)
                    .noMutantsCreated();
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

  @Test
  public void continuesUntilMatchingArgumentTypeIsFound() throws Exception {
    final Mutant mutant = getFirstMutant(OnlyFirstArgumentHasMatchingType.class);
    assertMutantCallableReturns(new OnlyFirstArgumentHasMatchingType("abc",
        new Object(), 3), mutant, "abc");
  }

  private static class OnlyFirstArgumentHasMatchingType implements Callable<String> {
    private final String aString;
    private final Object anObject;
    private final long   aLong;

    public OnlyFirstArgumentHasMatchingType(String aString, Object anObject,
        long aLong) {
      this.aString = aString;
      this.anObject = anObject;
      this.aLong = aLong;
    }

    @Override
    public String call() throws Exception {
      return aMethod(this.aString, this.anObject, this.aLong);
    }

    private String aMethod(String aString, Object anObject, long aLong) {
      return String.valueOf(anObject) + aString + String.valueOf(aLong);
    }
  }

  @Test
  public void usesLastArgumentOfMatchingTypeToReplaceMethod() throws Exception {
    final Mutant mutant = getFirstMutant(HasSeveralArgumentWithMatchingType.class);
    assertMutantCallableReturns(new HasSeveralArgumentWithMatchingType(11, 22),
        mutant, "22");
  }

  private static class HasSeveralArgumentWithMatchingType implements Callable<String> {
    private final int int1;
    private final int int2;

    public HasSeveralArgumentWithMatchingType(int i, int j) {
      this.int1 = i;
      this.int2 = j;
    }

    @Override
    public String call() throws Exception {
      final String anInt = "3";
      return String.valueOf(aMethod(this.int1, anInt, this.int2));
    }

    private int aMethod(int int1, String aString, int int2) {
      return int1 + int2;
    }
  }

  @Test
  public void alsoReplaceCallToMethodWhenReturnValueIsNotUsed() {
    v.forFunctionClass(ReturnValueNotUsed.class)
                    .firstMutantShouldReturn(() -> asList("xyz"), false);
  }

  private static class ReturnValueNotUsed implements Function<List<String>, Boolean> {
    @Override
    public Boolean apply(List<String> aList) {
      aList.set(0, "will not be present in list in mutated version");
      return aList
          .contains("will not be present in list in mutated version");
    }
  }

  @Test
  public void shouldReplaceMethodsReturningArraysMatchingArgumentType() {
    final Mutant mutant = getFirstMutant(HasArrayMethod.class);
    final String[] expected = { "1", "2" };
    final String[] actual = mutateAndCall(new HasArrayMethod(), mutant);
    assertThat(actual).containsExactly(expected);
  }

  private static class HasArrayMethod implements Callable<String[]> {

    public String[] delegate(final String[] ss) {
      return new String[] {};
    }

    @Override
    public String[] call() throws Exception {
      final String[] s = { "1", "2" };
      return delegate(s);
    }
  }

  @Test
  public void shouldNotReplaceMethodsReturningArraysOfUnmatchedType() {
    v.forClass(HasArrayMethodOfDifferentType.class)
                    .noMutantsCreated();
  }

  private static class HasArrayMethodOfDifferentType implements
  Callable<String[]> {

    public String[] delegate(final Integer[] ss) {
      return new String[] {};
    }

    @Override
    public String[] call() throws Exception {
      final Integer[] s = { 1, 2 };
      return delegate(s);
    }
  }

  @Test
  public void willSubstituteCollectionsOfDifferentTypesDueToTypeErasure() {
    final Mutant mutant = getFirstMutant(HasListMethod.class);
    final List<String> expected = Collections.emptyList();
    final List<String> actual = mutateAndCall(new HasListMethod(), mutant);
    assertThat(actual).isEqualTo(expected);
  }

  private static class HasListMethod implements Callable<List<String>> {

    public List<String> delegate(final List<Integer> is) {
      return Arrays.asList(new String[] { "foo", "bar" });
    }

    @Override
    public List<String> call() throws Exception {
      final List<Integer> s = Collections.emptyList();
      return delegate(s);
    }
  }

  @Test
  public void shouldReplaceInstanceMethodCallThatIsUsedAsArgumentForCallToOtherObject()
      throws Exception {
    final Mutant mutant = getFirstMutant(CallsOtherObjectWithResultOfInstanceMethod.class);
    final MyListener listener = new MyListener();
    assertMutantCallableReturns(new CallsOtherObjectWithResultOfInstanceMethod(
        "lowercase", listener), mutant, "lowercase");
  }

  private static class CallsOtherObjectWithResultOfInstanceMethod implements
      Callable<String> {
    private final String     arg;
    private final MyListener listener;

    public CallsOtherObjectWithResultOfInstanceMethod(String arg,
        MyListener listener) {
      this.arg = arg;
      this.listener = listener;
    }

    private String delegate(String aString) {
      return aString.toUpperCase();
    }

    @Override
    public String call() throws Exception {
      this.listener.call(delegate(this.arg));
      return this.listener.getCalledWith();
    }
  }

  @Test
  public void shouldReplaceStaticMethodCallThatIsUsedAsArgumentForCallToOtherObject()
      throws Exception {
    final Mutant mutant = getFirstMutant(CallsOtherObjectWithResultOfStaticMethod.class);
    final MyListener listener = new MyListener();
    assertMutantCallableReturns(new CallsOtherObjectWithResultOfStaticMethod(
        "lowercase", listener), mutant, "lowercase");
  }

  private static class CallsOtherObjectWithResultOfStaticMethod implements
      Callable<String> {
    private final String     arg;
    private final MyListener listener;

    public CallsOtherObjectWithResultOfStaticMethod(String arg,
        MyListener listener) {
      this.arg = arg;
      this.listener = listener;
    }

    private static String delegate(int i, String aString, long l) {
      return aString.toUpperCase();
    }

    @Override
    public String call() throws Exception {
      this.listener.call(delegate(3, this.arg, 5L));
      return this.listener.getCalledWith();
    }
  }

  @Test
  public void shouldReplaceInstanceMethodCallWithSeveralArgumentsThatIsUsedAsArgumentForCallToOtherObject()
      throws Exception {
    final Mutant mutant = getFirstMutant(CallsOtherObjectWithResultOfInstanceMethodHavingSeveralArguments.class);
    final MyListener listener = new MyListener();
    assertMutantCallableReturns(
        new CallsOtherObjectWithResultOfInstanceMethodHavingSeveralArguments(
            "lowercase", listener), mutant, "lowercase");

  }

  private static class CallsOtherObjectWithResultOfInstanceMethodHavingSeveralArguments
  implements Callable<String> {
    private final String     arg;
    private final MyListener listener;

    public CallsOtherObjectWithResultOfInstanceMethodHavingSeveralArguments(
        String arg, MyListener listener) {
      this.arg = arg;
      this.listener = listener;
    }

    private String delegate(int i, double aDouble, Object object,
        String aString, long l) {
      return aString.toUpperCase();
    }

    @Override
    public String call() throws Exception {
      this.listener.call(delegate(3, 4.2D, new Object(), this.arg, 5L));
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
}
