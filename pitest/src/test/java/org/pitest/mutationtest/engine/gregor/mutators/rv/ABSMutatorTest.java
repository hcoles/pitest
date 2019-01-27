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

import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;
import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.gregor.MutatorTestBase;
import org.pitest.mutationtest.engine.gregor.mutators.rv.ABSMutator;

public class ABSMutatorTest extends MutatorTestBase {

  @Before
  public void setupEngineToMutateOnlyMathFunctions() {
    createTesteeWith(mutateOnlyCallMethod(), ABSMutator.ABS_MUTATOR);
  }

  private static class HasILoad implements Callable<String> {
    @Override
    public String call() {
      int i = 20;
      return "" + i;
    }
  }

  @Test
  public void shouldReplaceIntegerLocalVariable() throws Exception {
    final Mutant mutant = getFirstMutant(HasILoad.class);
    assertMutantCallableReturns(new HasILoad(), mutant, "-20");
  }

  private static class HasFLoad implements Callable<String> {
    @Override
    public String call() {
      float f = 20;
      return "" + f;
    }
  }

  @Test
  public void shouldReplaceFloatLocalVariable() throws Exception {
    final Mutant mutant = getFirstMutant(HasFLoad.class);
    assertMutantCallableReturns(new HasFLoad(), mutant, "-20.0");
  }

  private static class HasLLoad implements Callable<String> {
    @Override
    public String call() {
      long l = 20;
      return "" + l;
    }
  }

  @Test
  public void shouldReplaceLongLocalVariable() throws Exception {
    final Mutant mutant = getFirstMutant(HasLLoad.class);
    assertMutantCallableReturns(new HasLLoad(), mutant, "-20");
  }

  private static class HasDLoad implements Callable<String> {
    @Override
    public String call() {
      double d = 20;
      return "" + d;
    }
  }

  @Test
  public void shouldReplaceDoubleLocalVariable() throws Exception {
    final Mutant mutant = getFirstMutant(HasDLoad.class);
    assertMutantCallableReturns(new HasDLoad(), mutant, "-20.0");
  }

  private static class HasIGetField implements Callable<String> {
    private int i;

    public HasIGetField(int i) {
      this.i = i;
    }

    @Override
    public String call() {
      return "" + this.i;
    }
  }

  @Test
  public void shouldReplaceIntegerField() throws Exception {
    final Mutant mutant = getFirstMutant(HasIGetField.class);
    assertMutantCallableReturns(new HasIGetField(20), mutant, "-20");
    assertMutantCallableReturns(new HasIGetField(-20), mutant, "20");
  }

  private static class HasFGetField implements Callable<String> {
    private float f;

    public HasFGetField(float f) {
      this.f = f;
    }

    @Override
    public String call() {
      return "" + this.f;
    }
  }

  @Test
  public void shouldReplaceFloatField() throws Exception {
    final Mutant mutant = getFirstMutant(HasFGetField.class);
    assertMutantCallableReturns(new HasFGetField(20), mutant, "-20.0");
    assertMutantCallableReturns(new HasFGetField(-20), mutant, "20.0");
  }

  private static class HasJGetField implements Callable<String> {
    private long l;

    public HasJGetField(long l) {
      this.l = l;
    }

    @Override
    public String call() {
      return "" + this.l;
    }
  }

  @Test
  public void shouldReplaceLongField() throws Exception {
    final Mutant mutant = getFirstMutant(HasJGetField.class);
    assertMutantCallableReturns(new HasJGetField(20), mutant, "-20");
    assertMutantCallableReturns(new HasJGetField(-20), mutant, "20");
  }

  private static class HasDGetField implements Callable<String> {
    private double d;

    public HasDGetField(double d) {
      this.d = d;
    }

    @Override
    public String call() {
      return "" + this.d;
    }
  }

  @Test
  public void shouldReplaceDoubleField() throws Exception {
    final Mutant mutant = getFirstMutant(HasDGetField.class);
    assertMutantCallableReturns(new HasDGetField(20), mutant, "-20.0");
    assertMutantCallableReturns(new HasDGetField(-20), mutant, "20.0");
  }

  private static class HasBGetField implements Callable<String> {
    private byte b;

    public HasBGetField(byte b) {
      this.b = b;
    }

    @Override
    public String call() {
      return "" + this.b;
    }
  }

  @Test
  public void shouldReplaceByteField() throws Exception {
    final Mutant mutant = getFirstMutant(HasBGetField.class);
    assertMutantCallableReturns(new HasBGetField((byte)20), mutant, "-20");
    assertMutantCallableReturns(new HasBGetField((byte)-20), mutant, "20");
  }

  private static class HasSGetField implements Callable<String> {
    private short s;

    public HasSGetField(short s) {
      this.s = s;
    }

    @Override
    public String call() {
      return "" + this.s;
    }
  }

  @Test
  public void shouldReplaceShortField() throws Exception {
    final Mutant mutant = getFirstMutant(HasSGetField.class);
    assertMutantCallableReturns(new HasSGetField((short)20), mutant, "-20");
    assertMutantCallableReturns(new HasSGetField((short)-20), mutant, "20");
  }

  private static class HasIGetStaticField implements Callable<String> {
    private static int value;
    private int i;

    public HasIGetStaticField(int i) {
      this.i = i;
    }

    @Override
    public String call() {
      value = this.i;
      return "" + value;
    }
  }

