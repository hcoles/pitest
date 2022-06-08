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
import org.pitest.sequence.QueryStart;
import org.pitest.sequence.SequenceMatcher;
import org.pitest.sequence.SequenceQuery;
import org.pitest.sequence.Slot;
import org.pitest.sequence.SlotRead;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.pitest.bytecode.analysis.InstructionMatchers.anyInstruction;
import static org.pitest.bytecode.analysis.InstructionMatchers.debug;
import static org.pitest.bytecode.analysis.InstructionMatchers.isA;
import static org.pitest.bytecode.analysis.InstructionMatchers.methodCallNamed;
import static org.pitest.bytecode.analysis.InstructionMatchers.methodDescEquals;
import static org.pitest.bytecode.analysis.InstructionMatchers.notAnInstruction;
import static org.pitest.bytecode.analysis.InstructionMatchers.writeNodeToSlot;
import static org.pitest.bytecode.analysis.OpcodeMatchers.ALOAD;
import static org.pitest.bytecode.analysis.OpcodeMatchers.ASTORE;
import static org.pitest.bytecode.analysis.OpcodeMatchers.ATHROW;
import static org.pitest.bytecode.analysis.OpcodeMatchers.GOTO;
import static org.pitest.bytecode.analysis.OpcodeMatchers.IFNONNULL;
import static org.pitest.bytecode.analysis.OpcodeMatchers.IFNULL;
import static org.pitest.bytecode.analysis.OpcodeMatchers.IF_ACMPEQ;
import static org.pitest.bytecode.analysis.OpcodeMatchers.INVOKEINTERFACE;
import static org.pitest.bytecode.analysis.OpcodeMatchers.INVOKEVIRTUAL;
import static org.pitest.sequence.QueryStart.any;
import static org.pitest.sequence.QueryStart.match;
import static org.pitest.sequence.Result.result;

public class TryWithResourcesFilter implements MutationInterceptor {

  private static final boolean DEBUG = false;

  private static final Slot<List<LabelNode>> HANDLERS = Slot.createList(LabelNode.class);

  private static final Slot<AbstractInsnNode> START = Slot.create(AbstractInsnNode.class);
  private static final Slot<AbstractInsnNode> END = Slot.create(AbstractInsnNode.class);

  private ClassTree currentClass;
  private Map<MethodTree, List<Region>> cache;

  private static final SequenceMatcher<AbstractInsnNode> TRY_WITH_RESOURCES =
          javac11()
          .or(javac())
          .or(ecj())
          .compile(QueryParams.params(AbstractInsnNode.class)
                  .withIgnores(notAnInstruction().or(aLabel().and(isLabel(HANDLERS.read()).negate())))
                  .withDebug(DEBUG)
          );

  private static SequenceQuery<AbstractInsnNode> javac11() {
    return any(AbstractInsnNode.class)
            .zeroOrMore(match(anyInstruction()))
            .then(closeSequence(true))
            .zeroOrMore(match(anyInstruction()))
            .then(isLabel(HANDLERS.read()).and(debug("handler")))
            .then(ASTORE)
            .then(ALOAD)
            .then(closeSequence(false))
            .then(GOTO)
            .then(isLabel(HANDLERS.read()).and(debug("handler")))
            .then(ASTORE)
            .then(ALOAD)
            .then(ALOAD)
            .then(addSuppressedMethodCall().and(debug("add suppressed")))
            .then(ALOAD)
            .then(ATHROW.and(recordPoint(END, true)))
            .zeroOrMore(match(anyInstruction()));
  }

  private static SequenceQuery<AbstractInsnNode> javac() {
    return any(AbstractInsnNode.class)
            .zeroOrMore(match(anyInstruction()))
            .then(javacCloseSequence(true))
            .zeroOrMore(match(anyInstruction()))
            .then(isLabel(HANDLERS.read()).and(debug("handler")))
            .then(ASTORE)
            .then(ALOAD)
            .then(ASTORE)
            .then(ALOAD)
            .then(ATHROW)
            .then(ASTORE)
            .then(javacCloseSequence(false))
            .then(ALOAD)
            .then(ATHROW.and(recordPoint(END, true)))
            .zeroOrMore(match(anyInstruction()));
  }

  private static SequenceQuery<AbstractInsnNode> ecj() {
    return any(AbstractInsnNode.class)
            .zeroOrMore(match(anyInstruction()))
            .then(ecjCloseSequence(true))
            .zeroOrMore(match(anyInstruction()))
            .then(ecjCloseAndThrow())
            .zeroOrMore(ecjCloseSuppress())
            .then(ecjSuppress())
            .then(ALOAD)
            .then(ATHROW.and(recordPoint(END, true)))
            .zeroOrMore(match(anyInstruction()));
  }

  private static SequenceQuery<AbstractInsnNode> ecjCloseSuppress() {
    return ecjCloseSequence(false)
            .then(GOTO) // FIXME check jump target?
            .then(ecjSuppress())
            .then(ecjCloseAndThrow());
  }

  private static SequenceQuery<AbstractInsnNode> ecjSuppress() {
    return match(ASTORE)
            .then(ALOAD)
            .then(IFNONNULL)
            .then(ALOAD)
            .then(ASTORE)
            .then(GOTO)
            .then(ALOAD)
            .then(ALOAD)
            .then(IF_ACMPEQ)
            .then(ALOAD)
            .then(ALOAD)
            .then(addSuppressedMethodCall());
  }

