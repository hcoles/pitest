package org.pitest.mutationtest.engine;

import nl.jqno.equalsverifier.EqualsVerifier;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class MutationDetailsTest {
  
  @Test
  public void shouldCreateNewCopyWithUpdatedPoisonStatusForStaticInitializerMarking() {
    MutationDetails testee = MutationDetailsMother.aMutationDetail().build();
    
    MutationDetails actual = testee.withPoisonStatus(PoisonStatus.IS_STATIC_INITIALIZER_CODE);
    assertThat(actual).isNotSameAs(testee);
    assertThat(actual.mayPoisonJVM()).isTrue();
    assertThat(actual.isInStaticInitializer()).isTrue();    
  }

  @Test
  public void shouldCreateNewCopyWithUpdatedPoisonStatusForJVMPoisoningMarking() {
    MutationDetails testee = MutationDetailsMother.aMutationDetail().build();
    
    MutationDetails actual = testee.withPoisonStatus(PoisonStatus.MAY_POISON_JVM);
    assertThat(actual).isNotSameAs(testee);
    assertThat(actual.mayPoisonJVM()).isTrue();
  }
  
  @Test
  public void shouldObeyHashcodeEqualsContract() {
    EqualsVerifier.forClass(MutationDetails.class).verify();
  }

}
