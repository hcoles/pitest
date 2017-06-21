package org.pitest.mutationtest.build.intercept.timeout;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceClassVisitor;
import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.bytecode.analysis.MethodTree;
import org.pitest.bytecode.analysis.Slot;
import org.pitest.classinfo.ClassName;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.functional.F;
import org.pitest.functional.FunctionalList;
import org.pitest.functional.predicate.Predicate;
import org.pitest.functional.predicate.True;
import org.pitest.functional.prelude.Prelude;
import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.gregor.GregorMutater;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.mutators.IncrementsMutator;
import org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveIncrementsMutator;
import org.pitest.sequence.QueryStart;
import org.pitest.sequence.SequenceMatcher;

public class SimpleInfiniteLoopInterceptorTest {
  ClassloaderByteArraySource    source = ClassloaderByteArraySource
      .fromContext();
  SimpleInfiniteLoopInterceptor testee = new SimpleInfiniteLoopInterceptor();
  GregorMutater                 mutator;

  @Before
  public void setUp() {
    ClassloaderByteArraySource source = ClassloaderByteArraySource
        .fromContext();
    Collection<MethodMutatorFactory> mutators = asList(
        RemoveIncrementsMutator.REMOVE_INCREMENTS_MUTATOR, IncrementsMutator.INCREMENTS_MUTATOR);
    mutator = new GregorMutater(source, True.<MethodInfo> all(), mutators);
  }

  @Test
  public void shouldFilterMutationsThatRemoveForLoopIncrement() {

    FunctionalList<MutationDetails> mutants = findMutationsFor(HasForLoop.class);

    Slot<AbstractInsnNode> loopStart = new Slot<AbstractInsnNode>();
    Slot<Integer> counterVariable = new Slot<Integer>();

    Predicate<AbstractInsnNode> ignore = Prelude.or(isA(LineNumberNode.class), isA(FrameNode.class));
    
    SequenceMatcher<AbstractInsnNode> infiniteLoop = QueryStart
        .any(AbstractInsnNode.class)
        .then(stores(counterVariable))
        .then(isA(LabelNode.class))
        .then(aGoto())
        .then(matchAndStore(isA(LabelNode.class), loopStart))
        .zeroOrMore(QueryStart.match(Prelude.not(increments(counterVariable))))
        .then(load(counterVariable))
        .then(any(AbstractInsnNode.class))
        .then(jumpsTo(loopStart))
        .zeroOrMore(QueryStart.match(any(AbstractInsnNode.class)))
        .compileIgnoring(ignore);

    ClassTree tree = ClassTree
        .fromBytes(source.getBytes(HasForLoop.class.getName()).value());
    MethodTree mutateMe = tree.methods().findFirst(named("mutateMe")).value();
    MethodTree noLoop = tree.methods().findFirst(named("noLoop")).value();

    mutateMe.instructions();

    int zeroIndexInstruction = mutants.get(0).getFirstIndex() -1;
    assertTrue(mutateMe.instructions().get(zeroIndexInstruction).getOpcode() == Opcodes.IINC);
    
   assertFalse(infiniteLoop.matches(mutateMe.instructions()));
   assertFalse(infiniteLoop.matches(noLoop.instructions()));
    
    Mutant mutant = mutator.getMutation(mutants.get(1).getId());
    
    printMutant(mutant);
    
    ClassTree mutantTree = ClassTree.fromBytes(mutant.getBytes());
    MethodTree infinite = mutantTree.methods().findFirst(named("mutateMe")).value();
    
    assertTrue(infiniteLoop.matches(infinite.instructions()));

    // at start can do a cheap check that it mutates an increment
    // at end can check the increment is for the idenitified local variable
    
  }

  private Predicate<AbstractInsnNode> increments(final Slot<Integer> counterVariable) {
    return new Predicate<AbstractInsnNode>() {
      @Override
      public Boolean apply(AbstractInsnNode a) {
        if (a instanceof IincInsnNode) {
          IincInsnNode inc = (IincInsnNode) a;
          return counterVariable.apply(inc.var) && (inc.incr != -1);
        } else {
          return false;
        }
      }
      
    };
  }

