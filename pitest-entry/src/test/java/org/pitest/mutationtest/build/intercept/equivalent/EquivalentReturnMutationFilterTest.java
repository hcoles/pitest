package org.pitest.mutationtest.build.intercept.equivalent;

import com.example.emptyreturns.AlreadyReturnsEmptyOptionalInTryWithResourcesBlock;
import org.junit.Test;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.build.intercept.javafeatures.FilterTester;
import org.pitest.mutationtest.engine.gregor.mutators.returns.EmptyObjectReturnValsMutator;
import org.pitest.mutationtest.engine.gregor.mutators.returns.NullReturnValsMutator;
import org.pitest.mutationtest.engine.gregor.mutators.returns.PrimitiveReturnsMutator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.pitest.mutationtest.engine.gregor.mutators.returns.BooleanFalseReturnValsMutator.FALSE_RETURNS;
import static org.pitest.mutationtest.engine.gregor.mutators.returns.BooleanTrueReturnValsMutator.TRUE_RETURNS;

public class EquivalentReturnMutationFilterTest {

  MutationInterceptor testee = new EquivalentReturnMutationFilter().createInterceptor(null);

  FilterTester verifier = new FilterTester("emptyReturns/{0}_{1}", this.testee, PrimitiveReturnsMutator.PRIMITIVE_RETURNS
                                                     , EmptyObjectReturnValsMutator.EMPTY_RETURNS
                                                     , NullReturnValsMutator.NULL_RETURNS
                                                     , FALSE_RETURNS
                                                     , TRUE_RETURNS);

  @Test
  public void shouldDeclareTypeAsFilter() {
    assertThat(this.testee.type()).isEqualTo(InterceptorType.FILTER);
  }

  @Test
  public void doesNotFilterNonEquivalents() {
    this.verifier.assertFiltersNMutationFromClass(0, ReturnsWidget.class);
  }

  @Test
  public void doesNotFilterNonEquivalentsWhenEquivalentMutantAlsoPresent() {
    this.verifier.assertFiltersNMutationFromClass(1, ReturnsNullAndWidget.class);
  }

  @Test
  public void filtersIndirectEquivalentNullReturns() {
    this.verifier.assertFiltersNMutationFromClass(1, ReturnsNullFromVariable.class);
  }

  @Test
  public void filtersNullEquivalentMutants() {
    this.verifier.assertFiltersNMutationFromClass(1, ReturnsNull.class);
  }

  @Test
  public void filtersEquivalentPrimitiveIntMutants() {
    this.verifier.assertFiltersNMutationFromClass(1, AlreadyReturnsConstZero.class);
  }

  @Test
  public void filtersEquivalentPrimitiveIntMutantsInTryCatch() {
    this.verifier.assertFiltersNMutationFromClass(1, AlreadyReturnsConstZeroInTryCatch.class);
  }
  
  @Test
  public void filtersEquivalentPrimitiveBooleanMutants() {
    this.verifier.assertFiltersMutationsFromMutator(FALSE_RETURNS.getGloballyUniqueId()
        , AlreadyReturnsFalse.class);
  }

  @Test
  public void filtersEquivalentPrimitiveBooleanTrueMutants() {
    this.verifier.assertFiltersMutationsFromMutator(TRUE_RETURNS.getGloballyUniqueId()
        , ReturnsTrue.class);
  }
  
  @Test
  public void filtersEquivalentPrimitiveBooleanTrueMutantsInTryCatch() {
    this.verifier.assertFiltersMutationsFromMutator(TRUE_RETURNS.getGloballyUniqueId()
        , ReturnsTrueInTryCatch.class);
  }

  @Test
  public void filtersEquivalentBoxedTrueMutantsInTryCatch() {
    this.verifier.assertFiltersMutationsFromMutator(TRUE_RETURNS.getGloballyUniqueId()
            , ReturnsBoxedTrueInTryCatch.class);
  }

  @Test
  public void filtersEquivalentBoxedFalseMutantsInTryCatch() {
    this.verifier.assertFiltersMutationsFromMutator(FALSE_RETURNS.getGloballyUniqueId()
            , ReturnsBoxedFalseInTryCatch.class);
  }

  @Test
  public void filtersEquivalentPrimitiveLongMutants() {
    this.verifier.assertFiltersNMutationFromClass(1, AlreadyReturnsLong0.class);
  }

  @Test
  public void filtersEquivalentPrimitiveFloatMutants() {
    this.verifier.assertFiltersNMutationFromClass(1, AlreadyReturnsFloat0.class);
  }

