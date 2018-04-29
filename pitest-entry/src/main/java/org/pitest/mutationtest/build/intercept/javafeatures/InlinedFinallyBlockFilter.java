package org.pitest.mutationtest.build.intercept.javafeatures;

import static org.pitest.functional.FCollection.bucket;
import static org.pitest.functional.FCollection.map;
import static org.pitest.functional.FCollection.mapTo;
import static org.pitest.functional.prelude.Prelude.isEqualTo;
import static org.pitest.functional.prelude.Prelude.not;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;

import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.functional.FCollection;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.util.Log;

/**
 * Detects mutations on same line, but within different code blocks. This
 * pattern indicates code inlined for a finally block . . . or normal code that
 * creates two blocks on the same line.
 *
 * Cannot be used with code that uses single line if statements
 */
public class InlinedFinallyBlockFilter implements MutationInterceptor {

  private static final Logger LOG = Log.getLogger();

  @Override
  public InterceptorType type() {
    return InterceptorType.FILTER;
  }

  @Override
  public void begin(ClassTree clazz) {
    // no-opp
  }

  @Override
  public Collection<MutationDetails> intercept(
      Collection<MutationDetails> mutations, Mutater m) {
    final List<MutationDetails> combined = new ArrayList<>(
        mutations.size());
    final Map<LineMutatorPair, Collection<MutationDetails>> mutatorLinebuckets = bucket(
        mutations, toLineMutatorPair());

    for (final Entry<LineMutatorPair, Collection<MutationDetails>> each : mutatorLinebuckets
        .entrySet()) {
      if (each.getValue().size() > 1) {
        checkForInlinedCode(combined, each);
      } else {
        combined.addAll(each.getValue());
      }
    }

    /** FIXME tests rely on order of returned mutants **/
    Collections.sort(combined, compareLineNumbers());
    return combined;
  }

  @Override
  public void end() {
    // no-opp
  }

  private static Comparator<MutationDetails> compareLineNumbers() {
    return (arg0, arg1) -> arg0.getLineNumber() - arg1.getLineNumber();
  }

  private void checkForInlinedCode(final Collection<MutationDetails> combined,
      final Entry<LineMutatorPair, Collection<MutationDetails>> each) {

    final List<MutationDetails> mutationsInHandlerBlock = FCollection
        .filter(each.getValue(), isInFinallyHandler());
    if (!isPossibleToCorrectInlining(mutationsInHandlerBlock)) {
      combined.addAll(each.getValue());
      return;
    }

    final MutationDetails baseMutation = mutationsInHandlerBlock.get(0);
    final int firstBlock = baseMutation.getBlock();

    // check that we have at least on mutation in a different block
    // to the base one (is this not implied by there being only 1 mutation in
    // the handler ????)
    final List<Integer> ids = map(each.getValue(), mutationToBlock());
    if (ids.stream().filter(not(isEqualTo(firstBlock))).findFirst().isPresent()) {
      combined.add(makeCombinedMutant(each.getValue()));
    } else {
      combined.addAll(each.getValue());
    }
  }

  private boolean isPossibleToCorrectInlining(
      final List<MutationDetails> mutationsInHandlerBlock) {
    if (mutationsInHandlerBlock.size() > 1) {
      LOG.warning("Found more than one mutation similar on same line in a finally block. Can't correct for inlining.");
      return false;
    }

    return !mutationsInHandlerBlock.isEmpty();
  }

  private static Predicate<MutationDetails> isInFinallyHandler() {
    return a -> a.isInFinallyBlock();
  }

  private static MutationDetails makeCombinedMutant(
      final Collection<MutationDetails> value) {
    final MutationDetails first = value.iterator().next();
    final Set<Integer> indexes = new HashSet<>();
    mapTo(value, mutationToIndex(), indexes);

    final MutationIdentifier id = new MutationIdentifier(first.getId()
        .getLocation(), indexes, first.getId().getMutator());

    return new MutationDetails(id, first.getFilename(), first.getDescription(),
        first.getLineNumber(), first.getBlock());
  }

  private static Function<MutationDetails, Integer> mutationToIndex() {
    return a -> a.getFirstIndex();
  }

  private static Function<MutationDetails, Integer> mutationToBlock() {
    return a -> a.getBlock();
  }

  private static Function<MutationDetails, LineMutatorPair> toLineMutatorPair() {
    return a -> new LineMutatorPair(a.getLineNumber(), a.getMutator());
  }

}
