package org.pitest.mutationtest.build.intercept.timeout;

import static org.pitest.bytecode.analysis.InstructionMatchers.aConditionalJump;
import static org.pitest.bytecode.analysis.InstructionMatchers.aConditionalJumpTo;
import static org.pitest.bytecode.analysis.InstructionMatchers.aLabelNode;
import static org.pitest.bytecode.analysis.InstructionMatchers.anILoadOf;
import static org.pitest.bytecode.analysis.InstructionMatchers.anIStore;
import static org.pitest.bytecode.analysis.InstructionMatchers.anyInstruction;
import static org.pitest.bytecode.analysis.InstructionMatchers.debug;
import static org.pitest.bytecode.analysis.InstructionMatchers.gotoLabel;
import static org.pitest.bytecode.analysis.InstructionMatchers.incrementsVariable;
import static org.pitest.bytecode.analysis.InstructionMatchers.isA;
import static org.pitest.bytecode.analysis.InstructionMatchers.isInstruction;
import static org.pitest.bytecode.analysis.InstructionMatchers.jumpsTo;
import static org.pitest.bytecode.analysis.InstructionMatchers.labelNode;
import static org.pitest.bytecode.analysis.InstructionMatchers.opCode;

import java.util.Collection;
import java.util.function.Predicate;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.bytecode.analysis.MethodMatchers;
import org.pitest.bytecode.analysis.MethodTree;
import org.pitest.functional.FCollection;
import org.pitest.functional.prelude.Prelude;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.sequence.Context;
import org.pitest.sequence.Match;
import org.pitest.sequence.QueryParams;
import org.pitest.sequence.QueryStart;
import org.pitest.sequence.SequenceMatcher;
import org.pitest.sequence.SequenceQuery;
import org.pitest.sequence.Slot;

/**
 * Removes mutants that affect for loop counters as these have
 * a high chance of timing out.
 */
public class AvoidForLoopCounterFilter implements MutationInterceptor {

  private static final boolean DEBUG = false;

  // GETFIELDS are ignored so field access can be matched in the same way as  local variables
  private static final Match<AbstractInsnNode> IGNORE = isA(LineNumberNode.class)
                                                        .or(isA(FrameNode.class)
                                                        .or(opCode(Opcodes.GETFIELD))
                                                        );

  private static final Slot<AbstractInsnNode> MUTATED_INSTRUCTION = Slot.create(AbstractInsnNode.class);

  static final SequenceMatcher<AbstractInsnNode> MUTATED_FOR_COUNTER = QueryStart
      .match(Match.<AbstractInsnNode>never())
      .or(conditionalAtEnd())
      .or(conditionalAtStart())
      .compile(QueryParams.params(AbstractInsnNode.class)
        .withIgnores(IGNORE)
        .withDebug(DEBUG)
        );


  private ClassTree currentClass;


  private static SequenceQuery<AbstractInsnNode> conditionalAtEnd() {
    final Slot<Integer> counterVariable = Slot.create(Integer.class);
    final Slot<LabelNode> loopStart = Slot.create(LabelNode.class);
    final Slot<LabelNode> loopEnd = Slot.create(LabelNode.class);
    return QueryStart
        .any(AbstractInsnNode.class)
   //     .then(anIntegerConstant()) // skip this?
        .then(anIStore(counterVariable.write()).and(debug("end_counter")))
        .then(isA(LabelNode.class))
        .then(gotoLabel(loopEnd.write()))
        .then(aLabelNode(loopStart.write()).and(debug("loop start")))
        .zeroOrMore(anything())
        .then(targetInstruction(counterVariable).and(debug("target")))
        .then(labelNode(loopEnd.read()).and(debug("loop end")))
        .then(anILoadOf(counterVariable.read()).and(debug("read")))
        .zeroOrMore(anything())
        .then(loadsAnIntegerToCompareTo())
        .then(aConditionalJumpTo(loopStart).and(debug("jump")))
        .zeroOrMore(anything());
  }


  private static SequenceQuery<AbstractInsnNode> conditionalAtStart() {
    final Slot<Integer> counterVariable = Slot.create(Integer.class);
    final Slot<LabelNode> loopStart = Slot.create(LabelNode.class);
    final Slot<LabelNode> loopEnd = Slot.create(LabelNode.class);
    return QueryStart
        .any(AbstractInsnNode.class)
       // .then(anIntegerConstant().and(debug("constant"))) // skip this?
        .then(anIStore(counterVariable.write()).and(debug("store")))
        .then(aLabelNode(loopStart.write()).and(debug("label")))
        .then(anILoadOf(counterVariable.read()).and(debug("load")))
        .zeroOrMore(QueryStart.match(opCode(Opcodes.ALOAD))) // optionally put object on stack
        .then(loadsAnIntegerToCompareTo().and(debug("push")))
        .then(jumpsTo(loopEnd.write()).and(aConditionalJump()))
        .then(isA(LabelNode.class))
        .zeroOrMore(anything())
        .then(targetInstruction(counterVariable).and(debug("target")))
        .then(jumpsTo(loopStart.read()).and(debug("jump")))
        .then(labelNode(loopEnd.read()))
        .zeroOrMore(anything());
  }

  private static Match<AbstractInsnNode> loadsAnIntegerToCompareTo() {
    return opCode(Opcodes.BIPUSH).or(integerMethodCall()).or(arrayLength());
  }


  private static Match<AbstractInsnNode> arrayLength() {
    return opCode(Opcodes.ARRAYLENGTH);
  }


  private static SequenceQuery<AbstractInsnNode> anything() {
    return QueryStart.match(anyInstruction());
  }

  private static Match<AbstractInsnNode> integerMethodCall() {
    return isA(MethodInsnNode.class);
  }

  private static Match<AbstractInsnNode> targetInstruction(Slot<Integer> counterVariable) {
    return incrementsVariable(counterVariable.read()).and(isInstruction(MUTATED_INSTRUCTION.read()));
  }

  @Override
  public InterceptorType type() {
    return InterceptorType.FILTER;
  }

  @Override
  public void begin(ClassTree clazz) {
    this.currentClass = clazz;
  }

  @Override
  public Collection<MutationDetails> intercept(
      Collection<MutationDetails> mutations, Mutater m) {
    return FCollection.filter(mutations, Prelude.not(mutatesAForLoopCounter()));
  }

  private Predicate<MutationDetails> mutatesAForLoopCounter() {
    return a -> {
      final int instruction = a.getInstructionIndex();
      final MethodTree method = AvoidForLoopCounterFilter.this.currentClass.methods().stream()
          .filter(MethodMatchers.forLocation(a.getId().getLocation()))
          .findFirst().get();
      final AbstractInsnNode mutatedInstruction = method.instructions().get(instruction);

      final Context<AbstractInsnNode> context = Context.start(method.instructions(), DEBUG);
      context.store(MUTATED_INSTRUCTION.write(), mutatedInstruction);
      return MUTATED_FOR_COUNTER.matches(method.instructions(), context);
    };
  }

  @Override
  public void end() {
    this.currentClass = null;
  }

}