  @Test
  public void filtersEquivalentPrimitiveDoubleMutants() {
    this.verifier.assertFiltersNMutationFromClass(1, AlreadyReturnsDouble0.class);
  }

  @Test
  public void filtersEquivalentBoxedBooleanMutants() {
    this.verifier.assertFiltersMutationsFromMutator(FALSE_RETURNS.getGloballyUniqueId()
            , AlreadyReturnsBoxedFalse.class);
  }

  @Test
  public void filtersEquivalentBoxedBooleanTrueMutants() {
    this.verifier.assertFiltersMutationsFromMutator(TRUE_RETURNS.getGloballyUniqueId()
        , AlreadyReturnsBoxedTrue.class);
  }

  @Test
  public void filtersEquivalentConstantTrueMutants() {
    this.verifier.assertFiltersMutationsFromMutator(TRUE_RETURNS.getGloballyUniqueId()
            , ReturnsConstantTrue.class);
  }

  @Test
  public void filtersEquivalentConstantFalseMutants() {
    this.verifier.assertFiltersMutationsFromMutator(FALSE_RETURNS.getGloballyUniqueId()
            , ReturnsConstantFalse.class);
  }

  @Test
  public void filtersEquivalentBoxedIntegerMutants() {
    this.verifier.assertFiltersNMutationFromClass(1, AlreadyReturnsBoxedZeroInteger.class);
  }

  @Test
  public void filtersEquivalentBoxedShortMutants() {
    this.verifier.assertFiltersNMutationFromClass(1, AlreadyReturnsBoxedZeroShort.class);
  }

  @Test
  public void filtersEquivalentBoxedLongMutants() {
    this.verifier.assertFiltersNMutationFromClass(1, AlreadyReturnsBoxedZeroLong.class);
  }

  @Test
  public void filtersEquivalentBoxedFloatMutants() {
    this.verifier.assertFiltersNMutationFromClass(1, AlreadyReturnsBoxedZeroFloat.class);
  }

  @Test
  public void filtersEquivalentBoxedDoubleMutants() {
    this.verifier.assertFiltersNMutationFromClass(1, AlreadyReturnsBoxedZeroDouble.class);
  }

  @Test
  public void doesNotFilterOtherSingleParamStaticMethodCalls() {
    this.verifier.assertFiltersNMutationFromClass(0, CallsAnIntegerReturningStaticWith0.class);
  }

  @Test
  public void filtersEquivalentStringMutants() {
    this.verifier.assertFiltersNMutationFromClass(1, AlreadyReturnsEmptyString.class);
  }

  @Test
  public void filtersEquivalentStringMutantsWhenEmptyStringHeldAsField() {
    this.verifier.assertFiltersNMutationFromClass(1, AlreadyReturnsEmptyStringFromField.class);
  }

  @Test
  public void filtersEquivalentListMutants() {
    this.verifier.assertFiltersNMutationFromClass(1, AlreadyReturnsEmptyList.class);
  }

  @Test
  public void filtersEquivalentMapMutants() {
    this.verifier.assertFiltersNMutationFromClass(1, AlreadyReturnsEmptyMap.class);
  }

  @Test
  public void filtersEquivalentListMutantsInTryCatch() {
    this.verifier.assertFiltersNMutationFromClass(1, AlreadyReturnsEmptyListInTryCatch.class);
  }

  
  @Test
  public void filtersEquivalentSetMutants() {
    this.verifier.assertFiltersNMutationFromClass(1, AlreadyReturnsEmptySet.class);
  }

  @Test
  public void filtersEquivalentOptionalMutants() {
    verifier.assertFiltersNMutationFromClass(1, AlreadyReturnsEmptyOptional.class);
  }

  @Test
  public void filtersEquivalentOptionalMutantsInTryBlocks() {
    verifier.assertFiltersNMutationFromClass(1, AlreadyReturnsEmptyOptionalInTryBlock.class);
  }

  @Test
  public void filtersEquivalentOptionalMutantsInTryWithResourcesBlocks() {
    verifier.assertFiltersNMutationFromClass(1, AlreadyReturnsEmptyOptionalInTryWithResourcesBlock.class);
  }

  @Test
  public void filtersEquivalentOptionalMutantsInTryWithResourcesBlocksForOtherCompilers() {
    // javac sample is for java 8
    verifier.assertFiltersNMutationFromSample(1, "AlreadyReturnsEmptyOptionalInTryWithResourcesBlock");
  }

  @Test
  public void filtersEquivalentStreamMutants() {
    verifier.assertFiltersNMutationFromClass(1, AlreadyReturnsEmptyStream.class);
  }

  @Test
  public void filtersEquivalentIterableMutants() {
    verifier.assertFiltersNMutationFromClass(1, AlreadyReturnsEmptyIterable.class);
  }
}

