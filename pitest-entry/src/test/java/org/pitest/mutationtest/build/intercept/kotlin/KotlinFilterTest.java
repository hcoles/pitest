package org.pitest.mutationtest.build.intercept.kotlin;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;

import org.junit.Test;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.MutationDetailsMother;

public class KotlinFilterTest {
  
  KotlinFilter testee = new KotlinFilter();
  private Mutater unused;

  @Test
  public void shouldDeclareTypeAsFilter() {
    assertThat(testee.type()).isEqualTo(InterceptorType.FILTER);
  }
  
  @Test
  public void shouldNotFilterMutationsInLineZeroOfAJavaClass() {
    List<MutationDetails> mutations = MutationDetailsMother.aMutationDetail()
    .withFilename("Foo.java")
    .withLineNumber(0)
    .build(0);
    assertThat(testee.intercept(mutations, unused)).containsAll(mutations);
  }
  
  @Test
  public void shouldFilterMutationsInLineZeroOfAKotlinClass() {
    List<MutationDetails> mutations = MutationDetailsMother.aMutationDetail()
    .withFilename("Foo.kt")
    .withLineNumber(0)
    .build(0);
    assertThat(testee.intercept(mutations, unused)).isEmpty();
  }
  
  @Test
  public void shouldNotFilterMutationsOhterLinesOfAKotlinClass() {
    List<MutationDetails> mutations = MutationDetailsMother.aMutationDetail()
    .withFilename("Foo.kt")
    .withLineNumber(1)
    .build(0);
    assertThat(testee.intercept(mutations, unused)).containsAll(mutations);
  }
  
  @Test
  public void shouldNotCareAboutCaseOfKotlinfileExtension() {
    List<MutationDetails> mutations = MutationDetailsMother.aMutationDetail()
    .withFilename("Foo.kT")
    .withLineNumber(0)
    .build(0);
    assertThat(testee.intercept(mutations, unused)).isEmpty();
  }

}
