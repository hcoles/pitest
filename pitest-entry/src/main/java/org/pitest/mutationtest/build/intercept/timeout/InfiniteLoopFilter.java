package org.pitest.mutationtest.build.intercept.timeout;

import static org.pitest.bytecode.analysis.InstructionMatchers.isA;
import static org.pitest.bytecode.analysis.MethodMatchers.forLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.bytecode.analysis.MethodTree;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.Option;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.engine.Location;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.sequence.Match;
import org.pitest.sequence.SequenceMatcher;

public abstract class InfiniteLoopFilter implements MutationInterceptor {
  
  static final Match<AbstractInsnNode> IGNORE = isA(LineNumberNode.class).or(isA(FrameNode.class));
  
  private ClassTree currentClass;
  
  @Override
  public InterceptorType type() {
    return InterceptorType.FILTER;
  }

  @Override
  public void begin(ClassTree clazz) {
    currentClass = clazz;
  }

  abstract SequenceMatcher<AbstractInsnNode> infiniteLoopMatcher();
  abstract  boolean couldCauseInfiniteLoop(MethodTree method, MutationDetails each);
  
  @Override
  public Collection<MutationDetails> intercept(
      Collection<MutationDetails> mutations, Mutater m) {
    Map<Location,Collection<MutationDetails>> buckets = FCollection.bucket(mutations, mutationToLocation());
    
    List<MutationDetails> willTimeout = new ArrayList<MutationDetails>();
    for (Entry<Location, Collection<MutationDetails>> each : buckets.entrySet() ) {
      willTimeout.addAll(findTimeoutMutants(each.getKey(), each.getValue(), m));
    }
    mutations.removeAll(willTimeout);
    return mutations;
  }

  private Collection<MutationDetails> findTimeoutMutants(Location location,
      Collection<MutationDetails> mutations, Mutater m) {
    
    MethodTree method = currentClass.methods().findFirst(forLocation(location)).value();
    
    //  give up if our matcher thinks loop is already infinite 
    if (infiniteLoopMatcher().matches(method.instructions())) {
      return Collections.emptyList();
    }
    
    List<MutationDetails> timeouts = new ArrayList<MutationDetails>();
    for ( MutationDetails each : mutations ) {
      // avoid cost of static analysis by first checking mutant is on
      // on instruction that could affect looping
      if (couldCauseInfiniteLoop(method, each) && isInfiniteLoop(each,m) ) {
        timeouts.add(each);
      }
    }
    return timeouts;
    
  }

  private boolean isInfiniteLoop(MutationDetails each, Mutater m) {
    ClassTree mutantClass = ClassTree.fromBytes(m.getMutation(each.getId()).getBytes());
    Option<MethodTree> mutantMethod = mutantClass.methods().findFirst(forLocation(each.getId().getLocation()));
    return infiniteLoopMatcher().matches(mutantMethod.value().instructions());
  }
  
  private F<MutationDetails, Location> mutationToLocation() {
    return new F<MutationDetails, Location>() {
      @Override
      public Location apply(MutationDetails a) {
        return a.getId().getLocation();
      }
    };
  }

  @Override
  public void end() {
    currentClass = null;
  }
  
}

