package org.pitest.mutationtest.build.intercept.equivalent;

import static org.pitest.bytecode.analysis.InstructionMatchers.anyInstruction;
import static org.pitest.bytecode.analysis.InstructionMatchers.debug;
import static org.pitest.bytecode.analysis.InstructionMatchers.isA;
import static org.pitest.bytecode.analysis.InstructionMatchers.opCode;

import java.util.Collection;
import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.bytecode.analysis.InstructionMatchers;
import org.pitest.bytecode.analysis.MethodMatchers;
import org.pitest.bytecode.analysis.MethodTree;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.FunctionalList;
import org.pitest.functional.Option;
import org.pitest.functional.prelude.Prelude;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.engine.Location;
import org.pitest.mutationtest.engine.MethodName;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.sequence.Match;
import org.pitest.sequence.QueryParams;
import org.pitest.sequence.QueryStart;
import org.pitest.sequence.SequenceMatcher;

public class EqualsPerformanceShortcutFilter implements MutationInterceptor {
  
  private static final boolean DEBUG = false;
  
  private static final Match<AbstractInsnNode> IGNORE = isA(LineNumberNode.class).or(isA(FrameNode.class));
  
  static final SequenceMatcher<AbstractInsnNode> ALWAYS_FALSE = QueryStart
      .any(AbstractInsnNode.class)
      .then(opCode(Opcodes.GOTO).and(debug("goto")))
      .then(isA(LabelNode.class).and(debug("label")))
      .then(opCode(Opcodes.ICONST_1).and(debug("const")))
      .then(opCode(Opcodes.IRETURN).and(debug("return")))
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
   FunctionalList<MutationDetails> doNotTouch = FCollection.filter(mutations, Prelude.not(inEqualsMethod()));
   if (doNotTouch.size() != mutations.size()) {
     FunctionalList<MutationDetails> inEquals = FCollection.filter(mutations, inEqualsMethod());
     List<MutationDetails> filtered = filter(inEquals, m);
     doNotTouch.addAll(filtered);
   }
   return doNotTouch;
  }

  private List<MutationDetails> filter(
      FunctionalList<MutationDetails> inEquals, Mutater m) {
    Location equalsMethod = inEquals.get(0).getId().getLocation();
    
    Option<MethodTree> maybeEquals = currentClass.methods()
        .findFirst(MethodMatchers.forLocation(equalsMethod));
    
    return inEquals.filter(Prelude.not(isShortcutEquals(maybeEquals.value(), m)));
  }

  private F<MutationDetails, Boolean> isShortcutEquals(final MethodTree tree, final Mutater m) {
    return new F<MutationDetails, Boolean>() {
      @Override
      public Boolean apply(MutationDetails a) {
        return shortCutEquals(tree,a, m);
      }
    };
  }

  private Boolean shortCutEquals(MethodTree tree, MutationDetails a, Mutater m) {
    AbstractInsnNode mutatedInsns = tree.instructions().get(a.getInstructionIndex());
    if (!InstructionMatchers.aConditionalJump().test(null, mutatedInsns)) {
      return false;
    }
    
    ClassTree mutant = ClassTree.fromBytes(m.getMutation(a.getId()).getBytes());
    MethodTree mutantEquals = mutant.methods().findFirst(MethodMatchers.forLocation(tree.asLocation())).value();
    
    return ALWAYS_FALSE.matches(mutantEquals.instructions());
  }
  private F<MutationDetails, Boolean> inEqualsMethod() {
    return new  F<MutationDetails, Boolean>() {
      @Override
      public Boolean apply(MutationDetails a) {
        Location loc = a.getId().getLocation();
        return loc.getMethodDesc().equals("(Ljava/lang/Object;)Z")
            && loc.getMethodName().equals(MethodName.fromString("equals"));
      }  
    };
  }

  @Override
  public void end() {
    
  }

}
