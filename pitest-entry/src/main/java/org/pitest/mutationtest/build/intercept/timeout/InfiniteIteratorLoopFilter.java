package org.pitest.mutationtest.build.intercept.timeout;

import static org.pitest.bytecode.analysis.InstructionMatchers.aJump;
import static org.pitest.bytecode.analysis.InstructionMatchers.aLabelNode;
import static org.pitest.bytecode.analysis.InstructionMatchers.anyInstruction;
import static org.pitest.bytecode.analysis.InstructionMatchers.jumpsTo;
import static org.pitest.bytecode.analysis.InstructionMatchers.methodCallThatReturns;
import static org.pitest.bytecode.analysis.InstructionMatchers.methodCallTo;
import static org.pitest.bytecode.analysis.InstructionMatchers.notAnInstruction;
import static org.pitest.bytecode.analysis.OpcodeMatchers.ASTORE;

import java.util.Iterator;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.pitest.bytecode.analysis.InstructionMatchers;
import org.pitest.bytecode.analysis.MethodTree;
import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.sequence.QueryParams;
import org.pitest.sequence.QueryStart;
import org.pitest.sequence.SequenceMatcher;
import org.pitest.sequence.SequenceQuery;
import org.pitest.sequence.Slot;

/**
 * Removes mutants that remove the only call to next in an iterator loop
 */
public class InfiniteIteratorLoopFilter extends InfiniteLoopFilter {

  private static final boolean DEBUG = false;

  static final SequenceMatcher<AbstractInsnNode> INFINITE_LOOP = inifniteIteratorLoop()
      .or(infiniteIteratorLoopJavac())
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
    var instruction = method.instructionForIndex(each.getInstructionIndex());
    return instruction
            .map(this::isIteratorNext)
            .orElse(false);
  }

  private static SequenceQuery<AbstractInsnNode> doesNotBreakIteratorLoop() {
    return QueryStart.match(methodCallTo(ClassName.fromClass(Iterator.class), "next").negate());
  }

  private boolean isIteratorNext(AbstractInsnNode instruction) {
    return InstructionMatchers.methodCallTo(ClassName.fromClass(Iterator.class), "next").asPredicate().test(instruction);
  }

  private static SequenceQuery<AbstractInsnNode> inifniteIteratorLoop() {
    final Slot<LabelNode> loopStart = Slot.create(LabelNode.class);

    return QueryStart
        .any(AbstractInsnNode.class)
        .then(methodCallThatReturns(ClassName.fromString("java/util/Iterator")))
        .then(ASTORE)
        .zeroOrMore(QueryStart.match(anyInstruction()))
        .then(aJump())
        .then(aLabelNode(loopStart.write()))
        .oneOrMore(doesNotBreakIteratorLoop())
        .then(jumpsTo(loopStart.read()))
        // can't currently deal with loops with conditionals that cause additional jumps back
        .zeroOrMore(QueryStart.match(jumpsTo(loopStart.read()).negate()));
  }

  private static SequenceQuery<AbstractInsnNode> infiniteIteratorLoopJavac() {
    final Slot<LabelNode> loopStart = Slot.create(LabelNode.class);

    return  QueryStart
        .any(AbstractInsnNode.class)
        .then(methodCallThatReturns(ClassName.fromString("java/util/Iterator")))
        .then(ASTORE)
        .then(aLabelNode(loopStart.write()))
        .oneOrMore(doesNotBreakIteratorLoop())
        .then(jumpsTo(loopStart.read()))
        // can't currently deal with loops with conditionals that cause additional jumps back
        .zeroOrMore(QueryStart.match(jumpsTo(loopStart.read()).negate()));
  }



}
