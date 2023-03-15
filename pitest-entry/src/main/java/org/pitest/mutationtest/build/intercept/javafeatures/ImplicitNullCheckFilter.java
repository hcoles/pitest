package org.pitest.mutationtest.build.intercept.javafeatures;

import static org.pitest.bytecode.analysis.InstructionMatchers.anyInstruction;
import static org.pitest.bytecode.analysis.InstructionMatchers.isA;
import static org.pitest.bytecode.analysis.InstructionMatchers.methodCallTo;
import static org.pitest.bytecode.analysis.InstructionMatchers.notAnInstruction;
import static org.pitest.bytecode.analysis.OpcodeMatchers.POP;
import static org.pitest.sequence.Result.result;

import java.util.List;
import java.util.stream.Collectors;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LabelNode;
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

public class ImplicitNullCheckFilter extends RegionInterceptor {

  private static final boolean DEBUG = false;

  private static final Slot<AbstractInsnNode> MUTATED_INSTRUCTION = Slot.create(AbstractInsnNode.class);

  static final SequenceMatcher<AbstractInsnNode> GET_CLASS_NULL_CHECK = QueryStart
      .any(AbstractInsnNode.class)
      .then(aGetClassCall().and(store(MUTATED_INSTRUCTION.write())))
      .then(POP) // immediate discard
      .then(isA(LabelNode.class).negate()) // use presence of a label to indicate this was a programmer call to getClass
      .zeroOrMore(QueryStart.match(anyInstruction()))
      .compile(QueryParams.params(AbstractInsnNode.class)
          .withIgnores(notAnInstruction())
          .withDebug(DEBUG)
          );

  private static Match<AbstractInsnNode> aGetClassCall() {
    return methodCallTo(ClassName.fromClass(Object.class), "getClass");
  }

  private static Match<AbstractInsnNode> store(SlotWrite<AbstractInsnNode> slot) {
    return (c, n) -> result(true, c.store(slot, n));
  }

  @Override
  protected List<Region> computeRegions(MethodTree method) {
    Context context = Context.start();
    return GET_CLASS_NULL_CHECK.contextMatches(method.instructions(), context).stream()
            .map(c -> new Region(c.retrieve(MUTATED_INSTRUCTION.read()).get(), c.retrieve(MUTATED_INSTRUCTION.read()).get()))
            .collect(Collectors.toList());
  }

}
