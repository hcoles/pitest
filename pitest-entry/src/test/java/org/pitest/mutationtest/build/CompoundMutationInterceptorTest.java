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
    when(this.modifyChild.type()).thenReturn(InterceptorType.MODIFY);
    when(this.filterChild.type()).thenReturn(InterceptorType.FILTER);
    when(this.otherChild.type()).thenReturn(InterceptorType.OTHER);
    when(this.cosmeticChild.type()).thenReturn(InterceptorType.MODIFY_COSMETIC);
    when(this.reportChild.type()).thenReturn(InterceptorType.REPORT);
  }

  @Test
  public void shouldNotifyAllChildrenOfNewClass() {
    this.testee = new CompoundMutationInterceptor(Arrays.asList(this.modifyChild,this.filterChild));
    final ClassTree aClass = new ClassTree(null);

    this.testee.begin(aClass);
    verify(this.modifyChild).begin(aClass);
    verify(this.filterChild).begin(aClass);
  }

  @Test
  public void shouldChainModifiedMutantListsThroughChildrenInCorrectOrder() {

    // add out of order
    this.testee = new CompoundMutationInterceptor(Arrays.asList(this.cosmeticChild, this.otherChild, this.modifyChild, this.reportChild, this.filterChild));

    final Collection<MutationDetails> original = aMutationDetail().build(1);
    final Collection<MutationDetails> modifyResult =  aMutationDetail().build(2);
    final Collection<MutationDetails> filterResult =  aMutationDetail().build(3);
    final Collection<MutationDetails> reportResult =  aMutationDetail().build(3);
    final Collection<MutationDetails> cosmeticResult =  aMutationDetail().build(3);
    final Collection<MutationDetails> otherResult =  aMutationDetail().build(3);

    when(this.modifyChild.intercept(any(Collection.class), any(Mutater.class))).thenReturn(modifyResult);
    when(this.filterChild.intercept(any(Collection.class), any(Mutater.class))).thenReturn(filterResult);
    when(this.reportChild.intercept(any(Collection.class), any(Mutater.class))).thenReturn(reportResult);
    when(this.cosmeticChild.intercept(any(Collection.class), any(Mutater.class))).thenReturn(cosmeticResult);
    when(this.otherChild.intercept(any(Collection.class), any(Mutater.class))).thenReturn(otherResult);

    final Collection<MutationDetails> actual = this.testee.intercept(original, this.mutater);

    assertThat(actual).isEqualTo(reportResult);

    verify(this.otherChild).intercept(original,this.mutater);
    verify(this.modifyChild).intercept(otherResult,this.mutater);
    verify(this.filterChild).intercept(modifyResult,this.mutater);
    verify(this.cosmeticChild).intercept(cosmeticResult,this.mutater);
    verify(this.reportChild).intercept(cosmeticResult,this.mutater);
  }

  @Test
  public void shouldNotifyAllChildrenOfEnd() {
    this.testee = new CompoundMutationInterceptor(Arrays.asList(this.modifyChild,this.filterChild));
    this.testee.end();

    verify(this.modifyChild).end();
    verify(this.filterChild).end();
  }

}
