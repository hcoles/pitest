package org.pitest.mutationtest.build.intercept.javafeatures;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.bytecode.analysis.MethodTree;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.sequence.Context;
import org.pitest.sequence.Match;
import org.pitest.sequence.QueryParams;
import org.pitest.sequence.QueryStart;
import org.pitest.sequence.SequenceMatcher;
import org.pitest.sequence.Slot;
import org.pitest.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.util.function.Predicate.isEqual;
import static org.pitest.bytecode.analysis.InstructionMatchers.anyInstruction;
import static org.pitest.bytecode.analysis.InstructionMatchers.isInstruction;
import static org.pitest.bytecode.analysis.InstructionMatchers.notAnInstruction;
import static org.pitest.bytecode.analysis.OpcodeMatchers.ARETURN;
import static org.pitest.bytecode.analysis.OpcodeMatchers.ATHROW;
import static org.pitest.bytecode.analysis.OpcodeMatchers.DRETURN;
import static org.pitest.bytecode.analysis.OpcodeMatchers.FRETURN;
import static org.pitest.bytecode.analysis.OpcodeMatchers.IRETURN;
import static org.pitest.bytecode.analysis.OpcodeMatchers.LRETURN;
import static org.pitest.bytecode.analysis.OpcodeMatchers.RETURN;
import static org.pitest.functional.FCollection.bucket;
import static org.pitest.functional.FCollection.mapTo;
import static org.pitest.functional.prelude.Prelude.not;
import static org.pitest.sequence.Result.result;

/**
 * Detects mutations on same line, but within different code blocks. This
 * pattern indicates code inlined for a finally block . . . or normal code that
 * creates two blocks on the same line.
 *
 * Cannot be used with code that uses single line if statements
 */
public class InlinedFinallyBlockFilter implements MutationInterceptor {

  private static final Logger LOG = Log.getLogger();

  private static final boolean DEBUG = false;

  private static final Slot<AbstractInsnNode> MUTATED_INSTRUCTION = Slot.create(AbstractInsnNode.class);
  private static final Slot<List> HANDLERS = Slot.create(List.class);

  static final SequenceMatcher<AbstractInsnNode> IS_IN_HANDLER = QueryStart
          .any(AbstractInsnNode.class)
          .then(handlerLabel(HANDLERS))
          .zeroOrMore(QueryStart.match(doesNotEndBlock()))
          .then(isInstruction(MUTATED_INSTRUCTION.read()))
          .zeroOrMore(QueryStart.match(anyInstruction()))
          .compile(QueryParams.params(AbstractInsnNode.class)
                  .withIgnores(notAnInstruction())
                  .withDebug(DEBUG)
          );

  private static Match<AbstractInsnNode> doesNotEndBlock() {
     return endsBlock().negate();
  }

  private static Match<AbstractInsnNode> endsBlock() {
    return RETURN
            .or(ARETURN)
            .or(DRETURN)
            .or(FRETURN)
            .or(IRETURN)
            .or(LRETURN)
            .or(ATHROW); // dubious if this is needed
  }

  private static Match<AbstractInsnNode> handlerLabel(Slot<List> handlers) {
    return (c,t) -> {
      if (t instanceof LabelNode) {
        LabelNode label = (LabelNode) t;
        List<LabelNode> labels = c.retrieve(handlers.read()).get();
        return result(labels.contains(label), c);
      }
      return result(false, c);
    };
  }

  private ClassTree currentClass;

  @Override
  public InterceptorType type() {
    return InterceptorType.FILTER;
  }

  @Override
  public void begin(ClassTree clazz) {
    currentClass = clazz;
  }

