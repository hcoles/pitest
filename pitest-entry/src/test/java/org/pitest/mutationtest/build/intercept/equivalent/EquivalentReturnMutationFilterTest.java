package org.pitest.mutationtest.build.intercept.equivalent;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.mutationtest.build.intercept.javafeatures.FilterTester;
import org.pitest.mutationtest.engine.gregor.mutators.PrimitiveReturnsMutator;

public class EquivalentReturnMutationFilterTest {
  
  TrivialEquivalanceFilter testee = new TrivialEquivalanceFilter();
  
  FilterTester verifier = new FilterTester("", testee, PrimitiveReturnsMutator.PRIMITIVE_RETURN_VALS_MUTATOR);    
  
  @Test
  public void shouldDeclareTypeAsFilter() {
    assertThat(this.testee.type()).isEqualTo(InterceptorType.FILTER);
  }
  
  @Test
  public void doesNotFilterNonEquivalents() {
    verifier.assertFiltersNMutationFromClass(0, ReturnsTrue.class);
  }
  

  @Test
  public void filtersEquivalentPrimitiveIntMutants() {
    verifier.assertFiltersNMutationFromClass(1, AlreadyReturnsConstZero.class);
  }
  
  @Test
  public void filtersEquivalentPrimitiveBooleanMutants() {
    verifier.assertFiltersNMutationFromClass(1, AlreadyReturnsFalse.class);
  }

  @Test
  public void filtersEquivalentPrimitiveLongMutants() {
    verifier.assertFiltersNMutationFromClass(1, AlreadyReturnsLong0.class);
  }
  
  @Test
  public void filtersEquivalentPrimitiveFloatMutants() {
    verifier.assertFiltersNMutationFromClass(1, AlreadyReturnsFloat0.class);
  }
  
  @Test
  public void filtersEquivalentPrimitiveDoubleMutants() {
    verifier.assertFiltersNMutationFromClass(1, AlreadyReturnsDouble0.class);
  }
}

class ReturnsTrue {
  public boolean a() {
    return true;
  }
}

class AlreadyReturnsConstZero {
  public int a() {
    return 0;
  }
}

class AlreadyReturnsFalse {
  public boolean a() {
    return false;
  }
}

class AlreadyReturnsLong0 {
  public long a() {
    return 0;
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