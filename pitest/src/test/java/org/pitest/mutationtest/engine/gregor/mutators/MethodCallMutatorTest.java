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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;
import org.pitest.functional.predicate.True;
import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MutatorTestBase;

public class MethodCallMutatorTest extends MutatorTestBase {

  @Before
  public void setupEngineToRemoveVoidMethods() {
    createTesteeWith(mutateOnlyCallMethod(),
        MethodCallMutator.METHOD_CALL_MUTATOR);
  }

  private static class HasVoidMethodCall implements Callable<String> {

    private int i = 0;

    public void set(final int i) {
      this.i = i;
    }

    public String call() throws Exception {
      set(1);
      return "" + this.i;
    }

  }

  @Test
  public void shouldRemoveSimpleVoidMethodCall() throws Exception {
    final Mutant mutant = getFirstMutant(HasVoidMethodCall.class);
    assertMutantCallableReturns(new HasVoidMethodCall(), mutant, "0");
  }

  private static class HasVoidMethodCallWithFinallyBlock implements
      Callable<String> {

    private int i = 0;

    public void set(final int i, final long k, final double l,
        final HasVoidMethodCallWithFinallyBlock m, final String n) {
      this.i = i;
    }

    @SuppressWarnings("finally")
    public String call() throws Exception {
      final double a = 1;
      final String b = "foo";
      try {
        set(1, 2l, a, this, b);
      } finally {
        return "" + this.i;
      }
    }

  }

  @Test
  public void shouldMaintainStack() throws Exception {
    final Mutant mutant = getFirstMutant(HasVoidMethodCallWithFinallyBlock.class);
    assertMutantCallableReturns(new HasVoidMethodCallWithFinallyBlock(),
        mutant, "0");
  }

  private static class HasVoidStaticMethodCall implements Callable<String> {

    private static int i = 0;

    public static void set(final int newVal, final long k, final double l,
        final HasVoidStaticMethodCall m, final String n) {
      i = newVal;
    }

    @SuppressWarnings("finally")
    public String call() throws Exception {
      final double a = 1;
      final String b = "foo";
      try {
        set(1, 2l, a, this, b);
      } finally {
        return "" + i;
      }
    }

  }

  @Test
  public void shouldMaintainStackWhenCallIsStatic() throws Exception {
    final Mutant mutant = getFirstMutant(HasVoidStaticMethodCall.class);
    assertMutantCallableReturns(new HasVoidStaticMethodCall(), mutant, "0");
  }

  private static class HasIntMethodCall implements Callable<String> {

    private static int i = 0;

    public int set(final int newVal) {
      i = newVal;
      return i + 42;
    }

    @SuppressWarnings("finally")
    public String call() throws Exception {
      int c = 2;
      try {
        c = set(1);
      } finally {
        return "" + c;
      }
    }

  }

  @Test
  public void shouldReplaceAssignmentsFromIntMethodCallsWithZero()
      throws Exception {
    final Mutant mutant = getFirstMutant(HasIntMethodCall.class);
    assertMutantCallableReturns(new HasIntMethodCall(), mutant, "0");
  }

  private static class HasBooleanMethodCall implements Callable<String> {

    public boolean set(final int newVal) {
      return true;
    }

    @SuppressWarnings("finally")
    public String call() throws Exception {
      boolean c = true;
      try {
        c = set(1);
      } finally {
        return "" + c;
      }
    }

  }

  @Test
  public void shouldReplaceAssignmentsFromBooleanMethodCallsWithFalse()
      throws Exception {
    final Mutant mutant = getFirstMutant(HasBooleanMethodCall.class);
    assertMutantCallableReturns(new HasBooleanMethodCall(), mutant, "false");
  }

  private static class HasByteMethodCall implements Callable<String> {
    public byte set() {
      return 1;
    }

    public String call() throws Exception {
      byte c = 1;
      c = set();
      return "" + c;

    }

  }

  @Test
  public void shouldReplaceAssignmentsFromByteMethodCallsWithZero()
      throws Exception {
    final Mutant mutant = getFirstMutant(HasByteMethodCall.class);
    assertMutantCallableReturns(new HasByteMethodCall(), mutant, "0");
  }

  private static class HasCharMethodCall implements Callable<String> {
    public char set() {
      return 1;
    }

    public String call() throws Exception {
      char c = 1;
      c = set();
      return "" + c;

    }

  }

  @Test
  public void shouldReplaceAssignmentsFromCharMethodCallsWithZero()
      throws Exception {
    final Mutant mutant = getFirstMutant(HasCharMethodCall.class);
    final char expected = 0;
    assertMutantCallableReturns(new HasCharMethodCall(), mutant, "" + expected);
  }

