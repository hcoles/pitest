package org.pitest.mutationtest.build.intercept.javafeatures;

import static org.pitest.bytecode.analysis.InstructionMatchers.anyInstruction;
import static org.pitest.bytecode.analysis.InstructionMatchers.isA;
import static org.pitest.bytecode.analysis.InstructionMatchers.isInstruction;
import static org.pitest.bytecode.analysis.InstructionMatchers.methodCallTo;
import static org.pitest.bytecode.analysis.InstructionMatchers.opCode;

import java.util.Collection;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.bytecode.analysis.MethodMatchers;
import org.pitest.bytecode.analysis.MethodTree;
import org.pitest.classinfo.ClassName;
import org.pitest.functional.F;
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
import org.pitest.sequence.Slot;

public class ImplicitNullCheckFilter implements MutationInterceptor {
  
  private static final boolean DEBUG = false;
  
  private static final Match<AbstractInsnNode> IGNORE = isA(LineNumberNode.class).or(isA(FrameNode.class));

  private static final Slot<AbstractInsnNode> MUTATED_INSTRUCTION = Slot.create(AbstractInsnNode.class);
  
  static final SequenceMatcher<AbstractInsnNode> GET_CLASS_NULL_CHECK = QueryStart
      .any(AbstractInsnNode.class)
      .then(methodCallTo(ClassName.fromClass(Object.class), "getClass").and(isInstruction(MUTATED_INSTRUCTION.read())))
      .then(opCode(Opcodes.POP)) // immediate discard
      .zeroOrMore(QueryStart.match(anyInstruction()))
      .compile(QueryParams.params(AbstractInsnNode.class)
          .withIgnores(IGNORE)
          .withDebug(DEBUG)
          );
  
  
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
    return FCollection.filter(mutations, Prelude.not(isAnImplicitNullCheck()));
  }

  private F<MutationDetails, Boolean> isAnImplicitNullCheck() {
    return new F<MutationDetails, Boolean>() {
      @Override
      public Boolean apply(MutationDetails a) {
        int instruction = a.getInstructionIndex();
        MethodTree method = currentClass.methods().findFirst(MethodMatchers.forLocation(a.getId().getLocation())).value();
        if (!method.isSynthetic()) {
          return false;
        }
        
        AbstractInsnNode mutatedInstruction = method.instructions().get(instruction);

        Context<AbstractInsnNode> context = Context.start(method.instructions(), DEBUG);
        context.store(MUTATED_INSTRUCTION.write(), mutatedInstruction);
        return GET_CLASS_NULL_CHECK.matches(method.instructions(), context); 
      } 
    };
  }
  
  @Override
  public void end() {
    currentClass = null; 
  }

}