  @Override
  public Collection<MutationDetails> intercept(
      Collection<MutationDetails> mutations, Mutater m) {

    List<MutationDetails> combined = new ArrayList<>(
        mutations.size());
    Map<LineMutatorPair, Collection<MutationDetails>> mutatorLineBuckets = bucket(
        mutations, toLineMutatorPair());

    for (final Entry<LineMutatorPair, Collection<MutationDetails>> each : mutatorLineBuckets
        .entrySet()) {
      if (each.getValue().size() > 1) {
        checkForInlinedCode(combined, each.getValue());
      } else {
        combined.addAll(each.getValue());
      }
    }

    combined.sort(compareLineNumbers());
    return combined;
  }

  @Override
  public void end() {
    currentClass = null;
  }

  private static Comparator<MutationDetails> compareLineNumbers() {
    return Comparator.comparingInt(MutationDetails::getLineNumber);
  }

  private void checkForInlinedCode(Collection<MutationDetails> mutantsToReturn,
                                  Collection<MutationDetails> similarMutantsOnSameLine) {

    final List<MutationDetails> mutationsInHandlerBlock = similarMutantsOnSameLine.stream()
            .filter(this::isInFinallyBlock)
            .collect(Collectors.toList());

    if (!isPossibleToCorrectInlining(mutationsInHandlerBlock)) {
      mutantsToReturn.addAll(similarMutantsOnSameLine);
      return;
    }

    final MutationDetails baseMutation = mutationsInHandlerBlock.get(0);
    final int firstBlock = baseMutation.getBlocks().get(0);

    // check that we have at least on mutation in a different block
    // to the base one (is this not implied by there being only 1 mutation in
    // the handler ????)
    final List<Integer> ids = blocksForMutants(similarMutantsOnSameLine);
    if (ids.stream().anyMatch(not(isEqual(firstBlock)))) {
      mutantsToReturn.add(makeCombinedMutant(similarMutantsOnSameLine));
    } else {
      mutantsToReturn.addAll(similarMutantsOnSameLine);
    }
  }



  private boolean isInFinallyBlock(MutationDetails m) {
    Optional<MethodTree> maybeMethod = currentClass.method(m.getId().getLocation());
    if (!maybeMethod.isPresent()) {
      return false;
    }
    MethodTree method = maybeMethod.get();
    List<LabelNode> handlers = method.rawNode().tryCatchBlocks.stream()
            .filter(t -> t.type == null)
            .map(t -> t.handler)
            .collect(Collectors.toList());

    if (handlers.isEmpty()) {
      return false;
    }

    AbstractInsnNode mutatedInstruction = method.instruction(m.getInstructionIndex());

    Context context = Context.start(DEBUG);
    context = context.store(MUTATED_INSTRUCTION.write(), mutatedInstruction);
    context = context.store(HANDLERS.write(), handlers);
    return IS_IN_HANDLER.matches(method.instructions(), context);
  }

  private boolean isPossibleToCorrectInlining(List<MutationDetails> mutationsInHandlerBlock) {
    if (mutationsInHandlerBlock.size() > 1) {
      LOG.warning("Found more than one mutation similar on same line in a finally block. Can't correct for inlining.");
      return false;
    }

    return !mutationsInHandlerBlock.isEmpty();
  }

  private static MutationDetails makeCombinedMutant(Collection<MutationDetails> value) {
    MutationDetails first = value.iterator().next();
    Set<Integer> indexes = new HashSet<>();
    mapTo(value, MutationDetails::getFirstIndex, indexes);

    final MutationIdentifier id = new MutationIdentifier(first.getId()
        .getLocation(), indexes, first.getId().getMutator());

    return new MutationDetails(id, first.getFilename(), first.getDescription(),
        first.getLineNumber(), blocksForMutants(value));
  }

  private static Function<MutationDetails, LineMutatorPair> toLineMutatorPair() {
    // bucket by combination of mutator and description
    return a -> new LineMutatorPair(a.getLineNumber(), a.getMutator() + a.getDescription());
  }

  private static List<Integer> blocksForMutants(Collection<MutationDetails> mutants) {
    return mutants.stream()
            .flatMap(m -> m.getBlocks().stream())
            .collect(Collectors.toList());
  }

}
