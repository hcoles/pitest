package org.pitest.mutationtest.build.intercept.equivalent;

import static org.pitest.bytecode.analysis.InstructionMatchers.anyInstruction;
import static org.pitest.bytecode.analysis.InstructionMatchers.debug;
import static org.pitest.bytecode.analysis.InstructionMatchers.notAnInstruction;
import static org.pitest.bytecode.analysis.InstructionMatchers.opCode;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.bytecode.analysis.InstructionMatchers;
import org.pitest.bytecode.analysis.MethodMatchers;
import org.pitest.bytecode.analysis.MethodTree;
import org.pitest.functional.FCollection;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.engine.Location;
import org.pitest.mutationtest.engine.MethodName;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.sequence.QueryParams;
import org.pitest.sequence.QueryStart;
import org.pitest.sequence.SequenceMatcher;

public class EqualsPerformanceShortcutFilter implements MutationInterceptor {

  private static final boolean DEBUG = false;

  // Looks fairly specifically for a conditional mutated to a unconditional
  // rather than any always false condition
  static final SequenceMatcher<AbstractInsnNode> ALWAYS_FALSE = QueryStart
      .any(AbstractInsnNode.class)
      .then(opCode(Opcodes.ALOAD))
      .then(opCode(Opcodes.ALOAD))
      .then(opCode(Opcodes.POP2))
      .then(opCode(Opcodes.GOTO).and(debug("goto")))
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
   final List<MutationDetails> doNotTouch = FCollection.filter(mutations, inEqualsMethod().negate());
   if (doNotTouch.size() != mutations.size()) {
     final List<MutationDetails> inEquals = FCollection.filter(mutations, inEqualsMethod());
     final List<MutationDetails> filtered = filter(inEquals, m);
     doNotTouch.addAll(filtered);
   }
   return doNotTouch;
  }

  private List<MutationDetails> filter(
      List<MutationDetails> inEquals, Mutater m) {
    final Location equalsMethod = inEquals.get(0).getId().getLocation();

    final Optional<MethodTree> maybeEquals = this.currentClass.methods().stream()
        .filter(MethodMatchers.forLocation(equalsMethod))
        .findFirst();

    return inEquals.stream()
        .filter(isShortcutEquals(maybeEquals.get(), m).negate())
        .collect(Collectors.toList());
  }

  private Predicate<MutationDetails> isShortcutEquals(final MethodTree tree, final Mutater m) {
    return a -> shortCutEquals(tree,a, m);
  }

  private Boolean shortCutEquals(MethodTree tree, MutationDetails a, Mutater m) {
    if (!mutatesAConditionalJump(tree, a.getInstructionIndex())) {
      return false;
    }

    final ClassTree mutant = ClassTree.fromBytes(m.getMutation(a.getId()).getBytes());
    final MethodTree mutantEquals = mutant.methods().stream()
        .filter(MethodMatchers.forLocation(tree.asLocation()))
        .findFirst()
        .get();

    return ALWAYS_FALSE.matches(mutantEquals.instructions());
  }

  private boolean mutatesAConditionalJump(MethodTree tree, int index) {
    final AbstractInsnNode mutatedInsns = tree.instruction(index);
    return InstructionMatchers.aConditionalJump().test(null, mutatedInsns);
  }

  private Predicate<MutationDetails> inEqualsMethod() {
    return a -> {
      final Location loc = a.getId().getLocation();
      return loc.getMethodDesc().equals("(Ljava/lang/Object;)Z")
          && loc.getMethodName().equals(MethodName.fromString("equals"));
    };
  }

  @Override
  public void end() {

  }

}
