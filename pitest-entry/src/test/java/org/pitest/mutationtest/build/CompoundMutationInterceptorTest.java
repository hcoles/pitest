package org.pitest.mutationtest.build;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class CompoundMutationInterceptorTest {

  @Mock
  MutationInterceptor child1;
  
  @Mock 
  MutationInterceptor child2;
  
  @Mock
  Mutater mutater;
  
  CompoundMutationInterceptor testee; 
  
  @Before
  public void setUp() {
    testee = new CompoundMutationInterceptor(Arrays.asList(child1,child2));
  }
  
  @Test
  public void shouldNotifyAllChildrenOfNewClass() {
    ClassName aClass = ClassName.fromString("foo");

    testee.begin(aClass);
    verify(child1).begin(aClass);    
    verify(child2).begin(aClass);  
  }
  
  @SuppressWarnings("unchecked")
  @Test
  public void shouldChainModifiedMutantListsThroughChildren() {

    Collection<MutationDetails> original = new ArrayList<MutationDetails>();
    Collection<MutationDetails> child1Result = new ArrayList<MutationDetails>();
    Collection<MutationDetails> child2Result = new ArrayList<MutationDetails>();
    
    when(child1.intercept(any(Collection.class), any(Mutater.class))).thenReturn(child1Result);
    when(child2.intercept(any(Collection.class), any(Mutater.class))).thenReturn(child2Result);
    
    
    Collection<MutationDetails> actual = testee.intercept(original, mutater);
    
    assertThat(actual).isEqualTo(child2Result);
    
    verify(child1).intercept(original,mutater);  
    verify(child2).intercept(child1Result,mutater);   
  }
  
  @Test
  public void shouldNotifyAllChildrenOfEnd() {
    
    testee.end();
    
    verify(child1).end();    
    verify(child2).end();  
  }

}
