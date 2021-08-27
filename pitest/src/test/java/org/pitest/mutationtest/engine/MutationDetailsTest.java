package org.pitest.mutationtest.engine;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

public class MutationDetailsTest {

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
    EqualsVerifier.forClass(MutationDetails.class)
            .withOnlyTheseFields("id")
            .verify();
  }

}
