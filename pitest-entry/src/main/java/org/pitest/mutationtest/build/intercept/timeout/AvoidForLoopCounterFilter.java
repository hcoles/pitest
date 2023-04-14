package org.pitest.mutationtest.build.intercept.timeout;

import static org.pitest.bytecode.analysis.InstructionMatchers.aConditionalJump;
import static org.pitest.bytecode.analysis.InstructionMatchers.aConditionalJumpTo;
import static org.pitest.bytecode.analysis.InstructionMatchers.aLabelNode;
import static org.pitest.bytecode.analysis.InstructionMatchers.anILoad;
import static org.pitest.bytecode.analysis.InstructionMatchers.anILoadOf;
import static org.pitest.bytecode.analysis.InstructionMatchers.anIStore;
import static org.pitest.bytecode.analysis.InstructionMatchers.anIntegerConstant;
import static org.pitest.bytecode.analysis.InstructionMatchers.anyInstruction;
import static org.pitest.bytecode.analysis.InstructionMatchers.debug;
import static org.pitest.bytecode.analysis.InstructionMatchers.gotoLabel;
import static org.pitest.bytecode.analysis.InstructionMatchers.incrementsVariable;
import static org.pitest.bytecode.analysis.InstructionMatchers.isA;
import static org.pitest.bytecode.analysis.InstructionMatchers.jumpsTo;
import static org.pitest.bytecode.analysis.InstructionMatchers.labelNode;
import static org.pitest.bytecode.analysis.InstructionMatchers.notAnInstruction;
import static org.pitest.bytecode.analysis.OpcodeMatchers.ARRAYLENGTH;
import static org.pitest.bytecode.analysis.OpcodeMatchers.BIPUSH;
import static org.pitest.bytecode.analysis.OpcodeMatchers.GETFIELD;
import static org.pitest.sequence.Result.result;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.bytecode.analysis.MethodTree;
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
import org.pitest.sequence.SlotWrite;

/**
 * Removes mutants that affect for loop counters as these have
 * a high chance of timing out.
 */
public class AvoidForLoopCounterFilter implements MutationInterceptor {

  private static final boolean DEBUG = false;

  // GETFIELDS are ignored so field access can be matched in the same way as  local variables
  private static final Match<AbstractInsnNode> IGNORE = notAnInstruction()
                                                        .or(GETFIELD);

  private static final Slot<AbstractInsnNode> MUTATED_INSTRUCTION = Slot.create(AbstractInsnNode.class);

  static final SequenceMatcher<AbstractInsnNode> MUTATED_FOR_COUNTER = conditionalAtEnd()
      .or(conditionalAtStart())
      .compile(QueryParams.params(AbstractInsnNode.class)
        .withIgnores(IGNORE)
        .withDebug(DEBUG)
        );


  private ClassTree currentClass;
  private Map<MethodTree, Set<AbstractInsnNode>> cache;


  private static SequenceQuery<AbstractInsnNode> conditionalAtEnd() {
    final Slot<Integer> counterVariable = Slot.create(Integer.class);
    final Slot<LabelNode> loopStart = Slot.create(LabelNode.class);
    final Slot<LabelNode> loopEnd = Slot.create(LabelNode.class);
    return QueryStart
        .any(AbstractInsnNode.class)
        .then(anIStore(counterVariable.write()).and(debug("end_counter")))
        .then(isA(LabelNode.class).and(debug("label 1")))
        .then(gotoLabel(loopEnd.write()).and(debug("goto")))
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
            .then(aLabelNode(loopStart.write()).and(debug("conditional at start label")))
            .then(anILoad(counterVariable.write()).and(debug("iload")))
            .zeroOrMore(anything())
            .then(jumpsTo(loopEnd.write()).and(aConditionalJump()).and(debug("jump")))
            .then(isA(LabelNode.class))
            .zeroOrMore(anything())
            .then(targetInstruction(counterVariable).and(debug("target")))
            .then(jumpsTo(loopStart.read()).and(debug("jump")))
            .then(labelNode(loopEnd.read()))
            .zeroOrMore(anything());
  }

  private static Match<AbstractInsnNode> loadsAnIntegerToCompareTo() {
    return BIPUSH
            .or(integerMethodCall())
            .or(ARRAYLENGTH)
            .or(anIntegerConstant());
  }


  private static SequenceQuery<AbstractInsnNode> anything() {
    return QueryStart.match(anyInstruction());
  }

  private static Match<AbstractInsnNode> integerMethodCall() {
    return isA(MethodInsnNode.class);
  }

  private static Match<AbstractInsnNode> targetInstruction(Slot<Integer> counterVariable) {
    return incrementsVariable(counterVariable.read())
            .and(recordInstruction(MUTATED_INSTRUCTION.write()));
  }

  private static Match<AbstractInsnNode> recordInstruction(SlotWrite<AbstractInsnNode> slot) {
    return (c,t) -> result(true, c.store(slot, t));
  }

  @Override
  public InterceptorType type() {
    return InterceptorType.FILTER;
  }

  @Override
  public void begin(ClassTree clazz) {
    this.currentClass = clazz;
    this.cache = new IdentityHashMap<>();
  }

  @Override
  public Collection<MutationDetails> intercept(
      Collection<MutationDetails> mutations, Mutater m) {
    return mutations.stream()
            .filter(mutatesAForLoopCounter().negate())
            .collect(Collectors.toList());
  }

  private Predicate<MutationDetails> mutatesAForLoopCounter() {
    return a -> {
      final int instruction = a.getInstructionIndex();
      Optional<MethodTree> maybeMethod = AvoidForLoopCounterFilter.this.currentClass.method(a.getId().getLocation());
      if (!maybeMethod.isPresent()) {
        return false;
      }
      MethodTree method = maybeMethod.get();

      final AbstractInsnNode mutatedInstruction = method.instruction(instruction);

      // performance hack
      if (!(mutatedInstruction instanceof IincInsnNode)) {
        return false;
      }

      Set<AbstractInsnNode> loopIncrements = cache.computeIfAbsent(method, this::findLoopCounters);

      return loopIncrements.contains(mutatedInstruction);
    };
  }

  private Set<AbstractInsnNode> findLoopCounters(MethodTree method) {
    Context context = Context.start(DEBUG);
    return MUTATED_FOR_COUNTER.contextMatches(method.instructions(), context).stream()
            .map(c -> c.retrieve(MUTATED_INSTRUCTION.read()))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toSet());
  }

  @Override
  public void end() {
    this.currentClass = null;
    this.cache = null;
  }

}
