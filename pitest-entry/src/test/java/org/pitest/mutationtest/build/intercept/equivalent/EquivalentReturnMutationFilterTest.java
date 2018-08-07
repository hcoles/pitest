package org.pitest.mutationtest.build.intercept.equivalent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.pitest.mutationtest.engine.gregor.mutators.BooleanFalseReturnValsMutator.BOOLEAN_FALSE_RETURN;
import static org.pitest.mutationtest.engine.gregor.mutators.BooleanTrueReturnValsMutator.BOOLEAN_TRUE_RETURN;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.Test;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.build.intercept.javafeatures.FilterTester;
import org.pitest.mutationtest.engine.gregor.mutators.EmptyObjectReturnValsMutator;
import org.pitest.mutationtest.engine.gregor.mutators.NullReturnValsMutator;
import org.pitest.mutationtest.engine.gregor.mutators.PrimitiveReturnsMutator;

public class EquivalentReturnMutationFilterTest {

  MutationInterceptor testee = new EquivalentReturnMutationFilter().createInterceptor(null);

  FilterTester verifier = new FilterTester("", this.testee, PrimitiveReturnsMutator.PRIMITIVE_RETURN_VALS_MUTATOR
                                                     , EmptyObjectReturnValsMutator.EMPTY_RETURN_VALUES
                                                     , NullReturnValsMutator.NULL_RETURN_VALUES
                                                     , BOOLEAN_FALSE_RETURN
                                                     , BOOLEAN_TRUE_RETURN);

  @Test
  public void shouldDeclareTypeAsFilter() {
    assertThat(this.testee.type()).isEqualTo(InterceptorType.FILTER);
  }

  @Test
  public void doesNotFilterNonEquivalents() {
    this.verifier.assertFiltersNMutationFromClass(0, ReturnsWidget.class);
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
    this.verifier.assertFiltersMutationsFromMutator(BOOLEAN_FALSE_RETURN.getGloballyUniqueId()
        , AlreadyReturnsFalse.class);
  }

  @Test
  public void filtersEquivalentPrimitiveBooleanTrueMutants() {
    this.verifier.assertFiltersMutationsFromMutator(BOOLEAN_TRUE_RETURN.getGloballyUniqueId()
        , ReturnsTrue.class);
  }
  
  @Test
  public void filtersEquivalentPrimitiveBooleanTrueMutantsInTryCatch() {
    this.verifier.assertFiltersMutationsFromMutator(BOOLEAN_TRUE_RETURN.getGloballyUniqueId()
        , ReturnsTrueInTryCatch.class);
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
    this.verifier.assertFiltersNMutationFromClass(1, AlreadyReturnsBoxedFalse.class);
  }

  @Test
  public void filtersEquivalentBoxedBooleanTrueMutants() {
    this.verifier.assertFiltersMutationsFromMutator(BOOLEAN_TRUE_RETURN.getGloballyUniqueId()
        , AlreadyReturnsBoxedTrue.class);
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
}

class Widget{}

class ReturnsWidget {
  public Widget a() {
    return new Widget();
  }
}

class ReturnsNull {
  public Widget a() {
    return null;
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
    return 0l;
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
