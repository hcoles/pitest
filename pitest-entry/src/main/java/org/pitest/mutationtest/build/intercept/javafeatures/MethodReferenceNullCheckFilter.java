package org.pitest.mutationtest.build.intercept.javafeatures;

import static org.pitest.bytecode.analysis.InstructionMatchers.anyInstruction;
import static org.pitest.bytecode.analysis.InstructionMatchers.isInstruction;
import static org.pitest.bytecode.analysis.InstructionMatchers.methodCallTo;
import static org.pitest.bytecode.analysis.InstructionMatchers.notAnInstruction;
import static org.pitest.bytecode.analysis.InstructionMatchers.opCode;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.bytecode.analysis.MethodMatchers;
import org.pitest.bytecode.analysis.MethodTree;
import org.pitest.classinfo.ClassName;
import org.pitest.functional.FCollection;
import org.pitest.functional.prelude.Prelude;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.sequence.Context;
import org.pitest.sequence.QueryParams;
import org.pitest.sequence.QueryStart;
import org.pitest.sequence.SequenceMatcher;
import org.pitest.sequence.Slot;

/**
 * Filters out the calls to Objects.requireNotNull the compiler inserts when using method references.
 *
 */
public class MethodReferenceNullCheckFilter implements MutationInterceptor {

  private static final boolean DEBUG = false;

  private static final Slot<AbstractInsnNode> MUTATED_INSTRUCTION = Slot.create(AbstractInsnNode.class);

  static final SequenceMatcher<AbstractInsnNode> NULL_CHECK = QueryStart
      .any(AbstractInsnNode.class)
      .then(methodCallTo(ClassName.fromClass(Objects.class), "requireNonNull").and(isInstruction(MUTATED_INSTRUCTION.read())))
      .then(opCode(Opcodes.POP))
      .then(opCode(Opcodes.INVOKEDYNAMIC))
      .zeroOrMore(QueryStart.match(anyInstruction()))
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
    return FCollection.filter(mutations, Prelude.not(isAnImplicitNullCheck()));
  }

  private Predicate<MutationDetails> isAnImplicitNullCheck() {
    return a -> {
      final int instruction = a.getInstructionIndex();
      final MethodTree method = MethodReferenceNullCheckFilter.this.currentClass.methods().stream()
          .filter(MethodMatchers.forLocation(a.getId().getLocation()))
          .findFirst()
          .get();

      final AbstractInsnNode mutatedInstruction = method.instruction(instruction);

      final Context<AbstractInsnNode> context = Context.start(method.instructions(), DEBUG);
      context.store(MUTATED_INSTRUCTION.write(), mutatedInstruction);
      return NULL_CHECK.matches(method.instructions(), context);
    };
  }

  @Override
  public void end() {
    this.currentClass = null;
  }

}
