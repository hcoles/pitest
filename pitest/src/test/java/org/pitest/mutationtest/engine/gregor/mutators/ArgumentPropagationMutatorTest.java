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

import org.junit.Before;
import org.junit.Test;
import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.gregor.MutatorTestBase;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.pitest.mutationtest.engine.gregor.mutators.ArgumentPropagationMutator.ARGUMENT_PROPAGATION_MUTATOR;

public class ArgumentPropagationMutatorTest extends MutatorTestBase {

  @Before
  public void setupEngineToUseReplaceMethodWithArgumentOfSameTypeAsReturnValueMutator() {
    createTesteeWith(mutateOnlyCallMethod(), ARGUMENT_PROPAGATION_MUTATOR);
  }

  @Test
  public void shouldReplaceMethodCallWithStringArgument() throws Exception {
    final Mutant mutant = getFirstMutant(HasStringMethodCall.class);
    assertMutantCallableReturns(new HasStringMethodCall("example"), mutant,
        "example");
  }

  private static class HasStringMethodCall implements Callable<String> {
    private String arg;

    public HasStringMethodCall(String arg) {
      this.arg = arg;
    }

    public String delegate(final String aString) {
      return "abc" + aString;
    }

    public String call() throws Exception {
      return delegate(arg);
    }
  }

  @Test
  public void shouldReplaceMethodCallWithIntArgument() throws Exception {
    final Mutant mutant = getFirstMutant(HasIntMethodCall.class);
    assertMutantCallableReturns(new HasIntMethodCall(20), mutant, "20");
  }

  private static class HasIntMethodCall implements Callable<String> {
    private int arg;

    public HasIntMethodCall(int arg) {
      this.arg = arg;
    }

    public int delegate(int aInt) {
      return 22 + aInt;
    }

    public String call() throws Exception {
      return String.valueOf(delegate(arg));
    }
  }

  @Test
  public void shouldReplaceMethodCallWithLongArgument() throws Exception {
    final Mutant mutant = getFirstMutant(HasLongMethodCall.class);
    assertMutantCallableReturns(new HasLongMethodCall(20L), mutant, "20");
  }

  private static class HasLongMethodCall implements Callable<String> {
    private long arg;

    public HasLongMethodCall(long arg) {
      this.arg = arg;
    }

    public long delegate(long argument) {
      return 22L + argument;
    }

    public String call() throws Exception {
      return String.valueOf(delegate(arg));
    }
  }

  @Test
  public void shouldNotMutateMethodThatReturnsDifferentType() throws Exception {
    assertNoMutants(ReturnsDifferentType.class);
  }

  class ReturnsDifferentType implements Callable<String> {
    public String call() {
      return addThreeAndConvertToString(3);
    }

    private String addThreeAndConvertToString(int argument) {
      return String.valueOf(3 + argument);
    }
  }

  @Test
  public void continuesUntilMatchingArgumentTypeIsFound() throws Exception {
    Mutant mutant = getFirstMutant(OnlyFirstArgumentHasMatchingType.class);
    assertMutantCallableReturns(new OnlyFirstArgumentHasMatchingType("abc",
        new Object(), 3), mutant, "abc");
  }

  private class OnlyFirstArgumentHasMatchingType implements Callable<String> {
    private String aString;
    private Object anObject;
    private long   aLong;

    public OnlyFirstArgumentHasMatchingType(String aString, Object anObject,
        long aLong) {
      this.aString = aString;
      this.anObject = anObject;
      this.aLong = aLong;
    }

    public String call() throws Exception {
      return aMethod(aString, anObject, aLong);
    }

    private String aMethod(String aString, Object anObject, long aLong) {
      return String.valueOf(anObject) + aString + String.valueOf(aLong);
    }
  }

  @Test
  public void usesLastArgumentOfMatchingTypeToReplaceMethod() throws Exception {
    Mutant mutant = getFirstMutant(HasSeveralArgumentWithMatchingType.class);
    assertMutantCallableReturns(new HasSeveralArgumentWithMatchingType(11, 22),
        mutant, "22");
  }

  private class HasSeveralArgumentWithMatchingType implements Callable<String> {
    private int int1;
    private int int2;

    public HasSeveralArgumentWithMatchingType(int i, int j) {
      this.int1 = i;
      this.int2 = j;
    }

    public String call() throws Exception {
      String anInt = "3";
      return String.valueOf(aMethod(int1, anInt, int2));
    }

    private int aMethod(int int1, String aString, int int2) {
      return int1 + int2;
    }
  }

  @Test
  public void alsoReplaceCallToMethodWhenReturnValueIsNotUsed()
      throws Exception {
    Mutant mutant = getFirstMutant(ReturnValueNotUsed.class);
    assertMutantCallableReturns(new ReturnValueNotUsed(), mutant, false);
  }

  private class ReturnValueNotUsed implements Callable<Boolean> {
    private List<String> aList = asList("xyz");

    public Boolean call() throws Exception {
      aList.set(0, "will not be present in list in mutated version");
      return aList.contains("will not be present in list in mutated version");
    }
  }