  @Test
  public void shouldReplaceIntegerStaticField() throws Exception {
    final Mutant mutant = getNthMutant(HasIGetStaticField.class, 1);
    assertMutantCallableReturns(new HasIGetStaticField(20), mutant, "-20");
    assertMutantCallableReturns(new HasIGetStaticField(-20), mutant, "20");
  }

  private static class HasFGetStaticField implements Callable<String> {
    private static float value;
    private float f;

    public HasFGetStaticField(float f) {
      this.f = f;
    }

    @Override
    public String call() {
      value = this.f;
      return "" + value;
    }
  }

  @Test
  public void shouldReplaceFloatStaticField() throws Exception {
    final Mutant mutant = getNthMutant(HasFGetStaticField.class, 1);
    assertMutantCallableReturns(new HasFGetStaticField(20), mutant, "-20.0");
    assertMutantCallableReturns(new HasFGetStaticField(-20), mutant, "20.0");
  }

  private static class HasJGetStaticField implements Callable<String> {
    private static long value;
    private long l;

    public HasJGetStaticField(long l) {
      this.l = l;
    }

    @Override
    public String call() {
      value = this.l;
      return "" + value;
    }
  }

  @Test
  public void shouldReplaceLongStaticField() throws Exception {
    final Mutant mutant = getNthMutant(HasJGetStaticField.class, 1);
    assertMutantCallableReturns(new HasJGetStaticField(20), mutant, "-20");
    assertMutantCallableReturns(new HasJGetStaticField(-20), mutant, "20");
  }

  private static class HasDGetStaticField implements Callable<String> {
    private static double value;
    private double d;

    public HasDGetStaticField(double d) {
      this.d = d;
    }

    @Override
    public String call() {
      value = this.d;
      return "" + value;
    }
  }

  @Test
  public void shouldReplaceDoubleStaticField() throws Exception {
    final Mutant mutant = getNthMutant(HasDGetStaticField.class, 1);
    assertMutantCallableReturns(new HasDGetStaticField(20), mutant, "-20.0");
    assertMutantCallableReturns(new HasDGetStaticField(-20), mutant, "20.0");
  }

  private static class HasBGetStaticField implements Callable<String> {
    private static byte value;
    private byte b;

    public HasBGetStaticField(byte b) {
      this.b = b;
    }

    @Override
    public String call() {
      value = this.b;
      return "" + value;
    }
  }

  @Test
  public void shouldReplaceByteStaticField() throws Exception {
    final Mutant mutant = getNthMutant(HasBGetStaticField.class, 1);
    assertMutantCallableReturns(new HasBGetStaticField((byte)20), mutant, "-20");
    assertMutantCallableReturns(new HasBGetStaticField((byte)-20), mutant, "20");
  }

  private static class HasSGetStaticField implements Callable<String> {
    private static short value;
    private short s;

    public HasSGetStaticField(short s) {
      this.s = s;
    }

    @Override
    public String call() {
      value = this.s;
      return "" + value;
    }
  }

  @Test
  public void shouldReplaceShortStaticField() throws Exception {
    final Mutant mutant = getNthMutant(HasSGetStaticField.class, 1);
    assertMutantCallableReturns(new HasSGetStaticField((short)20), mutant, "-20");
    assertMutantCallableReturns(new HasSGetStaticField((short)-20), mutant, "20");
  }

  private static class HasIaload implements Callable<String> {
    @Override
    public String call() {
      int[] value = {20};
      return "" + value[0];
    }
  }

  @Test
  public void shouldReplaceIntegerArrayVariable() throws Exception {
    final Mutant mutant = getFirstMutant(HasIaload.class);
    assertMutantCallableReturns(new HasIaload(), mutant, "-20");
  }

  private static class HasFaload implements Callable<String> {
    @Override
    public String call() {
      float[] value = {20};
      return "" + value[0];
    }
  }

  @Test
  public void shouldReplaceFloatArrayVariable() throws Exception {
    final Mutant mutant = getFirstMutant(HasFaload.class);
    assertMutantCallableReturns(new HasFaload(), mutant, "-20.0");
  }

  private static class HasLaload implements Callable<String> {
    @Override
    public String call() {
      long[] value = {20};
      return "" + value[0];
    }
  }

  @Test
  public void shouldReplaceLongArrayVariable() throws Exception {
    final Mutant mutant = getFirstMutant(HasLaload.class);
    assertMutantCallableReturns(new HasLaload(), mutant, "-20");
  }

  private static class HasDaload implements Callable<String> {

    @Override
    public String call() {
      double[] value = {20};
      return "" + value[0];
    }
  }

  @Test
  public void shouldReplaceDoubleArrayVariable() throws Exception {
    final Mutant mutant = getFirstMutant(HasDaload.class);
    assertMutantCallableReturns(new HasDaload(), mutant, "-20.0");
  }

  private static class HasBaload implements Callable<String> {

    @Override
    public String call() {
      byte[] value = {20};
      return "" + value[0];
    }
  }

  @Test
  public void shouldReplaceByteArrayVariable() throws Exception {
    final Mutant mutant = getFirstMutant(HasBaload.class);
    assertMutantCallableReturns(new HasBaload(), mutant, "-20");
  }

  private static class HasSGetSaload implements Callable<String> {
    @Override
    public String call() {
      short[] value = {20};
      return "" + value[0];
    }
  }

  @Test
  public void shouldReplaceShortArrayVariable() throws Exception {
    final Mutant mutant = getFirstMutant(HasSGetSaload.class);
    assertMutantCallableReturns(new HasSGetSaload(), mutant, "-20");
  }

}
