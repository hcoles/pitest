package org.pitest.mutationtest.build.intercept.staticinitializers;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.bytecode.analysis.MethodTree;
import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.engine.Location;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

      Map<Location, List<Call>> callTree = tree.methods().stream()
              .filter(m -> m.isPrivate() || m.rawNode().name.equals("<clinit>"))
              .flatMap(m -> callsFor(tree, m).stream().map(c -> new Call(m.asLocation(), c)))
              .filter(c -> privateMethods.contains(c.to()))
              .collect(Collectors.groupingBy(Call::from));

      Set<Location> visited = new HashSet<>();

      visit(callTree, visited, clinit.get().asLocation());

      this.isStaticInitCode = m -> visited.contains(m.getId().getLocation());
    }
  }

  private List<Location> callsFor(ClassTree tree, MethodTree m) {
    return m.instructions().stream()
            .flatMap(is(MethodInsnNode.class))
            .filter(calls(tree.name()))
            .map(this::asLocation)
            .collect(Collectors.toList());
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

  private Location asLocation(MethodInsnNode call) {
    return Location.location(ClassName.fromString(call.owner), call.name, call.desc);
  }

  private Predicate<MethodInsnNode> calls(final ClassName self) {
    return a -> a.owner.equals(self.asInternalName());
  }

  private <T extends AbstractInsnNode> Function<AbstractInsnNode,Stream<T>> is(final Class<T> clazz) {
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
}