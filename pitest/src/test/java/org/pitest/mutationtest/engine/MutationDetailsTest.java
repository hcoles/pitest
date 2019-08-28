package org.pitest.mutationtest.engine;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

public class MutationDetailsTest {

  @Test
  public void shouldCreateNewCopyWithUpdatedPoisonStatusForStaticInitializerMarking() {
    final MutationDetails testee = MutationDetailsMother.aMutationDetail().build();

    final MutationDetails actual = testee.withPoisonStatus(PoisonStatus.IS_STATIC_INITIALIZER_CODE);
    assertThat(actual).isNotSameAs(testee);
    assertThat(actual.mayPoisonJVM()).isTrue();
    assertThat(actual.isInStaticInitializer()).isTrue();
  }

  @Test
  public void shouldCreateNewCopyWithUpdatedPoisonStatusForJVMPoisoningMarking() {
    final MutationDetails testee = MutationDetailsMother.aMutationDetail().build();

    final MutationDetails actual = testee.withPoisonStatus(PoisonStatus.MAY_POISON_JVM);
    assertThat(actual).isNotSameAs(testee);
    assertThat(actual.mayPoisonJVM()).isTrue();
  }
  
  @Test
  public void shouldDefaultFilenameWhenNoneKnown() {
    final MutationDetails testee = MutationDetailsMother
        .aMutationDetail()
        .withFilename(null)
        .build();
    assertThat(testee.getFilename()).isEqualTo("unknown_source");
  }

  @Test
  public void shouldObeyHashcodeEqualsContract() {
    EqualsVerifier.forClass(MutationDetails.class).verify();
  }

}
