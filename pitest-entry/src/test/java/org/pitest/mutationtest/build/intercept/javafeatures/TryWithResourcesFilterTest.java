package org.pitest.mutationtest.build.intercept.javafeatures;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.mutationtest.engine.gregor.config.Mutator;

public class TryWithResourcesFilterTest {

  private static final String             PATH      = "trywithresources/{0}_{1}";

  TryWithResourcesFilter testee = new TryWithResourcesFilter();
  
  FilterTester verifier = new FilterTester(PATH, testee, Mutator.defaults());
    
  @Test
  public void shouldDeclareTypeAsFilter() {
    assertEquals(InterceptorType.FILTER, this.testee.type());
  }
  
  @Test
  public void shouldWorkWithTry() {
    verifier.assertLeavesNMutants(1, "TryExample");
  }
  
  @Test
  public void shouldWorkWithTryCatch() {
    verifier.assertLeavesNMutants(2, "TryCatchExample");
  }
  
  @Test
  public void shouldWorkWithTryWithInterface() {
    verifier.assertLeavesNMutants(1, "TryWithInterfaceExample");
  }
  
  @Test
  public void shouldWorkWithTryWithNestedTry() {
    verifier.assertLeavesNMutants(1, "TryWithNestedTryExample");
  }
  
  @Test
  public void shouldWorkWithTwoClosables() {
    verifier.assertLeavesNMutants(1, "TryWithTwoCloseableExample");
  }
  
}
