package org.pitest.mutationtest.engine.gregor.mutators;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.gregor.MutatorTestBase;

public class EmptyObjectReturnValsTest extends MutatorTestBase {

  @Before
  public void setupEngineToMutateOnlyReturnVals() {
    createTesteeWith(EmptyObjectReturnValsMutator.EMPTY_RETURN_VALUES);
  }

  @Test
  public void doesNotMutateObjectReturnValuesWithNoEmptyValueOption() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(
        ObjectReturn.class);
    assertThat(actual).isEmpty();
  }

  @Test
  public void mutatesBoxedIntegersToZero() throws Exception {
    assertMutantCallableReturns(new BoxedInteger(),
        createFirstMutant(BoxedInteger.class), 0);
  }

  @Test
  public void describesMutationsToIntegers() {
    assertMutantDescriptionIncludes("replaced Integer return value with 0", BoxedInteger.class);
    assertMutantDescriptionIncludes("BoxedInteger::call", BoxedInteger.class);
  }

  @Test
  public void doesNotMutateBoolean() throws Exception {
    assertNoMutants(BoxedBoolean.class);
  }

  @Test
  public void mutatesBoxedShortsToZero() throws Exception {
    assertMutantCallableReturns(new BoxedShort(),
        createFirstMutant(BoxedShort.class), (short)0);
  }

  @Test
  public void describesMutationsToShorts() {
    assertMutantDescriptionIncludes("replaced Short return value with 0", BoxedShort.class);
    assertMutantDescriptionIncludes("BoxedShort::call", BoxedShort.class);
  }

  @Test
  public void mutatesBoxedCharsToZero() throws Exception {
    assertMutantCallableReturns(new BoxedChar(),
        createFirstMutant(BoxedChar.class), (char)0);
  }

  @Test
  public void describesMutationsToChars() {
    assertMutantDescriptionIncludes("replaced Character return value with 0", BoxedChar.class);
    assertMutantDescriptionIncludes("BoxedChar::call", BoxedChar.class);
  }

  @Test
  public void mutatesBoxedLongsToZero() throws Exception {
    assertMutantCallableReturns(new BoxedLong(),
        createFirstMutant(BoxedLong.class), 0l);
  }

  @Test
  public void describesMutationsToLongs() {
    assertMutantDescriptionIncludes("replaced Long return value with 0", BoxedLong.class);
    assertMutantDescriptionIncludes("BoxedLong::call", BoxedLong.class);
  }

  @Test
  public void mutatesBoxedFloatsToZero() throws Exception {
    assertMutantCallableReturns(new BoxedFloat(),
        createFirstMutant(BoxedFloat.class), 0f);
  }

  @Test
  public void describesMutationsToFloats() {
    assertMutantDescriptionIncludes("replaced Float return value with 0", BoxedFloat.class);
    assertMutantDescriptionIncludes("BoxedFloat::call", BoxedFloat.class);
  }

  @Test
  public void mutatesBoxedDoublesToZero() throws Exception {
    assertMutantCallableReturns(new BoxedDouble(),
        createFirstMutant(BoxedDouble.class), 0d);
  }

  @Test
  public void describesMutationsToDoubles() {
    assertMutantDescriptionIncludes("replaced Double return value with 0", BoxedDouble.class);
    assertMutantDescriptionIncludes("BoxedDouble::call", BoxedDouble.class);
  }

  @Test
  public void mutatesBoxedIntegersToZeroWhenAnnotatedNotNull() throws Exception {
    assertMutantCallableReturns(new BoxedIntegerWithNoNullAnnotation(),
        createFirstMutant(BoxedIntegerWithNoNullAnnotation.class), 0);
  }

  @Test
  public void mutatesToEmptyString() throws Exception {
    assertMutantCallableReturns(new AString(),
        createFirstMutant(AString.class), "");
  }

  @Test
  public void describesMutationsToString() {
    assertMutantDescriptionIncludes("replaced return value with \"\"", AString.class);
    assertMutantDescriptionIncludes("AString::call", AString.class);
  }

  @Test
  public void mutatesListToEmptyList() throws Exception {
    assertMutantCallableReturns(new AList(),
        createFirstMutant(AList.class), Collections.<String>emptyList());
  }

  @Test
  public void mutatesSetToEmptySet() throws Exception {
    assertMutantCallableReturns(new ASet(),
        createFirstMutant(ASet.class), Collections.<String>emptySet());
  }

  @Test
  public void mutatesCollectionsToEmptyList() throws Exception {
    assertMutantCallableReturns(new ACollection(),
        createFirstMutant(ACollection.class), Collections.<String>emptyList());
  }

// must build on java 7
//  @Test
//  public void mutatesToOptionalEmpty() throws Exception {
//    assertMutantCallableReturns(new AnOptional(),
//        createFirstMutant(AnOptional.class), Optional.<String>empty());
//  }

  private static class ObjectReturn implements Callable<Object> {
    @Override
    public Object call() throws Exception {
      return "";
    }
  }

  private static class BoxedInteger implements Callable<Integer> {
    @Override
    public Integer call() throws Exception {
      return 1;
    }
  }

  private static class BoxedIntegerWithNoNullAnnotation implements Callable<Integer> {
    @Override
    @NotNull
    public Integer call() throws Exception {
      return 1;
    }
  }

  private static class BoxedBoolean implements Callable<Boolean> {
    @Override
    public Boolean call() throws Exception {
      return true;
    }
  }

  private static class BoxedShort implements Callable<Short> {
    @Override
    public Short call() throws Exception {
      return 1;
    }
  }

  private static class BoxedChar implements Callable<Character> {
    @Override
    public Character call() throws Exception {
      return 1;
    }
  }

  private static class BoxedLong implements Callable<Long> {
    @Override
    public Long call() throws Exception {
      return 1l;
    }
  }

  private static class BoxedFloat implements Callable<Float> {
    @Override
    public Float call() throws Exception {
      return 1f;
    }
  }

  private static class BoxedDouble implements Callable<Double> {
    @Override
    public Double call() throws Exception {
      return 1d;
    }
  }

  private static class AString implements Callable<String> {
    @Override
    public String call() throws Exception {
      return "hello";
    }
  }

  private static class AList implements Callable<List<String>> {
    @Override
    public List<String> call() throws Exception {
      return Collections.singletonList("");
    }
  }

  private static class ASet implements Callable<Set<String>> {
    @Override
    public Set<String> call() throws Exception {
      return Collections.singleton("");
    }
  }

  private static class ACollection implements Callable<Collection<String>> {
    @Override
    public Collection<String> call() throws Exception {
      return Collections.singleton("");
    }
  }

//  private static class AnOptional implements Callable<Optional<String>> {
//    @Override
//    public Optional<String> call() throws Exception {
//      return Optional.of("hello");
//    }
//  }


}