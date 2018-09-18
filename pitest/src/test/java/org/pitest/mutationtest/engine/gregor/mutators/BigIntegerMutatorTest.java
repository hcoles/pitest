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

import java.math.BigInteger;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import org.junit.Before;
import org.junit.Test;
import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.gregor.MutatorTestBase;
import org.pitest.mutationtest.engine.gregor.mutators.experimental.BigIntegerMutator;

public class BigIntegerMutatorTest extends MutatorTestBase {

  @Before
  public void setupEngineToMutateOnlyReturnVals() {
    createTesteeWith(BigIntegerMutator.INSTANCE);
  }

  @Test
  public void add() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(Add.class);
    final Mutant mutant = getFirstMutant(actual);
    assertMutantCallableReturns(new Add(0, 2), mutant, "-2");
  }

  @Test
  public void subtract() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(Subtract.class);
    final Mutant mutant = getFirstMutant(actual);
    assertMutantCallableReturns(new Subtract(0, 3), mutant, "3");
  }

  @Test
  public void mutliply() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(Multiply.class);
    final Mutant mutant = getFirstMutant(actual);
    assertMutantCallableReturns(new Multiply(2, 2), mutant, "1");
  }

  @Test
  public void divide() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(Divide.class);
    final Mutant mutant = getFirstMutant(actual);
    assertMutantCallableReturns(new Divide(2, 2), mutant, "4");
  }

  @Test
  public void modulo() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(Modulo.class);
    final Mutant mutant = getFirstMutant(actual);
    assertMutantCallableReturns(new Modulo(25, 6), mutant, "4");
  }

  @Test
  public void moduloLambda() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(ModuloLambda.class);
    final Mutant mutant = getFirstMutant(actual);
    assertMutantCallableReturns(new ModuloLambda(25, 6), mutant, "4");
  }

  private static abstract class AbstractMath implements Callable<String> {

    private final BigInteger value1;
    private final BigInteger value2;

    public AbstractMath(long v1, long v2) {
      this.value1 = BigInteger.valueOf(v1);
      this.value2 = BigInteger.valueOf(v2);
    }

    abstract BigInteger apply(BigInteger left, BigInteger right);

    @Override
    public String call() throws Exception {
      return String.valueOf(apply(value1, value2));
    }
  }

  private static class Add extends AbstractMath {

    public Add(long v1, long v2) {
      super(v1, v2);
    }

    @Override
    BigInteger apply(BigInteger left, BigInteger right) {
      return left.add(right);
    }
  }

  private static class Subtract extends AbstractMath {

    public Subtract(long v1, long v2) {
      super(v1, v2);
    }

    @Override
    BigInteger apply(BigInteger left, BigInteger right) {
      return left.subtract(right);
    }
  }

  private static class Divide extends AbstractMath {

    public Divide(long v1, long v2) {
      super(v1, v2);
    }

    @Override
    BigInteger apply(BigInteger left, BigInteger right) {
      return left.divide(right);
    }
  }

  private static class Multiply extends AbstractMath {

    public Multiply(long v1, long v2) {
      super(v1, v2);
    }

    @Override
    BigInteger apply(BigInteger left, BigInteger right) {
      return left.multiply(right);
    }
  }

  private static class Modulo extends AbstractMath {

    public Modulo(long v1, long v2) {
      super(v1, v2);
    }

    @Override
    BigInteger apply(BigInteger left, BigInteger right) {
      return left.mod(right);
    }
  }

  private static class ModuloLambda extends AbstractMath {

    private BiFunction<BigInteger, BigInteger, BigInteger> function;

    public ModuloLambda(long v1, long v2) {
      super(v1, v2);
      this.function = BigInteger::mod;
    }

    @Override
    BigInteger apply(BigInteger left, BigInteger right) {
      return function.apply(left, right);
    }
  }
}