  private static class HasShortMethodCall implements Callable<String> {
    public short set() {
      return 1;
    }

    public String call() throws Exception {
      short c = 1;
      c = set();
      return "" + c;

    }

  }

  @Test
  public void shouldReplaceAssignmentsFromShortMethodCallsWithZero()
      throws Exception {
    final Mutant mutant = getFirstMutant(HasShortMethodCall.class);

    assertMutantCallableReturns(new HasShortMethodCall(), mutant, "0");
  }

  private static class HasLongMethodCall implements Callable<String> {
    public long set() {
      return 1;
    }

    public String call() throws Exception {
      long c = 1;
      c = set();
      return "" + c;

    }

  }

  @Test
  public void shouldReplaceAssignmentsFromLongMethodCallsWithZero()
      throws Exception {
    final Mutant mutant = getFirstMutant(HasLongMethodCall.class);
    assertMutantCallableReturns(new HasLongMethodCall(), mutant, "0");
  }

  private static class HasFloatMethodCall implements Callable<String> {
    public float set() {
      return 1;
    }

    public String call() throws Exception {
      float c = 1;
      c = set();
      return "" + c;

    }

  }

  @Test
  public void shouldReplaceAssignmentsFromFloatMethodCallsWithZero()
      throws Exception {
    final Mutant mutant = getFirstMutant(HasFloatMethodCall.class);
    assertMutantCallableReturns(new HasFloatMethodCall(), mutant, "0.0");
  }

  private static class HasDoubleMethodCall implements Callable<String> {
    public double set() {
      return 1;
    }

    public String call() throws Exception {
      double c = 1;
      c = set();
      return "" + c;

    }

  }

  @Test
  public void shouldReplaceAssignmentsFromDoubleMethodCallsWithZero()
      throws Exception {
    final Mutant mutant = getFirstMutant(HasDoubleMethodCall.class);
    assertMutantCallableReturns(new HasDoubleMethodCall(), mutant, "0.0");
  }

  private static class HasObjectMethodCall implements Callable<String> {
    public Object set() {
      return "foo";
    }

    public String call() throws Exception {
      final Object c = set();
      return "" + c;

    }

  }

  @Test
  public void shouldReplaceAssignmentsFromObjectMethodCallsWithNull()
      throws Exception {
    final Mutant mutant = getFirstMutant(HasObjectMethodCall.class);
    assertMutantCallableReturns(new HasObjectMethodCall(), mutant, "null");
  }

  private static class HasLogger implements Callable<String> {
    @SuppressWarnings("unused")
    private static Logger log = Logger.getLogger(HasLogger.class.getName());

    public String call() throws Exception {
      return "ok";
    }
  }

  @Test
  public void shouldNotGenerateRunErrorsWhenMutatingLoggers() throws Exception {
    createTesteeWith(True.<MethodInfo> all(),
        MethodCallMutator.METHOD_CALL_MUTATOR);
    assertTrue(this.findMutationsFor(HasLogger.class).isEmpty());

  }

  private static class HasConstructorCall implements Callable<String> {
    public String call() throws Exception {
      final Integer i = new Integer(12);
      return "" + (i == null);
    }
  }

  @Test
  public void shouldReplaceConstructorCallsWithNullValue() throws Exception {
    final Mutant mutant = getFirstMutant(HasConstructorCall.class);
    assertMutantCallableReturns(new HasConstructorCall(), mutant, "true");
  }

  private static class HasArrayListConstructor implements Callable<String> {

    private List<String> list;

    public String call() throws Exception {

      this.list = new ArrayList<String>();

      return "" + this.list;
    }
  }

  @Test
  public void shouldCreateViableClassWhenMutatingArrayListConstructor()
      throws Exception {
    final Mutant mutant = getFirstMutant(HasArrayListConstructor.class);
    assertMutantCallableReturns(new HasArrayListConstructor(), mutant, "null");
  }

  @Test
  public void shouldNotRemoveCallsToSuper() throws Exception {
    createTesteeWith(True.<MethodInfo> all(),
        MethodCallMutator.METHOD_CALL_MUTATOR);
    assertFalse(findMutationsFor(HasConstructorCall.class).contains(
        descriptionContaining("java/lang/Object::<init>")));
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

    public String call() throws Exception {
      return "" + this.i;
    }

  }

  @Test
  public void shouldNotRemoveCallsToDelegateContructor() throws Exception {
    createTesteeWith(True.<MethodInfo> all(),
        MethodCallMutator.METHOD_CALL_MUTATOR);
    assertFalse(findMutationsFor(HasDelegateConstructorCall.class).contains(
        descriptionContaining("HasDelegateConstructorCall::<init>")));
  }

}
