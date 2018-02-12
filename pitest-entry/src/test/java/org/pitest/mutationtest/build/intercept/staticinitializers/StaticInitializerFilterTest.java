package org.pitest.mutationtest.build.intercept.staticinitializers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.pitest.mutationtest.engine.MutationDetailsMother.aMutationDetail;

import java.util.Collection;

import org.junit.Test;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.PoisonStatus;

public class StaticInitializerFilterTest {

  StaticInitializerFilter testee  = new StaticInitializerFilter();

  @Test
  public void shouldRemoveMutationsInStaticInitCode() {
    final Collection<MutationDetails> marked = aMutationDetail()
        .withPoison(PoisonStatus.IS_STATIC_INITIALIZER_CODE)
        .build(2);

    assertThat(this.testee.intercept(marked, null)).isEmpty();
  }

  @Test
  public void shouldNotFilterNotStaticMutants() {
    final Collection<MutationDetails> unmarked = aMutationDetail()
        .build(2);
    assertThat(this.testee.intercept(unmarked, null)).containsAll(unmarked);
  }

}
