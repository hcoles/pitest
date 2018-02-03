package org.pitest.mutationtest.build.intercept.javafeatures;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.mutationtest.engine.gregor.config.Mutator;

public class ImplicitNullCheckFilterTest {

  private static final String             PATH      = "implicitnullcheck/{0}_{1}";

  ImplicitNullCheckFilter testee = new ImplicitNullCheckFilter();
  FilterTester verifier = new FilterTester(PATH, this.testee, Mutator.all());

  @Test
  public void shouldDeclareTypeAsFilter() {
    assertThat(this.testee.type()).isEqualTo(InterceptorType.FILTER);
  }

  @Test
  public void shouldFilterMutantsThatAlterGetClassCallsInALambda() {
    this.verifier.assertFiltersNMutationFromSample(1, "RemovedCallBug");
  }

  @Test
  public void flteraMutantsThatAlterGetClassInImplicitNullCheck() {
    this.verifier.assertFiltersNMutationFromSample(1, "ImplicitNullCheck");
  }

  @Test
  public void shouldNotFilterDeadCallsToGetClassInNonLambdaMethods() {
    this.verifier.assertFiltersNMutationFromClass(0, HasDeadCode.class);
  }

}

class HasDeadCode {
  public void foo(Object o) {
    o.getClass();
  }
}

