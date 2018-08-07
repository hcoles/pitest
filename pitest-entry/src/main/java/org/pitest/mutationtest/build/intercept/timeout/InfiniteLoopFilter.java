package org.pitest.mutationtest.build.intercept.timeout;

import static org.pitest.bytecode.analysis.MethodMatchers.forLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.bytecode.analysis.MethodTree;
import org.pitest.functional.FCollection;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.engine.Location;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.sequence.SequenceMatcher;

public abstract class InfiniteLoopFilter implements MutationInterceptor {

  private ClassTree currentClass;

  @Override
  public InterceptorType type() {
    return InterceptorType.FILTER;
  }

  @Override
  public void begin(ClassTree clazz) {
    this.currentClass = clazz;
  }

  abstract SequenceMatcher<AbstractInsnNode> infiniteLoopMatcher();
  abstract  boolean couldCauseInfiniteLoop(MethodTree method, MutationDetails each);

  @Override
  public Collection<MutationDetails> intercept(
      Collection<MutationDetails> mutations, Mutater m) {
    final Map<Location,Collection<MutationDetails>> buckets = FCollection.bucket(mutations, mutationToLocation());

    final List<MutationDetails> willTimeout = new ArrayList<>();
    for (final Entry<Location, Collection<MutationDetails>> each : buckets.entrySet() ) {
      willTimeout.addAll(findTimeoutMutants(each.getKey(), each.getValue(), m));
    }
    mutations.removeAll(willTimeout);
    return mutations;
  }

  private Collection<MutationDetails> findTimeoutMutants(Location location,
      Collection<MutationDetails> mutations, Mutater m) {

    final MethodTree method = this.currentClass.methods().stream()
        .filter(forLocation(location))
        .findFirst()
        .get();

    //  give up if our matcher thinks loop is already infinite
    if (infiniteLoopMatcher().matches(method.instructions())) {
      return Collections.emptyList();
    }

    final List<MutationDetails> timeouts = new ArrayList<>();
    for ( final MutationDetails each : mutations ) {
      // avoid cost of static analysis by first checking mutant is on
      // on instruction that could affect looping
      if (couldCauseInfiniteLoop(method, each) && isInfiniteLoop(each,m) ) {
        timeouts.add(each);
      }
    }
    return timeouts;

  }

  private boolean isInfiniteLoop(MutationDetails each, Mutater m) {
    final ClassTree mutantClass = ClassTree.fromBytes(m.getMutation(each.getId()).getBytes());
    final Optional<MethodTree> mutantMethod = mutantClass.methods().stream()
        .filter(forLocation(each.getId().getLocation()))
        .findFirst();
    return infiniteLoopMatcher().matches(mutantMethod.get().instructions());
  }

  private Function<MutationDetails, Location> mutationToLocation() {
    return a -> a.getId().getLocation();
  }

  @Override
  public void end() {
    this.currentClass = null;
  }

}

