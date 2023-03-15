package org.pitest.mutationtest.build.intercept.javafeatures;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.pitest.bytecode.analysis.MethodTree;
import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.build.intercept.Region;
import org.pitest.mutationtest.build.intercept.RegionInterceptor;
import org.pitest.sequence.Context;
import org.pitest.sequence.Match;
import org.pitest.sequence.QueryParams;
import org.pitest.sequence.QueryStart;
import org.pitest.sequence.SequenceMatcher;
import org.pitest.sequence.Slot;
import org.pitest.sequence.SlotWrite;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.pitest.bytecode.analysis.InstructionMatchers.anyInstruction;
import static org.pitest.bytecode.analysis.InstructionMatchers.methodCallTo;
import static org.pitest.bytecode.analysis.InstructionMatchers.notAnInstruction;
import static org.pitest.bytecode.analysis.OpcodeMatchers.INVOKEDYNAMIC;
import static org.pitest.bytecode.analysis.OpcodeMatchers.POP;
import static org.pitest.sequence.Result.result;

/**
 * Filters out the calls to Objects.requireNotNull the compiler inserts when using method references.
 *
 */
public class MethodReferenceNullCheckFilter extends RegionInterceptor {

  private static final boolean DEBUG = false;
  private static final Slot<AbstractInsnNode> MUTATED_INSTRUCTION = Slot.create(AbstractInsnNode.class);

  static final SequenceMatcher<AbstractInsnNode> NULL_CHECK = QueryStart
      .any(AbstractInsnNode.class)
      .then(requireNonNullCall().and(store(MUTATED_INSTRUCTION.write())))
      .then(POP)
      .then(INVOKEDYNAMIC)
      .zeroOrMore(QueryStart.match(anyInstruction()))
      .compile(QueryParams.params(AbstractInsnNode.class)
          .withIgnores(notAnInstruction())
          .withDebug(DEBUG)
          );

  private static Match<AbstractInsnNode> requireNonNullCall() {
    return methodCallTo(ClassName.fromClass(Objects.class), "requireNonNull");
  }

  @Override
  protected List<Region> computeRegions(MethodTree method) {
    Context context = Context.start();
    return NULL_CHECK.contextMatches(method.instructions(), context).stream()
            .map(c -> new Region(c.retrieve(MUTATED_INSTRUCTION.read()).get(), c.retrieve(MUTATED_INSTRUCTION.read()).get()))
            .collect(Collectors.toList());
  }

  private static Match<AbstractInsnNode> store(SlotWrite<AbstractInsnNode> slot) {
    return (c, n) -> result(true, c.store(slot, n));
  }

}