class Widget{}

class ReturnsWidget {
  public Widget a() {
    return new Widget();
  }
}

class ReturnsNullAndWidget {
  public Widget a(boolean b) {
    if (b) {
      return null;
    }
    return new Widget();
  }
}

class ReturnsNull {
  public Widget a() {
    return null;
  }
}

class ReturnsNullFromVariable {
  public Widget a(boolean b) {
    Widget w = null;
    if (b) {
      return w;
    } else {
      w = new Widget();
    }
    return w;
  }
}

class ReturnsTrue {
  public boolean a() {
    return true;
  }
}

class ReturnsTrueInTryCatch {
  public boolean a(String s) {
    try {
      Double.valueOf(s);
      return true;
    } catch (NumberFormatException ex) {
      return HideConstant.hide(false);
    }
  }
}

class ReturnsBoxedTrueInTryCatch {
  public Boolean a(String s) {
    try {
      Double.valueOf(s);
      return true;
    } catch (NumberFormatException ex) {
      return HideConstant.hide(false);
    }
  }
}

class ReturnsBoxedFalseInTryCatch {
  public Boolean a(String s) {
    try {
      Double.valueOf(s);
      return false;
    } catch (NumberFormatException ex) {
      return HideConstant.hide(false);
    }
  }
}

class HideConstant {
    public static boolean hide(boolean b) {
        return b;
    }
}

class AlreadyReturnsConstZero {
  public int a() {
    return 0;
  }
}

class AlreadyReturnsConstZeroInTryCatch {
  public int a(String s) {
    try {
      Double.valueOf(s);
      return 0;      
    } catch(NumberFormatException ex) {
      return 42;  
    }
  }
}


class AlreadyReturnsBoxedFalse {
  public Boolean a() {
    return false;
  }
}

class AlreadyReturnsBoxedTrue {
  public Boolean a() {
    return true;
  }
}

class AlreadyReturnsFalse {
  public boolean a() {
    return false;
  }
}

class AlreadyReturnsBoxedZeroInteger {
  public Integer a() {
    return 0;
  }
}

class ReturnsConstantTrue {
  public Boolean a() {
    return Boolean.TRUE;
  }
}

class ReturnsConstantFalse {
  public Boolean a() {
    return Boolean.FALSE;
  }
}


class CallsAnIntegerReturningStaticWith0 {

  static Integer foo(int a) {
    return 42;
  }

  public Integer a() {
    return foo(0);
  }
}

class AlreadyReturnsBoxedZeroShort {
  public Short a() {
    return 0;
  }
}

class AlreadyReturnsLong0 {
  public long a() {
    return 0;
  }
}

class AlreadyReturnsBoxedZeroLong {
  public Long a() {
    return 0L;
  }
}

class AlreadyReturnsFloat0 {
  public float a() {
    return 0;
  }
}

class AlreadyReturnsDouble0 {
  public double a() {
    return 0;
  }
}


class AlreadyReturnsBoxedZeroFloat {
  public Float a() {
    return 0f;
  }
}

class AlreadyReturnsBoxedZeroDouble {
  public Double a() {
    return 0d;
  }
}

class AlreadyReturnsEmptyString {
  public String a() {
    return "";
  }
}

class AlreadyReturnsEmptyStringFromField {
  static final String EMPTY = "";
  public String a() {
    return EMPTY;
  }
}

class AlreadyReturnsEmptyList {
  public List<Integer> a() {
    return Collections.emptyList();
  }
}

class AlreadyReturnsEmptyMap {
  public Map<Integer, Integer> a() {
    return Collections.emptyMap();
  }
}

class AlreadyReturnsEmptyListInTryCatch {
  public List<Integer> a(String s) {
    try {
      Double.valueOf(s);
      return Collections.emptyList();
    } catch (NumberFormatException e) {
      return new ArrayList<>();
    }
  }
}

class AlreadyReturnsEmptySet {
  public Set<Integer> a() {
    return Collections.emptySet();
  }
}

class AlreadyReturnsEmptyOptional {
  public Optional<String> a() {
    return Optional.empty();
  }
}

class AlreadyReturnsEmptyOptionalInTryBlock {
  public Optional<String> a() {
    try {
      Double.parseDouble("12");
      return Optional.empty();
    } catch (Exception ex) {
      return Optional.of("foo");
    }
  }
}

class AlreadyReturnsEmptyStream {
  public Stream<String> a() {
    return Stream.empty();
  }
}

class AlreadyReturnsEmptyIterable {
  public Iterable<String> a() {
    return Collections.emptyList();
  }
}