  @Test
  public void shouldReplaceMethodsReturningArraysMatchingArgumentType()
      throws Exception {
    final Mutant mutant = getFirstMutant(HasArrayMethod.class);
    String[] expected = { "1", "2" };
    String[] actual = mutateAndCall(new HasArrayMethod(), mutant);
    assertThat(actual).containsExactly(expected);
  }

  private static class HasArrayMethod implements Callable<String[]> {

    public String[] delegate(final String[] ss) {
      return new String[] {};
    }

    public String[] call() throws Exception {
      String[] s = { "1", "2" };
      return delegate(s);
    }
  }

  @Test
  public void shouldNotReplaceMethodsReturningArraysOfUnmatchedType()
      throws Exception {
    assertNoMutants(HasArrayMethodOfDifferentType.class);
  }

  private static class HasArrayMethodOfDifferentType implements
      Callable<String[]> {

    public String[] delegate(final Integer[] ss) {
      return new String[] {};
    }

    public String[] call() throws Exception {
      Integer[] s = { 1, 2 };
      return delegate(s);
    }
  }

  @Test
  public void willSubstituteCollectionsOfDifferentTypesDueToTypeErasure()
      throws Exception {
    final Mutant mutant = getFirstMutant(HasListMethod.class);
    List<String> expected = Collections.emptyList();
    List<String> actual = mutateAndCall(new HasListMethod(), mutant);
    assertThat(actual).isEqualTo(expected);
  }

  private static class HasListMethod implements Callable<List<String>> {

    public List<String> delegate(final List<Integer> is) {
      return Arrays.asList(new String[] { "foo", "bar" });
    }

    public List<String> call() throws Exception {
      List<Integer> s = Collections.emptyList();
      return delegate(s);
    }
  }

  @Test
  public void shouldReplaceInstanceMethodCallThatIsUsedAsArgumentForCallToOtherObject()
      throws Exception {
    final Mutant mutant = getFirstMutant(
        CallsOtherObjectWithResultOfInstanceMethod.class);
    MyListener listener = new MyListener();
    assertMutantCallableReturns(
        new CallsOtherObjectWithResultOfInstanceMethod("lowercase", listener),
        mutant, "lowercase");
  }

  private class CallsOtherObjectWithResultOfInstanceMethod
      implements Callable<String> {
    private String     arg;
    private MyListener listener;

    public CallsOtherObjectWithResultOfInstanceMethod(String arg,
        MyListener listener) {
      this.arg = arg;
      this.listener = listener;
    }

    private String delegate(String aString) {
      return aString.toUpperCase();
    }

    public String call() throws Exception {
      listener.call(delegate(arg));
      return listener.getCalledWith();
    }
  }

  @Test
  public void shouldReplaceStaticMethodCallThatIsUsedAsArgumentForCallToOtherObject()
      throws Exception {
    final Mutant mutant = getFirstMutant(
        CallsOtherObjectWithResultOfStaticMethod.class);
    MyListener listener = new MyListener();
    assertMutantCallableReturns(
        new CallsOtherObjectWithResultOfStaticMethod("lowercase", listener),
        mutant, "lowercase");
  }

  private static class CallsOtherObjectWithResultOfStaticMethod
      implements Callable<String> {
    private String     arg;
    private MyListener listener;

    public CallsOtherObjectWithResultOfStaticMethod(String arg,
        MyListener listener) {
      this.arg = arg;
      this.listener = listener;
    }

    private static String delegate(int i, String aString, long l) {
      return aString.toUpperCase();
    }

    public String call() throws Exception {
      listener.call(delegate(3, arg, 5L));
      return listener.getCalledWith();
    }
  }

  @Test
  public void shouldReplaceInstanceMethodCallWithSeveralArgumentsThatIsUsedAsArgumentForCallToOtherObject()
      throws Exception {
    final Mutant mutant = getFirstMutant(
        CallsOtherObjectWithResultOfInstanceMethodHavingSeveralArguments.class);
    MyListener listener = new MyListener();
    assertMutantCallableReturns(
        new CallsOtherObjectWithResultOfInstanceMethodHavingSeveralArguments(
            "lowercase", listener),
        mutant, "lowercase");

  }

  private static class CallsOtherObjectWithResultOfInstanceMethodHavingSeveralArguments
      implements Callable<String> {
    private String     arg;
    private MyListener listener;

    public CallsOtherObjectWithResultOfInstanceMethodHavingSeveralArguments(
        String arg,
        MyListener listener) {
      this.arg = arg;
      this.listener = listener;
    }

    private String delegate(int i, double aDouble, Object object, String aString,
        long l) {
      return aString.toUpperCase();
    }

    public String call() throws Exception {
      listener.call(delegate(3, 4.2D, new Object(), arg, 5L));
      return listener.getCalledWith();
    }
  }

  private static class MyListener {
    private String calledWith = "not called";

    public void call(String text) {
      calledWith = text;
    }

    public String getCalledWith() {
      return calledWith;
    }
  }
}
