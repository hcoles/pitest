package org.pitest.mutationtest.build.intercept.javafeatures;

import static org.pitest.bytecode.analysis.InstructionMatchers.aConditionalJump;
import static org.pitest.bytecode.analysis.InstructionMatchers.aConditionalJumpTo;
import static org.pitest.bytecode.analysis.InstructionMatchers.aLabelNode;
import static org.pitest.bytecode.analysis.InstructionMatchers.anIStore;
import static org.pitest.bytecode.analysis.InstructionMatchers.anyInstruction;
import static org.pitest.bytecode.analysis.InstructionMatchers.debug;
import static org.pitest.bytecode.analysis.InstructionMatchers.gotoLabel;
import static org.pitest.bytecode.analysis.InstructionMatchers.incrementsVariable;
import static org.pitest.bytecode.analysis.InstructionMatchers.jumpsTo;
import static org.pitest.bytecode.analysis.InstructionMatchers.labelNode;
import static org.pitest.bytecode.analysis.InstructionMatchers.methodCallThatReturns;
import static org.pitest.bytecode.analysis.InstructionMatchers.methodCallTo;
import static org.pitest.bytecode.analysis.InstructionMatchers.notAnInstruction;
import static org.pitest.bytecode.analysis.InstructionMatchers.opCode;
import static org.pitest.bytecode.analysis.InstructionMatchers.recordTarget;
import static org.pitest.sequence.Result.result;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.bytecode.analysis.MethodTree;
import org.pitest.classinfo.ClassName;
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

public class ForEachLoopFilter implements MutationInterceptor {

  private static final boolean DEBUG = false;

  private static final Slot<AbstractInsnNode> MUTATED_INSTRUCTION = Slot.create(AbstractInsnNode.class);
  private static final Slot<Boolean> FOUND = Slot.create(Boolean.class);


  private static final SequenceMatcher<AbstractInsnNode> ITERATOR_LOOP =
       conditionalAtStart()
      .or(conditionalAtEnd())
      .or(arrayConditionalAtEnd())
      .or(arrayConditionalAtStart())
      .then(containMutation(FOUND))
      .compile(QueryParams.params(AbstractInsnNode.class)
        .withIgnores(notAnInstruction())
        .withDebug(DEBUG)
        );

  private ClassTree currentClass;


  private static SequenceQuery<AbstractInsnNode> conditionalAtEnd() {
    final Slot<LabelNode> loopStart = Slot.create(LabelNode.class);
    final Slot<LabelNode> loopEnd = Slot.create(LabelNode.class);
    return QueryStart
        .any(AbstractInsnNode.class)
        .zeroOrMore(QueryStart.match(anyInstruction()))
        .then(aMethodCallReturningAnIterator().and(mutationPoint()))
        .then(opCode(Opcodes.ASTORE))
        .then(gotoLabel(loopEnd.write()))
        .then(aLabelNode(loopStart.write()))
        .then(opCode(Opcodes.ALOAD))
        .then(methodCallTo(ClassName.fromString("java/util/Iterator"), "next").and(mutationPoint()))
        .zeroOrMore(QueryStart.match(anyInstruction()))
        .then(labelNode(loopEnd.read()))
        .then(opCode(Opcodes.ALOAD))
        .then(hasNextMethodCall().and(mutationPoint()))
        .then(aConditionalJumpTo(loopStart).and(mutationPoint()))
        .zeroOrMore(QueryStart.match(anyInstruction()));
  }


  private static SequenceQuery<AbstractInsnNode> conditionalAtStart() {
    final Slot<LabelNode> loopStart = Slot.create(LabelNode.class);
    final Slot<LabelNode> loopEnd = Slot.create(LabelNode.class);
    return QueryStart
        .any(AbstractInsnNode.class)
        .zeroOrMore(QueryStart.match(anyInstruction()))
        .then(aMethodCallReturningAnIterator().and(mutationPoint()))
        .then(opCode(Opcodes.ASTORE))
        .then(aLabelNode(loopStart.write()))
        .then(opCode(Opcodes.ALOAD))
        .then(hasNextMethodCall().and(mutationPoint()))
        .then(aConditionalJump().and(jumpsTo(loopEnd.write())).and(mutationPoint()))
        .then(opCode(Opcodes.ALOAD))
        .then(methodCallTo(ClassName.fromString("java/util/Iterator"), "next").and(mutationPoint()))
        .zeroOrMore(QueryStart.match(anyInstruction()))
        .then(opCode(Opcodes.GOTO).and(jumpsTo(loopStart.read())))
        .then(labelNode(loopEnd.read()))
        .zeroOrMore(QueryStart.match(anyInstruction()));
  }

