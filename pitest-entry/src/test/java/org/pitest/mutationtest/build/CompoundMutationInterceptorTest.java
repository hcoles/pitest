package org.pitest.mutationtest.build;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.pitest.mutationtest.engine.MutationDetailsMother.aMutationDetail;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;

@RunWith(MockitoJUnitRunner.class)
public class CompoundMutationInterceptorTest {

  @Mock
  MutationInterceptor modifyChild;
  
  @Mock 
  MutationInterceptor filterChild;
  
  @Mock 
  MutationInterceptor otherChild;
  
  @Mock 
  MutationInterceptor reportChild;
  
  @Mock 
  MutationInterceptor cosmeticChild;
  
  @Mock
  Mutater mutater;
  
  CompoundMutationInterceptor testee; 
  
  @Before
  public void setUp() {
    when(modifyChild.type()).thenReturn(InterceptorType.MODIFY);
    when(filterChild.type()).thenReturn(InterceptorType.FILTER);
    when(otherChild.type()).thenReturn(InterceptorType.OTHER);
    when(cosmeticChild.type()).thenReturn(InterceptorType.MODIFY_COSMETIC);
    when(reportChild.type()).thenReturn(InterceptorType.REPORT);
  }
  
  @Test
  public void shouldNotifyAllChildrenOfNewClass() {
    testee = new CompoundMutationInterceptor(Arrays.asList(modifyChild,filterChild));
    ClassTree aClass = new ClassTree(null);

    testee.begin(aClass);
    verify(modifyChild).begin(aClass);    
    verify(filterChild).begin(aClass);  
  }
  
  @SuppressWarnings("unchecked")
  @Test
  public void shouldChainModifiedMutantListsThroughChildrenInCorrectOrder() {

    // add out of order
    testee = new CompoundMutationInterceptor(Arrays.asList(cosmeticChild, otherChild, modifyChild, reportChild, filterChild));
    
    Collection<MutationDetails> original = aMutationDetail().build(1);
    Collection<MutationDetails> modifyResult =  aMutationDetail().build(2);
    Collection<MutationDetails> filterResult =  aMutationDetail().build(3);
    Collection<MutationDetails> reportResult =  aMutationDetail().build(3);
    Collection<MutationDetails> cosmeticResult =  aMutationDetail().build(3);
    Collection<MutationDetails> otherResult =  aMutationDetail().build(3);
    
    when(modifyChild.intercept(any(Collection.class), any(Mutater.class))).thenReturn(modifyResult);
    when(filterChild.intercept(any(Collection.class), any(Mutater.class))).thenReturn(filterResult);
    when(reportChild.intercept(any(Collection.class), any(Mutater.class))).thenReturn(reportResult);
    when(cosmeticChild.intercept(any(Collection.class), any(Mutater.class))).thenReturn(cosmeticResult);
    when(otherChild.intercept(any(Collection.class), any(Mutater.class))).thenReturn(otherResult);
    
    Collection<MutationDetails> actual = testee.intercept(original, mutater);
    
    assertThat(actual).isEqualTo(reportResult);
    
    verify(otherChild).intercept(original,mutater);  
    verify(modifyChild).intercept(otherResult,mutater);
    verify(filterChild).intercept(modifyResult,mutater);  
    verify(cosmeticChild).intercept(cosmeticResult,mutater); 
    verify(reportChild).intercept(cosmeticResult,mutater); 
  }
  
  @Test
  public void shouldNotifyAllChildrenOfEnd() {
    testee = new CompoundMutationInterceptor(Arrays.asList(modifyChild,filterChild));
    testee.end();
    
    verify(modifyChild).end();    
    verify(filterChild).end();  
  }

}
