package org.pitest.mutationtest.engine.gregor.mutators;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
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
  public void replacesObjectReturnValuesWithNull() throws Exception {
    assertMutantCallableReturns(new ObjectReturn(),
        createFirstMutant(ObjectReturn.class), null);
  }
  
  @Test
  public void describesMutationsToObject() {
    assertMutantDescriptionIncludes("replaced return value with null", ObjectReturn.class);
    assertMutantDescriptionIncludes("ObjectReturn::call", ObjectReturn.class);
  }

  @Test
  public void doesNotMutateMethodsAnnotatedWithNotNull() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(
        AnnotatedObjectReturn.class);
    assertThat(actual).isEmpty();
  }
  
  @Test
  public void mutatesMethodsWithOtherAnnoations() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(
        HasOtherAnnotation.class);
    assertThat(actual).hasSize(1);
  }

  
  @Test
  public void doesNotMutateMethodsAnnotatedWithNotNullAndOthers() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(
        MultipleAnnotatedObjectReturn.class);
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

  private static class AnnotatedObjectReturn implements Callable<Object> {
    @Override
    @NotNull
    public Object call() throws Exception {
      return "";
    }
  }
  
  private static class HasOtherAnnotation implements Callable<Object> {
    @Override
    @SomethingElse
    public String call() throws Exception {
      return "";
    }
  }
  
  private static class MultipleAnnotatedObjectReturn implements Callable<Object> {
    @Override
    @NotNull
    @SomethingElse
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
  
//  private static class AnOptional implements Callable<Optional<String>> {
//    @Override
//    public Optional<String> call() throws Exception {
//      return Optional.of("hello");
//    }
//  }
  
    
}

@interface SomethingElse {
  
}

@interface NotNull {

}