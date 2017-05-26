package org.pitest.mutationtest.filter;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.pitest.mutationtest.LocationMother.aMutationId;

import java.util.Collection;

import org.junit.Test;
import org.pitest.classpath.CodeSource;
import org.pitest.mutationtest.LocationMother.MutationIdentifierBuilder;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.MutationDetailsMother;
import org.pitest.mutationtest.filter.support.MutationOnSwitchInstructionLookup;

public class RemoveConditionalMutationForSwitchOnStringFilterTest {

  @Test
  public void shouldFilterOutRemoveConditionalMutator_EQUAL_IF_MutationOnSwitch()
      throws Exception {
    // given
    final CodeSource source = mock(CodeSource.class);
    final MutationOnSwitchInstructionLookup mutationOnSwitchInstructionLookup = mock(
        MutationOnSwitchInstructionLookup.class);
    final RemoveConditionalMutationForSwitchOnStringFilter filter = new RemoveConditionalMutationForSwitchOnStringFilter(
        mutationOnSwitchInstructionLookup, source);

    final MutationDetails firstMutation = MutationDetailsMother
        .aMutationDetail()
        .withId(aMutationId()
            .withMutator(
                "org.pitest.mutationtest.engine.gregor.mutators.RemoveConditionalMutator_EQUAL_IF")
            .withIndex(1))
        .build();

    final MutationDetails secondMutation = MutationDetailsMother
        .aMutationDetail()
        .withId(aMutationId()
            .withMutator(
                "org.pitest.mutationtest.engine.gregor.mutators.RemoveConditionalMutator_EQUAL_IF_ELSE")
            .withIndex(2))
        .build();

    given(mutationOnSwitchInstructionLookup
        .isMutationOnSwitchInstruction(firstMutation, source)).willReturn(true);
    given(mutationOnSwitchInstructionLookup
        .isMutationOnSwitchInstruction(secondMutation, source))
            .willReturn(true);

    // when
    final Collection<MutationDetails> filtered = filter
        .filter(asList(firstMutation, secondMutation));

    // then
    assertThat(filtered).isEmpty();
  }

  @Test
  public void shouldLeaveMutationsOtherThan_RemoveConditionalMutator_EQUAL_IF()
      throws Exception {
    // given
    final CodeSource source = mock(CodeSource.class);
    final MutationOnSwitchInstructionLookup mutationOnSwitchInstructionLookup = mock(
        MutationOnSwitchInstructionLookup.class);
    final RemoveConditionalMutationForSwitchOnStringFilter filter = new RemoveConditionalMutationForSwitchOnStringFilter(
        mutationOnSwitchInstructionLookup, source);

    final MutationIdentifierBuilder id = aMutationId()
        .withMutator("SOME_OTHER_MUTATION");

    final MutationDetails firstMutation = MutationDetailsMother
        .aMutationDetail().withId(id.withIndex(1)).build();

    final MutationDetails secondMutation = MutationDetailsMother
        .aMutationDetail().withId(id.withIndex(2)).build();

    // when
    final Collection<MutationDetails> filtered = filter
        .filter(asList(firstMutation, secondMutation));

    // then
    assertThat(filtered).containsOnly(firstMutation, secondMutation);
    verifyZeroInteractions(mutationOnSwitchInstructionLookup);
  }

  @Test
  public void shouldLeaveRemoveConditionalMutator_EQUAL_IF_MutationNotOnSwitch()
      throws Exception {
    // given
    final CodeSource source = mock(CodeSource.class);
    final MutationOnSwitchInstructionLookup mutationOnSwitchInstructionLookup = mock(
        MutationOnSwitchInstructionLookup.class);
    final RemoveConditionalMutationForSwitchOnStringFilter filter = new RemoveConditionalMutationForSwitchOnStringFilter(
        mutationOnSwitchInstructionLookup, source);

    final MutationDetails firstMutation = MutationDetailsMother
        .aMutationDetail()
        .withId(aMutationId()
            .withMutator(
                "org.pitest.mutationtest.engine.gregor.mutators.RemoveConditionalMutator_EQUAL_IF")
            .withIndex(1))
        .build();

    final MutationDetails secondMutation = MutationDetailsMother
        .aMutationDetail()
        .withId(aMutationId()
            .withMutator(
                "org.pitest.mutationtest.engine.gregor.mutators.RemoveConditionalMutator_EQUAL_IF_ELSE")
            .withIndex(2))
        .build();

    given(mutationOnSwitchInstructionLookup
        .isMutationOnSwitchInstruction(firstMutation, source))
            .willReturn(false);
    given(mutationOnSwitchInstructionLookup
        .isMutationOnSwitchInstruction(secondMutation, source))
            .willReturn(false);

    // when
    final Collection<MutationDetails> filtered = filter
        .filter(asList(firstMutation, secondMutation));

    // then
    assertThat(filtered).containsOnly(firstMutation, secondMutation);
  }
}
