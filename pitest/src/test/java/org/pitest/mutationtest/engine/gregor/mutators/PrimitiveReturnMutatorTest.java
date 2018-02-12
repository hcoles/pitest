package org.pitest.mutationtest.engine.gregor.mutators;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.gregor.MutatorTestBase;

public class PrimitiveReturnMutatorTest extends MutatorTestBase {

  @Before
  public void setupEngineToMutateOnlyReturnVals() {
    createTesteeWith(PrimitiveReturnsMutator.PRIMITIVE_RETURN_VALS_MUTATOR);
  }

  @Test
  public void doesNotMutateBooleans() throws Exception {
    assertNoMutants(BooleanReturn.class);
  }

  @Test
  public void mutatesReturnTo0ForBytes() throws Exception {
    assertMutantCallableReturns(new ByteReturn(),
        createFirstMutant(ByteReturn.class), "0");
  }

  @Test
  public void describesMutationsToBytes() {
    assertMutantDescriptionIncludes("replaced byte return with 0", ByteReturn.class);
    assertMutantDescriptionIncludes("ByteReturn::mutable", ByteReturn.class);
  }

  @Test
  public void mutatesReturnToReturn0ForInts() throws Exception {
    assertMutantCallableReturns(new IntReturn(),
        createFirstMutant(IntReturn.class), "0");
  }

  @Test
  public void describesMutationsToInts() {
    assertMutantDescriptionIncludes("replaced int return with 0", IntReturn.class);
    assertMutantDescriptionIncludes("IntReturn::mutable", IntReturn.class);
  }

  @Test
  public void mutatesReturnToReturn0ForShorts() throws Exception {
    assertMutantCallableReturns(new ShortReturn(),
        createFirstMutant(ShortReturn.class), "0");
  }

  @Test
  public void describesMutationsToShorts() {
    assertMutantDescriptionIncludes("replaced short return with 0", ShortReturn.class);
    assertMutantDescriptionIncludes("ShortReturn::mutable", ShortReturn.class);
  }

  @Test
  public void mutatesReturnToReturn0ForChars() throws Exception {
    assertMutantCallableReturns(new CharReturn(),
        createFirstMutant(CharReturn.class), "" + (char)0);
  }

  @Test
  public void describesMutationsToChars() {
    assertMutantDescriptionIncludes("replaced char return with 0", CharReturn.class);
    assertMutantDescriptionIncludes("CharReturn::mutable", CharReturn.class);
  }

  @Test
  public void mutatesReturnToReturn0ForLongs() throws Exception {
    assertMutantCallableReturns(new LongReturn(),
        createFirstMutant(LongReturn.class), "0");
  }

  @Test
  public void describesMutationsToLongs() {
    assertMutantDescriptionIncludes("replaced long return with 0", LongReturn.class);
    assertMutantDescriptionIncludes("LongReturn::mutable", LongReturn.class);
  }

  @Test
  public void mutatesReturnToReturn0ForFloats() throws Exception {
    assertMutantCallableReturns(new FloatReturn(),
        createFirstMutant(FloatReturn.class), "0.0");
  }

  @Test
  public void describesMutationsToFloats() {
    assertMutantDescriptionIncludes("replaced float return with 0.0f", FloatReturn.class);
    assertMutantDescriptionIncludes("FloatReturn::mutable", FloatReturn.class);
  }

  @Test
  public void mutatesReturnToReturn0ForDoubles() throws Exception {
    assertMutantCallableReturns(new DoubleReturn(),
        createFirstMutant(DoubleReturn.class), "0.0");
  }

  @Test
  public void describesMutationsToDoubless() {
    assertMutantDescriptionIncludes("replaced double return with 0.0d", DoubleReturn.class);
    assertMutantDescriptionIncludes("DoubleReturn::mutable", DoubleReturn.class);
  }

  @Test
  public void doesNotMutateObjectReturns() {
    final Collection<MutationDetails> actual = findMutationsFor(BoxedIntReturn.class);
    assertThat(actual).isEmpty();
  }

  private static class BooleanReturn implements Callable<String> {
    public boolean mutable() {
      return true;
    }

    @Override
    public String call() throws Exception {
      return "" + mutable();
    }
  }

  private static class ByteReturn implements Callable<String> {
    public byte mutable() {
      return 101;
    }

    @Override
    public String call() throws Exception {
      return "" + mutable();
    }
  }

  private static class IntReturn implements Callable<String> {
    public int mutable() {
      return 101;
    }

    @Override
    public String call() throws Exception {
      return "" + mutable();
    }
  }


  private static class ShortReturn implements Callable<String> {
    public short mutable() {
      return 1;
    }

    @Override
    public String call() throws Exception {
      return "" + mutable();
    }
  }

  private static class CharReturn implements Callable<String> {
    public char mutable() {
      return 42;
    }

    @Override
    public String call() throws Exception {
      return "" + mutable();
    }
  }

  private static class LongReturn implements Callable<String> {
    public long mutable() {
      return 10;
    }

    @Override
    public String call() throws Exception {
      return "" + mutable();
    }
  }


  private static class FloatReturn implements Callable<String> {
    public float mutable() {
      return Float.MAX_VALUE;
    }

    @Override
    public String call() throws Exception {
      return "" + mutable();
    }
  }

  private static class DoubleReturn implements Callable<String> {
    public double mutable() {
      return Double.MAX_VALUE;
    }

    @Override
    public String call() throws Exception {
      return "" + mutable();
    }
  }

  private static class BoxedIntReturn implements Callable<String> {
    public Integer mutable() {
      return null;
    }

    @Override
    public String call() throws Exception {
      return "" + mutable();
    }
  }

}
