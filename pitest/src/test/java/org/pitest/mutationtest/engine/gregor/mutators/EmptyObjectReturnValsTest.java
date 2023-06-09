package org.pitest.mutationtest.engine.gregor.mutators;

import org.junit.Test;
import org.pitest.verifier.mutants.MutatorVerifierStart;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.pitest.mutationtest.engine.gregor.mutators.returns.EmptyObjectReturnValsMutator.EMPTY_RETURNS;

public class EmptyObjectReturnValsTest {

  MutatorVerifierStart v = MutatorVerifierStart.forMutator(EMPTY_RETURNS);

  @Test
  public void doesNotMutateObjectReturnValuesWithNoEmptyValueOption() {
    v.forClass(ObjectReturn.class)
            .noMutantsCreated();
  }

  @Test
  public void mutatesBoxedIntegersToZero() {
    v.forCallableClass(BoxedInteger.class)
                    .firstMutantShouldReturn(0);
  }

  @Test
  public void describesMutationsToIntegers() {
    v.forClass(BoxedInteger.class)
            .firstMutantDescription()
            .contains("replaced Integer return value with 0")
            .contains("BoxedInteger::call");
  }

  @Test
  public void doesNotMutateBoolean() {
    v.forClass(BoxedBoolean.class)
            .noMutantsCreated();
  }

  @Test
  public void mutatesBoxedShortsToZero() {
    v.forCallableClass(BoxedShort.class)
                    .firstMutantShouldReturn((short)0);
  }

  @Test
  public void describesMutationsToShorts() {
    v.forClass(BoxedShort.class)
            .firstMutantDescription()
            .contains("replaced Short return value with 0")
            .contains("BoxedShort::call");
  }

  @Test
  public void mutatesBoxedCharsToZero() {
    v.forCallableClass(BoxedChar.class)
            .firstMutantShouldReturn((char)0);
  }

  @Test
  public void describesMutationsToChars() {
    v.forClass(BoxedChar.class)
            .firstMutantDescription()
            .contains("replaced Character return value with 0")
            .contains("BoxedChar::call");
  }

  @Test
  public void mutatesBoxedLongsToZero() {
    v.forCallableClass(BoxedLong.class)
            .firstMutantShouldReturn(0L);
  }

  @Test
  public void describesMutationsToLongs() {
    v.forClass(BoxedLong.class)
            .firstMutantDescription()
            .contains("replaced Long return value with 0")
            .contains("BoxedLong::call");
  }

  @Test
  public void mutatesBoxedFloatsToZero()  {
    v.forCallableClass(BoxedFloat.class)
            .firstMutantShouldReturn(0f);
  }

  @Test
  public void describesMutationsToFloats() {
    v.forClass(BoxedFloat.class)
            .firstMutantDescription()
            .contains("replaced Float return value with 0")
            .contains("BoxedFloat::call");
  }

  @Test
  public void mutatesBoxedDoublesToZero() {
    v.forCallableClass(BoxedDouble.class)
            .firstMutantShouldReturn(0d);
  }

  @Test
  public void describesMutationsToDoubles() {
    v.forClass(BoxedDouble.class)
            .firstMutantDescription()
            .contains("replaced Double return value with 0")
            .contains("BoxedDouble::call");
  }

  @Test
  public void mutatesBoxedIntegersToZeroWhenAnnotatedNotNull() {
    v.forCallableClass(BoxedIntegerWithNoNullAnnotation.class)
            .firstMutantShouldReturn(0);
  }

  @Test
  public void mutatesToEmptyString() {
    v.forCallableClass(AString.class)
            .firstMutantShouldReturn("");
  }

  @Test
  public void describesMutationsToString() {
    v.forClass(AString.class)
            .firstMutantDescription()
            .contains("replaced return value with \"\"")
            .contains("AString::call");
  }

  @Test
  public void mutatesListToEmptyList() {
    v.forCallableClass(AList.class)
            .firstMutantShouldReturn(Collections.emptyList());
  }

  @Test
  public void mutatesMapToEmptyMap() {
    v.forCallableClass(AMap.class)
            .firstMutantShouldReturn(Collections.emptyMap());
  }

  @Test
  public void mutatesSetToEmptySet() {
    v.forCallableClass(ASet.class)
            .firstMutantShouldReturn(Collections.emptySet());
  }

  @Test
  public void mutatesCollectionsToEmptyList() {
    v.forCallableClass(ACollection.class)
            .firstMutantShouldReturn(Collections.emptyList());
  }

  @Test
  public void mutatesToOptionalEmpty() {
    v.forCallableClass(AnOptional.class)
            .firstMutantShouldReturn(Optional.empty());
  }

  @Test
  public void mutatesToEmptyStream() {
    Stream actual = v.forCallableClass(AStream.class)
            .firstMutantReturnValue();

    assertThat(actual).isEmpty();
  }

  @Test
  public void mutatesIterableToEmptyList() {
    Iterable actual = v.forCallableClass(AnIterable.class)
            .firstMutantReturnValue();

    assertThat(actual).isEmpty();
  }

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
      return 1L;
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

  private static class AMap implements Callable<Map<String, String>> {
    @Override
    public Map<String, String> call() throws Exception {
      Map<String,String> m = new HashMap<>();
      m.put("a", "b");
      return m;
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

  private static class AnOptional implements Callable<Optional<String>> {
    @Override
    public Optional<String> call() throws Exception {
      return Optional.of("hello");
    }
  }

  private static class AStream implements Callable<Stream<String>> {
    @Override
    public Stream<String> call() throws Exception {
      return Stream.of("hello");
    }
  }

  private static class AnIterable implements Callable<Iterable<String>> {
    @Override
    public Iterable<String> call() throws Exception {
      return asList("hello");
    }
  }

}