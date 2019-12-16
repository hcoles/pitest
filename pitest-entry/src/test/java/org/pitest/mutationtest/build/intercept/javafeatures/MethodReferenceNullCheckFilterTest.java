package org.pitest.mutationtest.build.intercept.javafeatures;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;

import org.junit.Test;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.mutationtest.engine.gregor.config.Mutator;

public class MethodReferenceNullCheckFilterTest {

  private static final String             PATH      = "requirenotnull/{0}_{1}";

  MethodReferenceNullCheckFilter testee = new MethodReferenceNullCheckFilter();
  
  FilterTester verifier = new FilterTester(PATH, this.testee, Mutator.all());

  @Test
  public void shouldDeclareTypeAsFilter() {
    assertThat(this.testee.type()).isEqualTo(InterceptorType.FILTER);
  }

  @Test
  public void filtersRequireNotNullChecksForMethodReferences() {
    this.verifier.assertFiltersNMutationFromSample(2, "MethodReferenceNullChecks");
  }

  @Test
  public void shouldNotFilterDeadCallsToGetClassInNonLambdaMethods() {
    this.verifier.assertFiltersNMutationFromClass(0, HasNormalRequireNonNullCheck.class);
  }

}


class HasNormalRequireNonNullCheck {
  String aField;
  public void amethod() {
    Objects.requireNonNull(aField);
    System.out.println(aField);
  }
}

