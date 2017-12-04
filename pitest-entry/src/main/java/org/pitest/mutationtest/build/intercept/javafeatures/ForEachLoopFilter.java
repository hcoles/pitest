package org.pitest.mutationtest.build.intercept.javafeatures;

import static org.pitest.bytecode.analysis.InstructionMatchers.aConditionalJump;
import static org.pitest.bytecode.analysis.InstructionMatchers.aConditionalJumpTo;
import static org.pitest.bytecode.analysis.InstructionMatchers.aLabelNode;
import static org.pitest.bytecode.analysis.InstructionMatchers.anIStore;
import static org.pitest.bytecode.analysis.InstructionMatchers.anyInstruction;
import static org.pitest.bytecode.analysis.InstructionMatchers.debug;
import static org.pitest.bytecode.analysis.InstructionMatchers.gotoLabel;
import static org.pitest.bytecode.analysis.InstructionMatchers.incrementsVariable;
import static org.pitest.bytecode.analysis.InstructionMatchers.isA;
import static org.pitest.bytecode.analysis.InstructionMatchers.jumpsTo;
import static org.pitest.bytecode.analysis.InstructionMatchers.labelNode;
import static org.pitest.bytecode.analysis.InstructionMatchers.methodCallThatReturns;
import static org.pitest.bytecode.analysis.InstructionMatchers.methodCallTo;
import static org.pitest.bytecode.analysis.InstructionMatchers.opCode;
import static org.pitest.bytecode.analysis.InstructionMatchers.recordTarget;

import java.util.Collection;
import java.util.Iterator;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.LabelNode;
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
import org.pitest.sequence.SequenceQuery;
import org.pitest.sequence.Slot;

public class ForEachLoopFilter implements MutationInterceptor {
  
  private static final boolean DEBUG = false;
  
  private static final Match<AbstractInsnNode> IGNORE = isA(LineNumberNode.class)
      .or(isA(FrameNode.class)
      );
  
  private static final Slot<AbstractInsnNode> MUTATED_INSTRUCTION = Slot.create(AbstractInsnNode.class);
  private static final Slot<Boolean> FOUND = Slot.create(Boolean.class);
    
  
  private static final SequenceMatcher<AbstractInsnNode> ITERATOR_LOOP = QueryStart
      .match(Match.<AbstractInsnNode>never())
      .or(conditionalAtStart())
      .or(conditionalAtEnd()) 
      .or(arrayConditionalAtEnd())
      .or(arrayConditionalAtStart())      
      .then(containMutation(FOUND))
      .compile(QueryParams.params(AbstractInsnNode.class)
        .withIgnores(IGNORE)
        .withDebug(DEBUG)
        );

  private ClassTree currentClass;
  
   
  private static SequenceQuery<AbstractInsnNode> conditionalAtEnd() {
    Slot<LabelNode> loopStart = Slot.create(LabelNode.class);
    Slot<LabelNode> loopEnd = Slot.create(LabelNode.class);
    return QueryStart
        .any(AbstractInsnNode.class)
        .zeroOrMore(QueryStart.match(anyInstruction()))
        .then(aMethodCallReturningAnIterator().and(mutationPoint()))
        .then(opCode(Opcodes.ASTORE))
        .then(gotoLabel(loopEnd.write()))
        .then(aLabelNode(loopStart.write()))
        .then(opCode(Opcodes.ALOAD))
        .then(methodCallTo(ClassName.fromString("java/util/Iterator"), "next").and(mutationPoint()))
        .zeroOrMore(QueryStart.match(anyInstruction()))
        .then(labelNode(loopEnd.read()))
        .then(opCode(Opcodes.ALOAD))
        .then(methodCallTo(ClassName.fromString("java/util/Iterator"), "hasNext").and(mutationPoint()))        
        .then(aConditionalJumpTo(loopStart).and(mutationPoint()))
        .zeroOrMore(QueryStart.match(anyInstruction()));
  }


  private static SequenceQuery<AbstractInsnNode> conditionalAtStart() {
    Slot<LabelNode> loopStart = Slot.create(LabelNode.class);
    Slot<LabelNode> loopEnd = Slot.create(LabelNode.class);
    return QueryStart
        .any(AbstractInsnNode.class)
        .zeroOrMore(QueryStart.match(anyInstruction()))
        .then(aMethodCallReturningAnIterator().and(mutationPoint())) 
        .then(opCode(Opcodes.ASTORE))
        .then(aLabelNode(loopStart.write()))
        .then(opCode(Opcodes.ALOAD))
        .then(methodCallTo(ClassName.fromString("java/util/Iterator"), "hasNext").and(mutationPoint()))
        .then(aConditionalJump().and(jumpsTo(loopEnd.write())).and(mutationPoint()))
        .then(opCode(Opcodes.ALOAD))
        .then(methodCallTo(ClassName.fromString("java/util/Iterator"), "next").and(mutationPoint()))
        .zeroOrMore(QueryStart.match(anyInstruction()))
        .then(opCode(Opcodes.GOTO).and(jumpsTo(loopStart.read())))
        .then(labelNode(loopEnd.read()))
        .zeroOrMore(QueryStart.match(anyInstruction()));
  }
  