  private Predicate<AbstractInsnNode> stores(
      final Slot<Integer> counterVariable) {
    return new Predicate<AbstractInsnNode>() {
      @Override
      public Boolean apply(AbstractInsnNode a) {
        if (!(a instanceof VarInsnNode)) {
          return false;
        }
        VarInsnNode varNode = (VarInsnNode) a;

        if (a.getOpcode() == Opcodes.ISTORE) {
          counterVariable.setLastMatched(varNode.var);
          return true;
        }
        return false;
      }

    };
  }

  private Predicate<AbstractInsnNode> load(
      final Slot<Integer> counterVariable) {
    return new Predicate<AbstractInsnNode>() {
      @Override
      public Boolean apply(AbstractInsnNode a) {
        // TODO Auto-generated method stub
        if (a.getOpcode() != Opcodes.ILOAD) {
          return false;
        }

        VarInsnNode varNode = (VarInsnNode) a;
        return counterVariable.apply(varNode.var);
      }

    };
  }

  private Predicate<AbstractInsnNode> aGoto() {
    return new Predicate<AbstractInsnNode>() {
      @Override
      public Boolean apply(AbstractInsnNode a) {
        return a.getOpcode() == Opcodes.GOTO;
      }
    };
  }

  private F<MethodTree, Boolean> named(final String string) {
    return new F<MethodTree, Boolean>() {
      @Override
      public Boolean apply(MethodTree a) {
        return a.rawNode().name.equals(string);
      }

    };
  }

  private Predicate<AbstractInsnNode> jumpsTo(
      final Slot<AbstractInsnNode> loopStart) {
    return new Predicate<AbstractInsnNode>() {
      @Override
      public Boolean apply(AbstractInsnNode a) {
        if (!(a instanceof JumpInsnNode)) {
          return false;
        }
        JumpInsnNode jump = (JumpInsnNode) a;
        return loopStart.apply(jump.label);
      }

    };
  }

  private <T extends AbstractInsnNode> Predicate<AbstractInsnNode> isA(
      final Class<T> cls) {
    return new Predicate<AbstractInsnNode>() {
      @Override
      public Boolean apply(AbstractInsnNode a) {
        return a.getClass().isAssignableFrom(cls);
      }
    };
  }

  private static <T> Predicate<T> any(Class<T> t) {
    return new Predicate<T>() {
      @Override
      public Boolean apply(T a) {
        return true;
      }

    };
  };

  private static <T> Predicate<T> matchAndStore(final Predicate<T> target,
      final Slot<T> slot) {
    return new Predicate<T>() {
      @Override
      public Boolean apply(T t) {
        if (target.apply(t)) {
          System.out.println("Strogin " + t);
          slot.setLastMatched(t);
          return true;
        }
        return false;
      }

    };
  }

  private static List<MethodMutatorFactory> asList(
      MethodMutatorFactory... factories) {
    return Arrays.asList(factories);
  }

  private FunctionalList<MutationDetails> findMutationsFor(Class<?> clazz) {
    FunctionalList<MutationDetails> mutations = mutator
        .findMutations(ClassName.fromClass(clazz));
    assertThat(mutations).isNotEmpty();
    return mutations;
  }
  
  protected void printMutant(final Mutant mutant) {    
    final ClassReader reader = new ClassReader(mutant.getBytes());
    reader.accept(new TraceClassVisitor(null, new Textifier(), new PrintWriter(
        System.out)), ClassReader.EXPAND_FRAMES);
 }


}

class HasForLoop {
  public void mutateMe() {
    for (int i = 0; i != 10; i++) {
      System.out.println("" + i);
    }
  }

  public void noLoop() {
    int i = 0;
    if (i++ > 0)
      System.out.println("" + i);
  }

}


