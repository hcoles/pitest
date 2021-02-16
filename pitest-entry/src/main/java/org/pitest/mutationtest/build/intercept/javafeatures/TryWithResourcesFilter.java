package org.pitest.mutationtest.build.intercept.javafeatures;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.pitest.bytecode.analysis.ClassTree;
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
import org.pitest.sequence.SequenceMatcher;
import org.pitest.sequence.SequenceQuery;
import org.pitest.sequence.Slot;

import java.util.Collection;
import java.util.function.Predicate;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.IFNONNULL;
import static org.objectweb.asm.Opcodes.IFNULL;
import static org.objectweb.asm.Opcodes.IF_ACMPEQ;
import static org.pitest.bytecode.analysis.InstructionMatchers.anyInstruction;
import static org.pitest.bytecode.analysis.InstructionMatchers.isA;
import static org.pitest.bytecode.analysis.InstructionMatchers.methodCallNamed;
import static org.pitest.bytecode.analysis.InstructionMatchers.notAnInstruction;
import static org.pitest.bytecode.analysis.InstructionMatchers.opCode;
import static org.pitest.bytecode.analysis.InstructionMatchers.recordTarget;
import static org.pitest.sequence.QueryStart.any;
import static org.pitest.sequence.QueryStart.match;

public class TryWithResourcesFilter implements MutationInterceptor {

  private static final boolean DEBUG = false;

  private static final Slot<AbstractInsnNode> MUTATED_INSTRUCTION = Slot.create(AbstractInsnNode.class);
  private static final Slot<Boolean> FOUND = Slot.create(Boolean.class);

  private static SequenceQuery<AbstractInsnNode> javac11() {
    return any(AbstractInsnNode.class)
            .zeroOrMore(match(anyInstruction()))
            .then(aLabel())
            .then(anALoad())
            .then(closeCall())
            .then(aLabel())
            .then(aGoto())
            .then(aLabel())
            .then(anAStore())
            .then(anALoad())
            .then(anALoad())
            .then(addSuppressedCall())
            .zeroOrMore(match(anyInstruction()));
  }

  private static SequenceQuery<AbstractInsnNode> javac8() {
    return any(AbstractInsnNode.class)
            .zeroOrMore(match(anyInstruction()))
            .then(ifNull())
            .then(anALoad())
            .then(ifNull())
            .then(aLabel())
            .then(anALoad())
            .then(closeCall())
            .then(aLabel())
            .then(aGoto())
            .then(aLabel())
            .then(anAStore())
            .then(aLabel())
            .then(anALoad())
            .then(anALoad())
            .then(addSuppressedCall())
            .then(aLabel())
            .then(aGoto())
            .then(aLabel())
            .then(anALoad())
            .then(closeCall())
            .zeroOrMore(match(anyInstruction()));
  }

  private static SequenceQuery<AbstractInsnNode> ecj() {
    return any(AbstractInsnNode.class)
            .zeroOrMore(match(anyInstruction()))
            .then(closeCall())
            .then(aLabel())
            .then(anALoad())
            .then(opCode(ATHROW).and(mutationPoint()))
            .then(aLabel())
            .then(anAStore())
            .then(anALoad())
            .then(ifNonNull())
            .then(anALoad())
            .then(anAStore())
            .then(aGoto())
            .then(aLabel())
            .zeroOrMore(match(anyInstruction()));
  }

  private static SequenceQuery<AbstractInsnNode> ecjAddSuppressedCheck() {
    return any(AbstractInsnNode.class)
            .zeroOrMore(match(anyInstruction()))
            .then(ifNonNull())
            .then(anALoad())
            .then(anAStore())
            .then(aGoto())
            .then(aLabel())
            .then(anALoad())
            .then(anALoad())
            .then(opCode(IF_ACMPEQ).and(mutationPoint()))
            .then(anALoad())
            .then(anALoad())
            .then(addSuppressedCall())
            .then(aLabel())
            .zeroOrMore(match(anyInstruction()));
  }


  private static final SequenceMatcher<AbstractInsnNode> TRY_WITH_RESOURCES = match(Match.<AbstractInsnNode>never())
          .or(javac11())
          .or(javac8())
          .or(ecj())
          .or(ecjAddSuppressedCheck())
          .then(containMutation(FOUND))
          .compile(QueryParams.params(AbstractInsnNode.class)
                  .withIgnores(notAnInstruction())
                  .withDebug(DEBUG)
          );

  private ClassTree currentClass;

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
    return FCollection.filter(mutations, Prelude.not(mutatesTryWithResourcesScaffolding()));
  }

  private Predicate<MutationDetails> mutatesTryWithResourcesScaffolding() {
    return a -> {
      int instruction = a.getInstructionIndex();
      MethodTree method = currentClass.method(a.getId().getLocation())
              .orElseThrow(() -> new IllegalStateException("Could not find method for mutant " + a));

      // performance hack
      if (method.rawNode().tryCatchBlocks.size() <= 1) {
        return false;
      }

      AbstractInsnNode mutatedInstruction = method.instruction(instruction);

      Context<AbstractInsnNode> context = Context.start(method.instructions(), DEBUG);
      context.store(MUTATED_INSTRUCTION.write(), mutatedInstruction);
      return TRY_WITH_RESOURCES.matches(method.instructions(), context);
    };
  }

  @Override
  public void end() {
    this.currentClass = null;
  }

  private static Match<AbstractInsnNode> aLabel() {
    return isA(LabelNode.class);
  }
  private static Match<AbstractInsnNode> anALoad() {
    return opCode(ALOAD).and(mutationPoint());
  }

  private static Match<AbstractInsnNode> aGoto() {
    return opCode(GOTO).and(mutationPoint());
  }

  private static Match<AbstractInsnNode> addSuppressedCall() {
    return methodCallNamed("addSuppressed").and(mutationPoint());
  }

  private static Match<AbstractInsnNode> anAStore() {
    return opCode(ASTORE).and(mutationPoint());
  }

  private static Match<AbstractInsnNode> closeCall() {
    return methodCallNamed("close").and(mutationPoint());
  }

  private static Match<AbstractInsnNode> ifNonNull() {
    return opCode(IFNONNULL).and(mutationPoint());
  }

  private static Match<AbstractInsnNode> ifNull() {
    return opCode(IFNULL).and(mutationPoint());
  }

  private static Match<AbstractInsnNode> mutationPoint() {
    return recordTarget(MUTATED_INSTRUCTION.read(), FOUND.write());
  }

  private static Match<AbstractInsnNode> containMutation(final Slot<Boolean> found) {
    return (c, t) -> c.retrieve(found.read()).isPresent();
  }

}
