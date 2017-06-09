package org.pitest.mutationtest.build.intercept;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.pitest.mutationtest.engine.MutationDetailsMother.aMutationDetail;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pitest.mutationtest.build.ClassTree;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.PoisonStatus;

@RunWith(MockitoJUnitRunner.class)
public class StaticInitializerFilterTest {
  
  @Mock
  StaticInitializerInterceptor interceptor;
  
  StaticInitializerFilter testee;  
  
  @Before
  public void setUp() {
    testee = new StaticInitializerFilter(interceptor);
  }
  
  
  @Test
  public void shouldForwardBeginCallsToChild() {
    ClassTree clazz = new ClassTree(null);
    testee.begin(clazz);
    
    verify(interceptor).begin(clazz);
  }
  
  @Test
  public void shouldForwardEndCallsToChild() {
    testee.end();
    
    verify(interceptor).end();
  }

  @SuppressWarnings("unchecked")
  @Test
  public void shouldRemoveMutationsInStaticInitCode() {
    Collection<MutationDetails> input = aMutationDetail()
        .build(2);
    
    Collection<MutationDetails> marked = aMutationDetail()
        .withPoison(PoisonStatus.IS_STATIC_INITIALIZER_CODE)
        .build(2);
    
    when(interceptor.intercept(any(Collection.class), any(Mutater.class))).thenReturn(marked);
    
    assertThat(testee.intercept(input, null)).isEmpty();
  }
  
  @SuppressWarnings("unchecked")
  @Test
  public void shouldNotFilterNotStaticMutants() {
    Collection<MutationDetails> unmarked = aMutationDetail()
        .build(2);
    
    when(interceptor.intercept(any(Collection.class), any(Mutater.class))).thenReturn(unmarked);
    
    assertThat(testee.intercept(unmarked, null)).containsAll(unmarked);
  }
  
}
