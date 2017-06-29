package org.pitest.mutationtest.build.intercept.timeout;

import static org.pitest.bytecode.analysis.InstructionMatchers.aConditionalJump;
import static org.pitest.bytecode.analysis.InstructionMatchers.aJump;
import static org.pitest.bytecode.analysis.InstructionMatchers.aLabelNode;
import static org.pitest.bytecode.analysis.InstructionMatchers.aPush;
import static org.pitest.bytecode.analysis.InstructionMatchers.anIntegerConstant;
import static org.pitest.bytecode.analysis.InstructionMatchers.anyInstruction;
import static org.pitest.bytecode.analysis.InstructionMatchers.debug;
import static org.pitest.bytecode.analysis.InstructionMatchers.gotoLabel;
import static org.pitest.bytecode.analysis.InstructionMatchers.increments;
import static org.pitest.bytecode.analysis.InstructionMatchers.isA;
import static org.pitest.bytecode.analysis.InstructionMatchers.jumpsTo;
import static org.pitest.bytecode.analysis.InstructionMatchers.labelNode;
import static org.pitest.bytecode.analysis.InstructionMatchers.load;
import static org.pitest.bytecode.analysis.InstructionMatchers.methodCallThatReturns;
import static org.pitest.bytecode.analysis.InstructionMatchers.methodCallTo;
import static org.pitest.bytecode.analysis.InstructionMatchers.opCode;
import static org.pitest.bytecode.analysis.InstructionMatchers.stores;
import static org.pitest.bytecode.analysis.InstructionMatchers.storesTo;
import static org.pitest.bytecode.analysis.MethodMatchers.forLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.bytecode.analysis.InstructionMatchers;
import org.pitest.bytecode.analysis.MethodTree;
import org.pitest.classinfo.ClassName;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.Option;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.engine.Location;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.sequence.Match;
import org.pitest.sequence.QueryStart;
import org.pitest.sequence.SequenceMatcher;
import org.pitest.sequence.SequenceQuery;
import org.pitest.sequence.Slot;

/**
 * Removes mutants that will create infinite loops.
 * 
 * Types of infinite loop detected
 *   For loops without increments
 *   While loops with check on counter without increments
 *   Iterator loops without call to iterator.next (NOT yet implemented)
 *   For loops with conditional hard coded to true (NOT yet implemented)
 *   While loop with conditional hard coded to true (NOT yet implemented)
 *
 * Check is not designed to catch all possible infinite loops. Aim is to improve
 * performance by reducing number of mutants that timeout (costing about 4 seconds).
 */
public class SimpleInfiniteLoopInterceptor implements MutationInterceptor {

  private static final Match<AbstractInsnNode> IGNORE = isA(LineNumberNode.class).or(isA(FrameNode.class));
  
  static final SequenceMatcher<AbstractInsnNode> INFINITE_LOOP = QueryStart
      .match(Match.<AbstractInsnNode>never())
      .or(infiniteCountingLoopConditionAtStart())
      .or(infiniteCountingLoopConditionAtEnd())
      .or(inifniteIteratorLoop())
      .or(infiniteIteratorLoopJavac())
      .compileIgnoring(IGNORE);
      
  private ClassTree currentClass;
  
  private static SequenceQuery<AbstractInsnNode> doesNotBreakIteratorLoop() {
    return QueryStart.match(methodCallTo(ClassName.fromClass(Iterator.class), "next").negate());
  }  

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
    if (INFINITE_LOOP.matches(method.instructions())) {
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
    return INFINITE_LOOP.matches(mutantMethod.value().instructions());
  }
  
  private boolean couldCauseInfiniteLoop(MethodTree method, MutationDetails each) {
    AbstractInsnNode instruction = method.instructions().get(each.getInstructionIndex());
    return instruction.getOpcode() == Opcodes.IINC || isIteratorNext(instruction);
  }

