package org.pitest.mutationtest.filter;

import java.util.Collection;

import org.pitest.classpath.CodeSource;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.prelude.Prelude;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.filter.support.MutationOnSwitchInstructionLookup;

/**
 * Filters out RemoveConditional EQUAL mutations that are created for switch
 * with String.
 */
class RemoveConditionalMutationForSwitchOnStringFilter
    implements MutationFilter {

  private final MutationOnSwitchInstructionLookup mutationOnSwitchInstructionLookup;

  private final CodeSource                        source;

  RemoveConditionalMutationForSwitchOnStringFilter(
      final MutationOnSwitchInstructionLookup mutationOnSwitchInstructionLookup,
      final CodeSource source) {
    this.mutationOnSwitchInstructionLookup = mutationOnSwitchInstructionLookup;
    this.source = source;
  }

  @Override
  public Collection<MutationDetails> filter(
      final Collection<MutationDetails> mutations) {

    return FCollection.filter(mutations,
        Prelude.not(isRemoveConditionalMutationForSwitchOnString()));
  }

  private boolean isMutationOnSwitchInstruction(
      final MutationDetails mutation) {
    return mutationOnSwitchInstructionLookup
        .isMutationOnSwitchInstruction(mutation, this.source);
  }

  private boolean isRemoveConditionalMutation(final MutationDetails mutation) {
    return mutation.getId().getMutator().startsWith(
        "org.pitest.mutationtest.engine.gregor.mutators.RemoveConditionalMutator_EQUAL");
  }

  private F<MutationDetails, Boolean> isRemoveConditionalMutationForSwitchOnString() {
    return new F<MutationDetails, Boolean>() {
      @Override
      public Boolean apply(final MutationDetails mutation) {
        return isRemoveConditionalMutation(mutation)
            && isMutationOnSwitchInstruction(mutation);
      }
    };
  }

}
