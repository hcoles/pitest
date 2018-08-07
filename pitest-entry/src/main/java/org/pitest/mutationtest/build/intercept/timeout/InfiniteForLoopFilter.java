package org.pitest.mutationtest.build.intercept.timeout;

import static org.pitest.bytecode.analysis.InstructionMatchers.aConditionalJump;
import static org.pitest.bytecode.analysis.InstructionMatchers.aLabelNode;
import static org.pitest.bytecode.analysis.InstructionMatchers.anILoadOf;
import static org.pitest.bytecode.analysis.InstructionMatchers.anIStore;
import static org.pitest.bytecode.analysis.InstructionMatchers.anIStoreTo;
import static org.pitest.bytecode.analysis.InstructionMatchers.anIntegerConstant;
import static org.pitest.bytecode.analysis.InstructionMatchers.anyInstruction;
import static org.pitest.bytecode.analysis.InstructionMatchers.debug;
import static org.pitest.bytecode.analysis.InstructionMatchers.gotoLabel;
import static org.pitest.bytecode.analysis.InstructionMatchers.incrementsVariable;
import static org.pitest.bytecode.analysis.InstructionMatchers.isA;
import static org.pitest.bytecode.analysis.InstructionMatchers.jumpsTo;
import static org.pitest.bytecode.analysis.InstructionMatchers.labelNode;
import static org.pitest.bytecode.analysis.InstructionMatchers.methodCall;
import static org.pitest.bytecode.analysis.InstructionMatchers.notAnInstruction;
import static org.pitest.bytecode.analysis.InstructionMatchers.opCode;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.pitest.bytecode.analysis.MethodTree;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.sequence.Match;
import org.pitest.sequence.QueryParams;
import org.pitest.sequence.QueryStart;
import org.pitest.sequence.SequenceMatcher;
import org.pitest.sequence.SequenceQuery;
import org.pitest.sequence.Slot;

/**
 * Removes mutants that are likely to result in an infinite or long running
 * for loop or while loop based on a counter.
 */
public class InfiniteForLoopFilter extends InfiniteLoopFilter {

  private static final boolean DEBUG = false;

  static final SequenceMatcher<AbstractInsnNode> INFINITE_LOOP = QueryStart
      .match(Match.<AbstractInsnNode>never())
      .or(countingLoopWithoutWriteConditionalAtStart())
      .or(countingLoopWithoutWriteConditionAtEnd())
      .compile(QueryParams.params(AbstractInsnNode.class)
          .withIgnores(notAnInstruction())
          .withDebug(DEBUG)
          );

  @Override
  SequenceMatcher<AbstractInsnNode> infiniteLoopMatcher() {
    return INFINITE_LOOP;
  }

  @Override
  boolean couldCauseInfiniteLoop(MethodTree method, MutationDetails each) {
    final AbstractInsnNode instruction = method.instruction(each.getInstructionIndex());
    return instruction.getOpcode() == Opcodes.IINC;
  }

  private static SequenceQuery<AbstractInsnNode> countingLoopWithoutWriteConditionalAtStart() {
    final Slot<Integer> counterVariable = Slot.create(Integer.class);
    final Slot<LabelNode> loopStart = Slot.create(LabelNode.class);
    return QueryStart
        .any(AbstractInsnNode.class)
        .then(anIntegerConstant().and(debug("constant")))
        .zeroOrMore(QueryStart.match(opCode(Opcodes.IADD))) // FIXME just one rather than many
        .then(anIStore(counterVariable.write()).and(debug("counter")))
        .zeroOrMore(QueryStart.match(opCode(Opcodes.ILOAD)
                                    .or(opCode(Opcodes.ALOAD)
                                    .or(opCode(Opcodes.ISTORE))
                                    .or(methodCall()))))
        .then(aLabelNode(loopStart.write()).and(debug("label")))
        .then(anILoadOf(counterVariable.read()).and(debug("load")))
        .zeroOrMore(doesNotBreakLoop(counterVariable))
        .zeroOrMore(QueryStart.match(opCode(Opcodes.ILOAD).or(opCode(Opcodes.ALOAD).or(methodCall()))))
        .then(aConditionalJump().and(debug("jump")))
        .zeroOrMore(doesNotBreakLoop(counterVariable))
        .then(jumpsTo(loopStart.read()))
        .zeroOrMore(QueryStart.match(anyInstruction()));
  }

  private static SequenceQuery<AbstractInsnNode> countingLoopWithoutWriteConditionAtEnd() {
    final Slot<Integer> counterVariable = Slot.create(Integer.class);
    final Slot<LabelNode> loopStart = Slot.create(LabelNode.class);
    final Slot<LabelNode> loopEnd = Slot.create(LabelNode.class);

    return QueryStart
        .any(AbstractInsnNode.class)
        .then(anIntegerConstant())
        .then(anIStore(counterVariable.write()).and(debug("counter")))
        .then(isA(LabelNode.class))
        .then(gotoLabel(loopEnd.write()))
        .then(aLabelNode(loopStart.write()).and(debug("loop start")))
        .zeroOrMore(doesNotBreakLoop(counterVariable))
        .then(labelNode(loopEnd.read()).and(debug("loop end")))
        .then(anILoadOf(counterVariable.read()).and(debug("read"))) // is it really important that we read the counter?
        .zeroOrMore(doesNotBreakLoop(counterVariable))
        .then(jumpsTo(loopStart.read()).and(debug("jump")))
        .zeroOrMore(QueryStart.match(anyInstruction()));
  }

  private static SequenceQuery<AbstractInsnNode> doesNotBreakLoop(Slot<Integer> counterVariable) {
    return QueryStart
        .match(anIStoreTo(counterVariable.read()).and(debug("broken by store"))
            .or(incrementsVariable(counterVariable.read()))
            .negate());
  }


}
