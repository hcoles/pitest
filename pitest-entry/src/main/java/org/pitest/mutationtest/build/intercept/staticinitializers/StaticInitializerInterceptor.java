package org.pitest.mutationtest.build.intercept.staticinitializers;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.bytecode.analysis.MethodTree;
import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.engine.Location;
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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.pitest.bytecode.analysis.InstructionMatchers.anyInstruction;
import static org.pitest.bytecode.analysis.InstructionMatchers.isA;
import static org.pitest.bytecode.analysis.InstructionMatchers.methodCallNamed;
import static org.pitest.bytecode.analysis.InstructionMatchers.notAnInstruction;
import static org.pitest.bytecode.analysis.OpcodeMatchers.PUTSTATIC;
import static org.pitest.sequence.Result.result;

/**
 * Identifies and marks mutations in code that is active during class
 * Initialisation.
 *
 * The analysis is simplistic and non-exhaustive. Code is considered to be
 * for static initialisation if it is
 *
 * 1. In a static initializer (i.e <clinit>)
 * 2. In a private method or constructor called from <clinit> or another private method in the call tree
 *
 *
 */
class StaticInitializerInterceptor implements MutationInterceptor {

  static final Slot<AbstractInsnNode> START = Slot.create(AbstractInsnNode.class);

  static final SequenceMatcher<AbstractInsnNode> DELAYED_EXECUTION = QueryStart
          .any(AbstractInsnNode.class)
          .then(returnsDeferredExecutionCode().or(isA(InvokeDynamicInsnNode.class)).and(store(START.write())))
          .then(enumConstructorCallAndStore().or(QueryStart.match(PUTSTATIC)))
          .zeroOrMore(QueryStart.match(anyInstruction()))
          .compile(QueryParams.params(AbstractInsnNode.class)
                  .withIgnores(notAnInstruction())
          );

  private static Match<AbstractInsnNode> returnsDeferredExecutionCode() {
    return (c,n) -> result(n.getOpcode() == Opcodes.INVOKESTATIC && returnDelayedExecutionType(((MethodInsnNode) n).desc), c);
  }

  private static boolean returnDelayedExecutionType(String desc) {
    int endOfParams = desc.indexOf(')');
    return endOfParams <= 0 || desc.substring(endOfParams + 1).startsWith("Ljava/util/function/");
  }

  private static SequenceQuery<AbstractInsnNode> enumConstructorCallAndStore() {
    return QueryStart.match(methodCallNamed("<init>")).then(PUTSTATIC);
  }
  private Predicate<MutationDetails> isStaticInitCode;

  @Override
  public void begin(ClassTree clazz) {
      analyseClass(clazz);
  }

  @Override
  public Collection<MutationDetails> intercept(
      Collection<MutationDetails> mutations, Mutater m) {
    if (this.isStaticInitCode != null) {
      return mutations.stream()
              .filter(this.isStaticInitCode.negate())
              .collect(Collectors.toList());
    }
    return mutations;
  }

  @Override
  public void end() {
    this.isStaticInitCode = null;
  }

  private void analyseClass(ClassTree tree) {
    final Optional<MethodTree> clinit = tree.methods().stream().filter(nameEquals("<clinit>")).findFirst();

    if (clinit.isPresent()) {
      // We can't see if a method *call* is private from the call site
      // so collect a set of private methods within the class first
      Set<Location> privateMethods = tree.methods().stream()
              .filter(m -> m.isPrivate())
              .map(MethodTree::asLocation)
              .collect(Collectors.toSet());

      Set<Call> storedToSupplier = findsCallsStoredToSuppliers(tree);

      // Get map of each private method to the private methods it calls
      // Any call to a non private method breaks the chain
      Map<Location, List<Call>> callTree = tree.methods().stream()
              .filter(m -> m.isPrivate() || m.rawNode().name.equals("<clinit>"))
              .flatMap(m -> allCallsFor(tree, m).stream().map(c -> new Call(m.asLocation(), c)))
              .filter(c -> privateMethods.contains(c.to()))
              .filter(c -> !storedToSupplier.contains(c))
              .collect(Collectors.groupingBy(Call::from));


      Set<Location> calledOnlyFromStaticInitializer = new HashSet<>();

      visit(callTree, calledOnlyFromStaticInitializer, clinit.get().asLocation());

      this.isStaticInitCode = m -> calledOnlyFromStaticInitializer.contains(m.getId().getLocation());
    }
  }