  private static SequenceQuery<AbstractInsnNode> arrayConditionalAtEnd() {
    final Slot<LabelNode> loopStart = Slot.create(LabelNode.class);
    final Slot<LabelNode> loopEnd = Slot.create(LabelNode.class);
    final Slot<Integer> counter = Slot.create(Integer.class);
    return QueryStart
        .any(AbstractInsnNode.class)
        .zeroOrMore(QueryStart.match(anyInstruction()))
        .then(opCode(Opcodes.ARRAYLENGTH).and(mutationPoint()))
        .then(opCode(Opcodes.ISTORE))
        .then(opCode(Opcodes.ICONST_0).and(mutationPoint()))
        .then(anIStore(counter.write()).and(debug("store")))
        .then(gotoLabel(loopEnd.write()))
        .then(aLabelNode(loopStart.write()))
        .zeroOrMore(QueryStart.match(anyInstruction()))
        .then(incrementsVariable(counter.read()).and(mutationPoint()))
        .then(labelNode(loopEnd.read()))
        .then(opCode(Opcodes.ILOAD))
        .then(opCode(Opcodes.ILOAD))
        .then(aConditionalJumpTo(loopStart).and(mutationPoint()))
        .zeroOrMore(QueryStart.match(anyInstruction()));
  }

  private static SequenceQuery<AbstractInsnNode> arrayConditionalAtStart() {
    final Slot<LabelNode> loopStart = Slot.create(LabelNode.class);
    final Slot<LabelNode> loopEnd = Slot.create(LabelNode.class);
    final Slot<Integer> counter = Slot.create(Integer.class);
    return QueryStart
        .any(AbstractInsnNode.class)
        .zeroOrMore(QueryStart.match(anyInstruction()))
        .then(opCode(Opcodes.ARRAYLENGTH).and(mutationPoint()))
        .then(opCode(Opcodes.ISTORE))
        .then(opCode(Opcodes.ICONST_0).and(mutationPoint()))
        .then(anIStore(counter.write()).and(debug("store")))
        .then(aLabelNode(loopStart.write()))
        .then(opCode(Opcodes.ILOAD))
        .then(opCode(Opcodes.ILOAD))
        .then(aConditionalJump().and(jumpsTo(loopEnd.write())).and(mutationPoint()))
        .zeroOrMore(QueryStart.match(anyInstruction()))
        .then(incrementsVariable(counter.read()).and(mutationPoint()))
        .then(opCode(Opcodes.GOTO).and(jumpsTo(loopStart.read())))
        .zeroOrMore(QueryStart.match(anyInstruction()));
  }

  private static Match<AbstractInsnNode> hasNextMethodCall() {
    return methodCallTo(ClassName.fromString("java/util/Iterator"), "hasNext");
  }

  private static Match<AbstractInsnNode> aMethodCallReturningAnIterator() {
    return methodCallThatReturns(ClassName.fromClass(Iterator.class));
  }

  private static Match<AbstractInsnNode> mutationPoint() {
    return recordTarget(MUTATED_INSTRUCTION.read(), FOUND.write()).and(debug("Mutation point"));
  }


  private static Match<AbstractInsnNode> containMutation(final Slot<Boolean> found) {
   return (c, t) -> result(c.retrieve(found.read()).isPresent(), c);
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
    return mutations.stream().filter(mutatesIteratorLoopPlumbing().negate())
            .collect(Collectors.toList());
  }

  private Predicate<MutationDetails> mutatesIteratorLoopPlumbing() {
    return a -> {
      final int instruction = a.getInstructionIndex();
      final MethodTree method = currentClass.method(a.getId().getLocation()).get();

      //performance hack
      if (!mightContainForLoop(method.instructions())) {
        return false;
      }

      final AbstractInsnNode mutatedInstruction = method.instruction(instruction);

      Context context = Context.start(DEBUG);
      context = context.store(MUTATED_INSTRUCTION.write(), mutatedInstruction);
      return ITERATOR_LOOP.matches(method.instructions(), context);
    };
  }

  private boolean mightContainForLoop(List<AbstractInsnNode> instructions) {
    return instructions.stream()
            .anyMatch(i -> hasNextMethodCall().or(opCode(Opcodes.ARRAYLENGTH)).test(null, i).result());
  }

  @Override
  public void end() {
    this.currentClass = null;
  }
}