  private static SequenceQuery<AbstractInsnNode> arrayConditionalAtEnd() {
    Slot<LabelNode> loopStart = Slot.create(LabelNode.class);
    Slot<LabelNode> loopEnd = Slot.create(LabelNode.class);
    Slot<Integer> counter = Slot.create(Integer.class);    
    return QueryStart
        .any(AbstractInsnNode.class)
        .zeroOrMore(QueryStart.match(anyInstruction()))
        .then(opCode(Opcodes.ARRAYLENGTH).and(mutationPoint()))
        .then(opCode(Opcodes.ISTORE))
        .then(opCode(Opcodes.ICONST_0).and(mutationPoint()))
        .then(anIStore(counter.write()).and(debug("store")))
        .then(gotoLabel(loopEnd.write()))
        .then(aLabelNode(loopStart.write()))
        .zeroOrMore(QueryStart.match(anyInstruction()))
        .then(incrementsVariable(counter.read()).and(mutationPoint()))
        .then(labelNode(loopEnd.read()))   
        .then(opCode(Opcodes.ILOAD))
        .then(opCode(Opcodes.ILOAD))   
        .then(aConditionalJumpTo(loopStart).and(mutationPoint()))
        .zeroOrMore(QueryStart.match(anyInstruction()));
  }

  private static SequenceQuery<AbstractInsnNode> arrayConditionalAtStart() {
    Slot<LabelNode> loopStart = Slot.create(LabelNode.class);
    Slot<LabelNode> loopEnd = Slot.create(LabelNode.class);
    Slot<Integer> counter = Slot.create(Integer.class);
    return QueryStart
        .any(AbstractInsnNode.class)
        .zeroOrMore(QueryStart.match(anyInstruction()))
        .then(opCode(Opcodes.ARRAYLENGTH).and(mutationPoint()))
        .then(opCode(Opcodes.ISTORE))
        .then(opCode(Opcodes.ICONST_0).and(mutationPoint()))
        .then(anIStore(counter.write()).and(debug("store")))
        .then(aLabelNode(loopStart.write()))
        .then(opCode(Opcodes.ILOAD))
        .then(opCode(Opcodes.ILOAD))
        .then(aConditionalJump().and(jumpsTo(loopEnd.write())).and(mutationPoint()))
        .zeroOrMore(QueryStart.match(anyInstruction()))
        .then(incrementsVariable(counter.read()).and(mutationPoint()))
        .then(opCode(Opcodes.GOTO).and(jumpsTo(loopStart.read())))
        .zeroOrMore(QueryStart.match(anyInstruction()));
  }

  
  private static Match<AbstractInsnNode> aMethodCallReturningAnIterator() {
    return methodCallThatReturns(ClassName.fromClass(Iterator.class));
  }
  
  private static Match<AbstractInsnNode> mutationPoint() {
    return recordTarget(MUTATED_INSTRUCTION.read(), FOUND.write());
  }
  
  
  private static Match<AbstractInsnNode> containMutation(final Slot<Boolean> found) {
   return new Match<AbstractInsnNode>() {
    @Override
    public boolean test(Context<AbstractInsnNode> c, AbstractInsnNode t) {
      return c.retrieve(found.read()).hasSome();
    }
     
   };
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
    return FCollection.filter(mutations, Prelude.not(mutatesIteratorLoopPlumbing()));
  }

  private F<MutationDetails, Boolean> mutatesIteratorLoopPlumbing() {
    return new F<MutationDetails, Boolean>() {
      @Override
      public Boolean apply(MutationDetails a) {
        int instruction = a.getInstructionIndex();
        MethodTree method = currentClass.methods().findFirst(MethodMatchers.forLocation(a.getId().getLocation())).value();
        AbstractInsnNode mutatedInstruction = method.instructions().get(instruction);

        Context<AbstractInsnNode> context = Context.start(method.instructions(), DEBUG);
        context.store(MUTATED_INSTRUCTION.write(), mutatedInstruction);
        return ITERATOR_LOOP.matches(method.instructions(), context); 
      } 
    };
  }
  
  @Override
  public void end() {
    currentClass = null; 
  }
}
