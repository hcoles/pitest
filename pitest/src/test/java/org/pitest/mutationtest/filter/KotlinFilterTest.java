package org.pitest.mutationtest.filter;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;

import org.junit.Test;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.MutationDetailsMother;

public class KotlinFilterTest {
  
  KotlinFilter testee = new KotlinFilter();

  @Test
  public void shouldNotFilterMutationsInLineZeroOfAJavaClass() {
    List<MutationDetails> mutations = MutationDetailsMother.aMutationDetail()
    .withFilename("Foo.java")
    .withLineNumber(0)
    .build(0);
    assertThat(testee.filter(mutations)).containsAll(mutations);
  }
  
  @Test
  public void shouldFilterMutationsInLineZeroOfAKotlinClass() {
    List<MutationDetails> mutations = MutationDetailsMother.aMutationDetail()
    .withFilename("Foo.kt")
    .withLineNumber(0)
    .build(0);
    assertThat(testee.filter(mutations)).isEmpty();
  }
  
  @Test
  public void shouldNotFilterMutationsOhterLinesOfAKotlinClass() {
    List<MutationDetails> mutations = MutationDetailsMother.aMutationDetail()
    .withFilename("Foo.kt")
    .withLineNumber(1)
    .build(0);
    assertThat(testee.filter(mutations)).containsAll(mutations);
  }
  
  @Test
  public void shouldNotCareAboutCaseOfKotlinfileExtension() {
    List<MutationDetails> mutations = MutationDetailsMother.aMutationDetail()
    .withFilename("Foo.kT")
    .withLineNumber(0)
    .build(0);
    assertThat(testee.filter(mutations)).isEmpty();
  }

}
