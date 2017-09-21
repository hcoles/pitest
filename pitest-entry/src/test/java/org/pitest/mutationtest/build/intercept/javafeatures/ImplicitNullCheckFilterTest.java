package org.pitest.mutationtest.build.intercept.javafeatures;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.mutationtest.engine.gregor.config.Mutator;

public class ImplicitNullCheckFilterTest {

  private static final String             PATH      = "implicitnullcheck/{0}_{1}";
  
  ImplicitNullCheckFilter testee = new ImplicitNullCheckFilter();
  FilterTester verifier = new FilterTester(PATH, testee, Mutator.all());  
  
  @Test
  public void shouldDeclareTypeAsFilter() {
    assertThat(testee.type()).isEqualTo(InterceptorType.FILTER);
  }
  
  @Test
  public void shouldFilterMutantsThatAlterGetClassCallsInALambda() {
    verifier.assertFiltersNMutationFromSample(1, "RemovedCallBug");
  }
  
  @Test
  public void doesNotFilterMutantsThatAlterGetClassInImplicitNullCheck() {
    // Could probably filter this if we looked for POPs followed by an immediate
    // call to an inner class constructor - but rare enough that it may not be worth the effort
    //INVOKEVIRTUAL java/lang/Object.getClass ()Ljava/lang/Class;
    //POP
    //INVOKESPECIAL com/example/ImplicitNullCheck$Inner.<init> (Lcom/example/ImplicitNullCheck;)V
    verifier.assertFiltersNMutationFromSample(0, "ImplicitNullCheck");
  }
  
  @Test
  public void shouldNotFilterDeadCallsToGetClassInNonLambdaMethods() {
    verifier.assertFiltersNMutationFromClass(0, HasDeadCode.class);
  }

}

class HasDeadCode {
  public void foo(Object o) {
    o.getClass();
  }
}

