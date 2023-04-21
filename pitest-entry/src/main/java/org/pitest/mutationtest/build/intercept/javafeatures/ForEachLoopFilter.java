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
import static org.pitest.bytecode.analysis.OpcodeMatchers.ALOAD;
import static org.pitest.bytecode.analysis.OpcodeMatchers.ARRAYLENGTH;
import static org.pitest.bytecode.analysis.OpcodeMatchers.ASTORE;
import static org.pitest.bytecode.analysis.OpcodeMatchers.GOTO;
import static org.pitest.bytecode.analysis.OpcodeMatchers.ICONST_0;
import static org.pitest.bytecode.analysis.OpcodeMatchers.ILOAD;
import static org.pitest.bytecode.analysis.OpcodeMatchers.ISTORE;
import static org.pitest.sequence.Result.result;

import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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

  private static final Slot<List<AbstractInsnNode>> LOOP_INSTRUCTIONS = Slot.createList(AbstractInsnNode.class);

  private static final SequenceMatcher<AbstractInsnNode> ITERATOR_LOOP =
       conditionalAtStart()
      .or(conditionalAtEnd())
      .or(arrayConditionalAtEnd())
      .or(arrayConditionalAtStart())
      .compile(QueryParams.params(AbstractInsnNode.class)
        .withIgnores(notAnInstruction())
        .withDebug(DEBUG)
        );

  private ClassTree currentClass;
  private Map<MethodTree, Set<AbstractInsnNode>> cache;


  private static SequenceQuery<AbstractInsnNode> conditionalAtEnd() {
    final Slot<LabelNode> loopStart = Slot.create(LabelNode.class);
    final Slot<LabelNode> loopEnd = Slot.create(LabelNode.class);
    return QueryStart
        .any(AbstractInsnNode.class)
        .zeroOrMore(QueryStart.match(anyInstruction()))
        .then(aMethodCallReturningAnIterator().and(mutationPoint()))
        .then(ASTORE)
        .then(gotoLabel(loopEnd.write()))
        .then(aLabelNode(loopStart.write()))
        .then(ALOAD)
        .then(methodCallTo(ClassName.fromString("java/util/Iterator"), "next").and(mutationPoint()))
        .zeroOrMore(QueryStart.match(anyInstruction()))
        .then(labelNode(loopEnd.read()))
        .then(ALOAD)
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
        .then(ASTORE)
        .then(aLabelNode(loopStart.write()))
        .then(ALOAD)
        .then(hasNextMethodCall().and(mutationPoint()))
        .then(aConditionalJump().and(jumpsTo(loopEnd.write())).and(mutationPoint()))
        .then(ALOAD)
        .then(methodCallTo(ClassName.fromString("java/util/Iterator"), "next").and(mutationPoint()))
        .zeroOrMore(QueryStart.match(anyInstruction()))
        .then(GOTO.and(jumpsTo(loopStart.read())))
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
        .then(ARRAYLENGTH.and(mutationPoint()))
        .then(ISTORE)
        .then(ICONST_0.and(mutationPoint()))
        .then(anIStore(counter.write()).and(debug("store")))
        .then(gotoLabel(loopEnd.write()))
        .then(aLabelNode(loopStart.write()))
        .zeroOrMore(QueryStart.match(anyInstruction()))
        .then(incrementsVariable(counter.read()).and(mutationPoint()))
        .then(labelNode(loopEnd.read()))
        .then(ILOAD)
        .then(ILOAD)
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
        .then(ARRAYLENGTH.and(mutationPoint()))
        .then(ISTORE)
        .then(ICONST_0.and(mutationPoint()))
        .then(anIStore(counter.write()).and(debug("store")))
        .then(aLabelNode(loopStart.write()))
        .then(ILOAD)
        .then(ILOAD)
        .then(aConditionalJump().and(jumpsTo(loopEnd.write())).and(mutationPoint()))
        .zeroOrMore(QueryStart.match(anyInstruction()))
        .then(incrementsVariable(counter.read()).and(mutationPoint()))
        .then(GOTO.and(jumpsTo(loopStart.read())))
        .zeroOrMore(QueryStart.match(anyInstruction()));
  }

  private static Match<AbstractInsnNode> hasNextMethodCall() {
    return methodCallTo(ClassName.fromString("java/util/Iterator"), "hasNext");
  }

  private static Match<AbstractInsnNode> aMethodCallReturningAnIterator() {
    return methodCallThatReturns(ClassName.fromClass(Iterator.class));
  }

  private static Match<AbstractInsnNode> mutationPoint() {
    return (c,t) -> {
      c.retrieve(LOOP_INSTRUCTIONS.read()).get().add(t);
      return result(true, c);
    };
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
    return mutations.stream().filter(mutatesIteratorLoopPlumbing().negate())
            .collect(Collectors.toList());
  }

  private Predicate<MutationDetails> mutatesIteratorLoopPlumbing() {
    return a -> {
      final int instruction = a.getInstructionIndex();
      final Optional<MethodTree> maybeMethod = currentClass.method(a.getId().getLocation());
      if (!maybeMethod.isPresent()) {
        return false;
      }
      MethodTree method = maybeMethod.get();

      final AbstractInsnNode mutatedInstruction = method.instruction(instruction);

      Set<AbstractInsnNode> toAvoid = cache.computeIfAbsent(method, this::findLoopInstructions);

      return toAvoid.contains(mutatedInstruction);
    };
  }

  private Set<AbstractInsnNode> findLoopInstructions(MethodTree method) {
    Context context = Context.start(DEBUG).store(LOOP_INSTRUCTIONS.write(), new ArrayList<>());
    return ITERATOR_LOOP.contextMatches(method.instructions(), context).stream()
            .flatMap(c -> c.retrieve(LOOP_INSTRUCTIONS.read()).get().stream())
            .collect(Collectors.toSet());
  }

  @Override
  public void end() {
    this.currentClass = null;
    this.cache = null;
  }
}