  private Set<Call> findsCallsStoredToSuppliers(ClassTree tree) {
     Set<Call> all = new HashSet<>(directClinitCallsToDelayedExecutionCode(tree));
     all.addAll(storedViaEnumConstructor());
     return all;
  }

  private Set<Call> storedViaEnumConstructor() {
return Collections.emptySet();
  }

  private Set<Call> directClinitCallsToDelayedExecutionCode(ClassTree tree) {
    return tree.methods().stream()
            .filter(m -> m.isPrivate() || m.rawNode().name.equals("<clinit>"))
            .flatMap(m -> delayedExecutionCall(m).stream().map(c -> new Call(m.asLocation(), c)))
            .collect(Collectors.toSet());
  }

  private List<Location> delayedExecutionCall(MethodTree method) {
    Context context = Context.start();
    return DELAYED_EXECUTION.contextMatches(method.instructions(), context).stream()
            .map(c -> c.retrieve(START.read()).get())
            .flatMap(this::nodeToLocation)
            .collect(Collectors.toList());
  }

  private List<Location> allCallsFor(ClassTree tree, MethodTree m) {
    return Stream.concat(callsFor(tree,m), invokeDynamicCallsFor(tree,m))
            .collect(Collectors.toList());
  }

  private Stream<Location> callsFor(ClassTree tree, MethodTree m) {
    return m.instructions().stream()
            .flatMap(is(MethodInsnNode.class))
            .filter(calls(tree.name()))
            .map(this::asLocation);
  }

  private Stream<Location> invokeDynamicCallsFor(ClassTree tree, MethodTree m) {
    return m.instructions().stream()
            .flatMap(is(InvokeDynamicInsnNode.class))
            .filter(callsDynamically(tree.name()))
            .flatMap(this::asLocation);
  }

  private void visit(Map<Location, List<Call>> callTree, Set<Location> visited, Location l) {
    // avoid stack overflow if methods call each other in a cycle
    if (visited.contains(l)) {
      return;
    }

    visited.add(l);
    for (Call each : callTree.getOrDefault(l, Collections.emptyList())) {
      visit(callTree, visited, each.to());
    }
  }

  private Stream<Location> nodeToLocation(AbstractInsnNode n) {
    if (n instanceof MethodInsnNode) {
      return Stream.of(asLocation((MethodInsnNode) n));
    }

    if (n instanceof InvokeDynamicInsnNode) {
      return asLocation((InvokeDynamicInsnNode) n);
    }

    return Stream.empty();
  }

  private Location asLocation(MethodInsnNode call) {
    return Location.location(ClassName.fromString(call.owner), call.name, call.desc);
  }

  private Predicate<MethodInsnNode> calls(final ClassName self) {
    return a -> a.owner.equals(self.asInternalName());
  }

  private Predicate<InvokeDynamicInsnNode> callsDynamically(final ClassName self) {
    return a -> asLocation(a)
            .anyMatch(l -> l.getClassName().equals(self));

  }

  private Stream<Location> asLocation(InvokeDynamicInsnNode call) {
    return Arrays.stream(call.bsmArgs)
            .flatMap(is(Handle.class))
            .flatMap(this::handleToLocation);
  }

  private Stream<Location> handleToLocation(Handle handle) {
    ClassName c = ClassName.fromString(handle.getOwner());
    return Stream.of(Location.location(c, handle.getName(), handle.getDesc()));
  }

  private <T> Function<Object,Stream<T>> is(final Class<T> clazz) {
    return a -> {
      if (a.getClass().isAssignableFrom(clazz)) {
        return Stream.of((T)a);
      }
      return Stream.empty();
    };
  }

  private Predicate<MethodTree> nameEquals(final String name) {
    return a -> a.rawNode().name.equals(name);
  }

  @Override
  public InterceptorType type() {
    return InterceptorType.FILTER;
  }

  private static Match<AbstractInsnNode> store(SlotWrite<AbstractInsnNode> slot) {
    return (c, n) -> result(true, c.store(slot, n));
  }

}

class Call {
  private final Location from;
  private final Location to;

  Call(Location from, Location to) {
    this.from = from;
    this.to = to;
  }

  Location from() {
    return from;
  }

  Location to() {
    return to;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Call call = (Call) o;
    return Objects.equals(from, call.from) && Objects.equals(to, call.to);
  }

  @Override
  public int hashCode() {
    return Objects.hash(from, to);
  }
}