  private boolean isIteratorNext(AbstractInsnNode instruction) {
    return InstructionMatchers.methodCallTo(ClassName.fromClass(Iterator.class), "next").test(null, instruction);
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
  
  private static SequenceQuery<AbstractInsnNode> inifniteIteratorLoop() {
    Slot<LabelNode> loopStart = Slot.create(LabelNode.class);
    
    return QueryStart
        .any(AbstractInsnNode.class)
        .then(methodCallThatReturns(ClassName.fromString("java/util/Iterator")))
        .then(opCode(Opcodes.ASTORE))
        .zeroOrMore(QueryStart.match(anyInstruction()))
        .then(aJump())
        .then(aLabelNode(loopStart.write()))
        .oneOrMore(doesNotBreakIteratorLoop())
        .then(jumpsTo(loopStart.read()))
        // can't currently deal with loops with conditionals that cause additional jumps back
        .zeroOrMore(QueryStart.match(jumpsTo(loopStart.read()).negate()));
  }
  
  private static SequenceQuery<AbstractInsnNode> infiniteIteratorLoopJavac() {
    Slot<LabelNode> loopStart = Slot.create(LabelNode.class);
    
    return  QueryStart
        .any(AbstractInsnNode.class)
        .then(methodCallThatReturns(ClassName.fromString("java/util/Iterator")))
        .then(opCode(Opcodes.ASTORE))
        .then(aLabelNode(loopStart.write()))
        .oneOrMore(doesNotBreakIteratorLoop())
        .then(jumpsTo(loopStart.read()))
        // can't currently deal with loops with conditionals that cause additional jumps back
        .zeroOrMore(QueryStart.match(jumpsTo(loopStart.read()).negate()));   
  }
  
  private static SequenceQuery<AbstractInsnNode> infiniteCountingLoopConditionAtStart() {
    Slot<Integer> counterVariable = Slot.create(Integer.class);
    Slot<LabelNode> loopStart = Slot.create(LabelNode.class);
    return QueryStart
        .any(AbstractInsnNode.class)
        .then(anIntegerConstant())
        .then(stores(counterVariable.write()))
        .then(aLabelNode(loopStart.write()))
        .then(load(counterVariable.read()))
        .then(aPush())
        .then(aConditionalJump())
        .zeroOrMore(doesNotBreakLoop(counterVariable))
        .then(jumpsTo(loopStart.read()))
        // can't currently deal with loops with conditionals that cause additional jumps back
        .zeroOrMore(QueryStart.match(jumpsTo(loopStart.read()).negate()));
  }
  
  private static SequenceQuery<AbstractInsnNode> infiniteCountingLoopConditionAtEnd() {
    Slot<Integer> counterVariable = Slot.create(Integer.class);
    Slot<LabelNode> loopStart = Slot.create(LabelNode.class);
    Slot<LabelNode> loopEnd = Slot.create(LabelNode.class);
    
    return QueryStart
        .any(AbstractInsnNode.class)
        .then(anIntegerConstant())
        .then(stores(counterVariable.write()).and(debug("found counter")))
        .then(isA(LabelNode.class))
        .then(gotoLabel(loopEnd.write()))
        .then(aLabelNode(loopStart.write()).and(debug("loop start")))
        .zeroOrMore(doesNotBreakLoop(counterVariable))
        .then(labelNode(loopEnd.read()).and(debug("loop end")))
        .then(load(counterVariable.read()).and(debug("read"))) // is it really important that we read the counter?
        .zeroOrMore(doesNotBreakLoop(counterVariable))
        .then(jumpsTo(loopStart.read()).and(debug("jump")))
        .zeroOrMore(QueryStart.match(anyInstruction()));
        // can't currently deal with loops with conditionals that cause additional jumps back
        //.zeroOrMore(QueryStart.match(jumpsTo(loopStart.read()).negate()));
  }
  
  private static SequenceQuery<AbstractInsnNode> doesNotBreakLoop(Slot<Integer> counterVariable) {
    return QueryStart
        .match(storesTo(counterVariable.read()).and(debug("broken by store"))
            .or(increments(counterVariable.read()))
            .negate());
  }
}