  private static SequenceQuery<AbstractInsnNode> ecjCloseSequence(boolean record) {
    return match(ALOAD.and(recordPoint(START,record)))
            .then(IFNULL) // FIXME check jump target?
            .then(ALOAD)
            .then(closeMethodCall());
  }

  private static SequenceQuery<AbstractInsnNode> ecjCloseAndThrow() {
    return match(ALOAD)
            .then(IFNULL) // FIXME check jump target?
            .then(ALOAD)
            .then(closeMethodCall())
            // omit label check ?
            .then(ALOAD)
            .then(ATHROW);
  }

  private static SequenceQuery<AbstractInsnNode> javacCloseSequence(boolean record) {
    // javac may (or may not) generate a null check before the close
    return methodSequence(record)
            .or(fullSequence(record))
            .or(omittedNullCheckSequence(record))
            .or(optimalSequence(record));
  }

  private static SequenceQuery<AbstractInsnNode> methodSequence(boolean record) {
    return QueryStart.match(ALOAD.and(recordPoint(START, record)))
            .then(IFNULL)
            .then(ALOAD)
            .then(ALOAD)
            .then(closeResourceMethodCall());
  }

  private static SequenceQuery<AbstractInsnNode> fullSequence(boolean record) {
    return QueryStart.match(ALOAD.and(recordPoint(START, record)))
            .then(IFNULL)
            .then(omittedNullCheckSequence(false));
  }

  private static SequenceQuery<AbstractInsnNode> omittedNullCheckSequence(boolean record) {
    return QueryStart.match(ALOAD.and(recordPoint(START, record)))
            .then(IFNULL)
            .then(ALOAD)
            .then(closeMethodCall())
            .then(GOTO.and(debug("goto")))
            .then(isLabel(HANDLERS.read()).and(debug("handler")))
            .then(ASTORE.and(debug("store")))
            .then(ALOAD)
            .then(ALOAD)
            .then(addSuppressedMethodCall())
            .then(GOTO)
            .then(ALOAD)
            .then(closeMethodCall().and(debug("end of sequence")));
  }

  private static SequenceQuery<AbstractInsnNode> optimalSequence(boolean record) {
    return QueryStart.match(ALOAD.and(recordPoint(START, record)))
            .then(ALOAD)
            .then(closeResourceMethodCall());
  }

  private static SequenceQuery<AbstractInsnNode> closeSequence(boolean record) {
    // javac may (or may not) generate a null check before the close
    return match(closeMethodCall().and(recordPoint(START, record)))
            .or(match(IFNULL.and(recordPoint(START, record)))
                    .then(ALOAD)
                    .then(closeMethodCall()));
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

      List<Region> regions = cache.computeIfAbsent(method, this::computeRegions);

      return regions.stream()
              .anyMatch(r -> instruction >= method.instructions().indexOf(r.start) && instruction <= method.instructions().indexOf(r.end));

    };
  }

  private List<Region> computeRegions(MethodTree method) {
    List<LabelNode> handlers = method.rawNode().tryCatchBlocks.stream()
            .filter(t -> "java/lang/Throwable".equals(t.type))
            .filter(t -> t.handler != null)
            .map(t -> t.handler)
            .collect(Collectors.toList());


    Context context = Context.start(DEBUG);
    context = context.store(HANDLERS.write(), handlers);
    List<Region> regions = TRY_WITH_RESOURCES.contextMatches(method.instructions(), context).stream()
            .map(c -> new Region(c.retrieve(START.read()).get(), c.retrieve(END.read()).get()))
            .collect(Collectors.toList());
    return regions;
  }

  @Override
  public void end() {
    this.currentClass = null;
    this.cache = null;
  }

  private static Match<AbstractInsnNode> aLabel() {
    return isA(LabelNode.class);
  }

  private static Match<AbstractInsnNode> isLabel(SlotRead<List<LabelNode>> read) {
    return aLabel().and((c,t) -> result(c.retrieve(read).get().contains(t), c));
  }

  private static Match<AbstractInsnNode> closeMethodCall() {
    return methodCallNamed("close")
            .and(INVOKEINTERFACE.or(INVOKEVIRTUAL))
            .and(methodDescEquals("()V"));
  }

  private static Match<AbstractInsnNode> closeResourceMethodCall() {
    return methodCallNamed("$closeResource")
            .and(methodDescEquals("(Ljava/lang/Throwable;Ljava/lang/AutoCloseable;)V"));
  }

  private static Match<AbstractInsnNode> addSuppressedMethodCall() {
    return methodCallNamed("addSuppressed").and(methodDescEquals("(Ljava/lang/Throwable;)V"));
  }

  private static Match<AbstractInsnNode> recordPoint(Slot<AbstractInsnNode> slot, boolean record) {
    if (!record) {
      return (c,t) -> result(true,c);
    }
    return writeNodeToSlot(slot.write(), AbstractInsnNode.class);
  }

}

class Region {
  final AbstractInsnNode start;
  final AbstractInsnNode end;
  Region(AbstractInsnNode start, AbstractInsnNode end) {
    this.start = start;
    this.end = end;
  }

}

