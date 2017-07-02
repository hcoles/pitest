package org.pitest.mutationtest.build.intercept.staticinitializers;

import java.util.Collection;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.pitest.bytecode.analysis.AnalysisFunctions;
import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.bytecode.analysis.MethodTree;
import org.pitest.classinfo.ClassName;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.FunctionalList;
import org.pitest.functional.Option;
import org.pitest.functional.predicate.Predicate;
import org.pitest.functional.prelude.Prelude;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.engine.MethodName;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.PoisonStatus;

/**
 * Identifies and marks mutations in code that is active during class
 * Initialisation.
 * 
 * The analysis is simplistic and non-exhaustive. Code is considered to be
 * for static initialisation if it is
 * 
 * 1. In a static initializer (i.e <clinit>)
 * 2. In a private static method called directly from <clinit>
 * 
 * TODO A better analysis would include private static methods called indirectly from <clinit>
 * and would exclude methods called from location other than <clinit>.
 * 
 */
class StaticInitializerInterceptor implements MutationInterceptor {
  
  private static final MethodName CLINIT = MethodName.fromString("<clinit>");
  
  private Predicate<MutationDetails> isStaticInitCode;

  @Override
  public void begin(ClassTree clazz) {
      analyseClass(clazz);
  }
  
  @Override
  public Collection<MutationDetails> intercept(
      Collection<MutationDetails> mutations, Mutater m) {
    if (isStaticInitCode != null) {
      FunctionalList<MutationDetails> altered = 
          FCollection.filter(mutations, isStaticInitCode)
          .map(setStaticInitializerFlag());
      
      FunctionalList<MutationDetails> notAltered = 
          FCollection.filter(mutations, Prelude.not(isStaticInitCode));
      
      notAltered.addAll(altered);
      return notAltered;
    }
    return mutations;
  }
  
  @Override
  public void end() {
    isStaticInitCode = null;
  }
  
  @SuppressWarnings("unchecked")
  private void analyseClass(ClassTree tree) {
    Option<MethodTree> clinit = tree.methods().findFirst(nameEquals(CLINIT.name()));
        
    if (clinit.hasSome()) {
      FunctionalList<MethodInsnNode> selfCalls = 
          clinit.value().instructions()
        .flatMap(is(MethodInsnNode.class))
        .filter(calls(tree.name()));
      
      Predicate<MethodTree> matchingCalls = Prelude.or(selfCalls.map(toPredicate()));
      
      Predicate<MutationDetails> initOnlyMethods = Prelude.or(tree.methods()
      .filter(isPrivateStatic())
      .filter(matchingCalls)
      .map(AnalysisFunctions.matchMutationsInMethod())
      );
      
      isStaticInitCode = Prelude.or(isInStaticInitializer(), initOnlyMethods);
    }
  }


  private static Predicate<MutationDetails> isInStaticInitializer() {
    return new Predicate<MutationDetails>() {
      @Override
      public Boolean apply(MutationDetails a) {
        return a.getId().getLocation().getMethodName().equals(CLINIT);
      }
      
    };
  }

  private static F<MethodTree, Boolean> isPrivateStatic() {
    return new  F<MethodTree, Boolean>() {
      @Override
      public Boolean apply(MethodTree a) {
        return (a.rawNode().access & Opcodes.ACC_STATIC) != 0
            && (a.rawNode().access & Opcodes.ACC_PRIVATE) != 0;
      }
      
    };
  }


  
  private static F<MethodInsnNode, Predicate<MethodTree>> toPredicate() {   
    return new F<MethodInsnNode, Predicate<MethodTree>> () {
      @Override
      public Predicate<MethodTree> apply(MethodInsnNode a) {
        return matchesCall(a);
      }
    };
  }

  
  private static Predicate<MethodTree> matchesCall(final MethodInsnNode call) {   
    return new Predicate<MethodTree> () {     
      @Override
      public Boolean apply(MethodTree a) {
        return a.rawNode().name.equals(call.name) 
            && a.rawNode().desc.equals(call.desc);
      }
      
    };
  }

  private F<MethodInsnNode, Boolean> calls(final ClassName self) {
    return new F<MethodInsnNode, Boolean>() {
      @Override
      public Boolean apply(MethodInsnNode a) {
        return a.owner.equals(self.asInternalName());
      }
      
    };
  }

  private <T extends AbstractInsnNode> F<AbstractInsnNode,Option<T>> is(final Class<T> clazz) {
    return new  F<AbstractInsnNode,Option<T>>() {
      @SuppressWarnings("unchecked")
      @Override
      public Option<T> apply(AbstractInsnNode a) {
        if (a.getClass().isAssignableFrom(clazz)) {
          return Option.some((T)a);
        }
        return Option.none();
      }
      
    };
    
  }
  
  private Predicate<MethodTree> nameEquals(final String name) {
    return new Predicate<MethodTree>() {
      @Override
      public Boolean apply(MethodTree a) {
        return a.rawNode().name.equals(name);
      }  
    };
  }


  private F<MutationDetails, MutationDetails> setStaticInitializerFlag() {
    return new F<MutationDetails, MutationDetails>() {
      @Override
      public MutationDetails apply(MutationDetails a) {
        return a.withPoisonStatus(PoisonStatus.IS_STATIC_INITIALIZER_CODE);
      }
    };
  }

  @Override
  public InterceptorType type() {
    return InterceptorType.MODIFY;
  }

}
